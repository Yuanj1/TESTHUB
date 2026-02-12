package com.jxq.tools.dataprovider;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 数据读取器
 * 读取 .xlsx 文件，第一行作为表头（跳过），后续行作为测试数据。
 * 返回 Object[][] 格式供 TestNG @DataProvider 使用。
 */
public class ExcelDataReader {

    private static final Logger logger = LoggerFactory.getLogger(ExcelDataReader.class);

    /**
     * 读取 Excel 文件，默认读取第一个 Sheet
     *
     * @param resourcePath classpath 下的资源路径
     * @return Object[][] 测试数据
     */
    public static Object[][] readExcel(String resourcePath) {
        return readExcel(resourcePath, null);
    }

    /**
     * 读取 Excel 文件，指定 Sheet 名称
     *
     * @param resourcePath classpath 下的资源路径
     * @param sheetName    Sheet 名称，为 null 时读取第一个 Sheet
     * @return Object[][] 测试数据
     */
    public static Object[][] readExcel(String resourcePath, String sheetName) {
        List<Object[]> dataList = new ArrayList<>();

        try (InputStream inputStream = ExcelDataReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("Excel 文件未找到: " + resourcePath);
            }

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet;
            if (sheetName != null && !sheetName.isEmpty()) {
                sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new RuntimeException("Sheet 未找到: " + sheetName);
                }
            } else {
                sheet = workbook.getSheetAt(0);
            }

            int totalRows = sheet.getPhysicalNumberOfRows();
            if (totalRows <= 1) {
                logger.warn("Excel 文件无数据行: {}", resourcePath);
                workbook.close();
                return new Object[0][0];
            }

            // 第一行为表头，获取列数
            Row headerRow = sheet.getRow(0);
            int totalCols = headerRow.getPhysicalNumberOfCells();

            // 从第二行开始读取数据
            for (int i = 1; i < totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                Object[] rowData = new Object[totalCols];
                for (int j = 0; j < totalCols; j++) {
                    Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData[j] = getCellValueAsString(cell);
                }
                dataList.add(rowData);
            }

            workbook.close();
            logger.info("从 Excel 读取 {} 条数据: {}", dataList.size(), resourcePath);

        } catch (IOException e) {
            throw new RuntimeException("读取 Excel 文件失败: " + resourcePath, e);
        }

        return dataList.toArray(new Object[0][0]);
    }

    /**
     * 将 Cell 值转为 String
     */
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                // 避免数字被转为科学计数法
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue) && !Double.isInfinite(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return "";
            default:
                return "";
        }
    }
}
