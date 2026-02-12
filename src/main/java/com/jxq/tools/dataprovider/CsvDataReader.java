package com.jxq.tools.dataprovider;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CSV 数据读取器
 * 读取 .csv 文件，第一行作为表头（跳过），后续行作为测试数据。
 * 返回 Object[][] 格式供 TestNG @DataProvider 使用。
 */
public class CsvDataReader {

    private static final Logger logger = LoggerFactory.getLogger(CsvDataReader.class);

    /**
     * 读取 CSV 文件
     *
     * @param resourcePath classpath 下的资源路径
     * @return Object[][] 测试数据
     */
    public static Object[][] readCsv(String resourcePath) {
        try (InputStream inputStream = CsvDataReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("CSV 文件未找到: " + resourcePath);
            }

            CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            List<String[]> allRows = csvReader.readAll();
            csvReader.close();

            if (allRows.size() <= 1) {
                logger.warn("CSV 文件无数据行: {}", resourcePath);
                return new Object[0][0];
            }

            // 第一行为表头，跳过
            int dataRowCount = allRows.size() - 1;
            Object[][] data = new Object[dataRowCount][];

            for (int i = 0; i < dataRowCount; i++) {
                String[] row = allRows.get(i + 1);
                // trim 每个字段
                Object[] rowData = new Object[row.length];
                for (int j = 0; j < row.length; j++) {
                    rowData[j] = row[j] != null ? row[j].trim() : "";
                }
                data[i] = rowData;
            }

            logger.info("从 CSV 读取 {} 条数据: {}", dataRowCount, resourcePath);
            return data;

        } catch (IOException | CsvException e) {
            throw new RuntimeException("读取 CSV 文件失败: " + resourcePath, e);
        }
    }
}
