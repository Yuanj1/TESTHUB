package com.jxq.tools.dataprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据驱动统一入口工具类
 * 根据文件扩展名自动选择对应的 DataReader，
 * 支持 .xlsx、.csv、.json 三种数据格式。
 *
 * 用法示例：
 * <pre>
 * &#64;DataProvider(name = "testData")
 * public Object[][] testData() {
 *     return DataProviderUtils.getData("parameters/search/SearchTagsData.csv");
 * }
 * </pre>
 */
public class DataProviderUtils {

    private static final Logger logger = LoggerFactory.getLogger(DataProviderUtils.class);

    /**
     * 根据文件扩展名自动选择 Reader 读取测试数据
     *
     * @param resourcePath classpath 下的资源路径（支持 .xlsx / .csv / .json）
     * @return Object[][] 测试数据
     */
    public static Object[][] getData(String resourcePath) {
        if (resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("resourcePath 不能为空");
        }

        String lowerPath = resourcePath.toLowerCase();
        Object[][] data;

        if (lowerPath.endsWith(".xlsx") || lowerPath.endsWith(".xls")) {
            logger.info("使用 ExcelDataReader 读取: {}", resourcePath);
            data = ExcelDataReader.readExcel(resourcePath);
        } else if (lowerPath.endsWith(".csv")) {
            logger.info("使用 CsvDataReader 读取: {}", resourcePath);
            data = CsvDataReader.readCsv(resourcePath);
        } else if (lowerPath.endsWith(".json")) {
            logger.info("使用 JsonDataReader 读取: {}", resourcePath);
            data = JsonDataReader.readJson(resourcePath);
        } else {
            throw new IllegalArgumentException("不支持的文件格式: " + resourcePath
                    + "，仅支持 .xlsx / .csv / .json");
        }

        logger.info("数据驱动加载完成，共 {} 组测试数据", data.length);
        return data;
    }

    /**
     * 读取 Excel 文件，指定 Sheet 名称
     *
     * @param resourcePath classpath 下的资源路径
     * @param sheetName    Sheet 名称
     * @return Object[][] 测试数据
     */
    public static Object[][] getData(String resourcePath, String sheetName) {
        if (!resourcePath.toLowerCase().endsWith(".xlsx") && !resourcePath.toLowerCase().endsWith(".xls")) {
            throw new IllegalArgumentException("指定 sheetName 仅支持 Excel 文件: " + resourcePath);
        }
        logger.info("使用 ExcelDataReader 读取: {}, Sheet: {}", resourcePath, sheetName);
        return ExcelDataReader.readExcel(resourcePath, sheetName);
    }
}
