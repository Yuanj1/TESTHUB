package com.jxq.tools.dataprovider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * JSON 数据读取器
 * 读取 .json 文件，支持数组格式 [{...}, {...}]。
 * 将每个 JSON 对象的 values 转为 Object[]。
 * 返回 Object[][] 格式供 TestNG @DataProvider 使用。
 */
public class JsonDataReader {

    private static final Logger logger = LoggerFactory.getLogger(JsonDataReader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 读取 JSON 文件
     *
     * @param resourcePath classpath 下的资源路径
     * @return Object[][] 测试数据
     */
    public static Object[][] readJson(String resourcePath) {
        try (InputStream inputStream = JsonDataReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new RuntimeException("JSON 文件未找到: " + resourcePath);
            }

            // 读取为 List<LinkedHashMap> 保持字段顺序
            List<LinkedHashMap<String, Object>> dataList = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<LinkedHashMap<String, Object>>>() {}
            );

            if (dataList.isEmpty()) {
                logger.warn("JSON 文件无数据: {}", resourcePath);
                return new Object[0][0];
            }

            Object[][] data = new Object[dataList.size()][];
            for (int i = 0; i < dataList.size(); i++) {
                Collection<Object> values = dataList.get(i).values();
                // 将所有 value 转为 String
                Object[] rowData = new Object[values.size()];
                int j = 0;
                for (Object value : values) {
                    rowData[j++] = value != null ? String.valueOf(value) : "";
                }
                data[i] = rowData;
            }

            logger.info("从 JSON 读取 {} 条数据: {}", dataList.size(), resourcePath);
            return data;

        } catch (IOException e) {
            throw new RuntimeException("读取 JSON 文件失败: " + resourcePath, e);
        }
    }
}
