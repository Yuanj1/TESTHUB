package com.jxq.douban;

import com.alibaba.fastjson.JSONObject;
import com.jxq.douban.domain.MovieResponseVO;
import com.jxq.tools.JsonSchemaUtils;
import com.jxq.tools.dataprovider.DataProviderUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import retrofit2.Response;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Auther: jx
 * @Date: 2018/7/5 10:48
 * @Description: 豆瓣首页接口测试 - 数据驱动模式
 */
public class SearchTagsTest {
    private static Properties properties;
    private static HttpSearch implSearch;
    private static String SCHEMA_PATH = "parameters/search/schema/SearchTagsMovie.json";

    @BeforeSuite
    public void beforeSuite() throws IOException {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream("env.properties");
        properties = new Properties();
        properties.load(stream);
        String host = properties.getProperty("douban.host");
        implSearch = new HttpSearch(host);
        stream.close();
    }

    /**
     * 数据驱动：从 CSV 文件加载测试数据
     * 数据文件路径: parameters/search/SearchTagsData.csv
     * 也可替换为 .json 或 .xlsx 文件，DataProviderUtils 会自动识别格式
     */
    @DataProvider(name = "searchTagsData")
    public Object[][] searchTagsData() {
        return DataProviderUtils.getData("parameters/search/SearchTagsData.csv");
    }

    @Test(dataProvider = "searchTagsData", description = "数据驱动: SearchTags接口测试")
    public void testSearchTags(String type, String source) throws IOException {
        Response<MovieResponseVO> response = implSearch.searchTags(type, source);
        MovieResponseVO body = response.body();
        Assert.assertNotNull(body, "response.body()");
        // 响应返回内容通过 schema 标准校验
        JsonSchemaUtils.assertResponseJsonSchema(SCHEMA_PATH, JSONObject.toJSONString(body));
        // 再 Json 化成对象校验
        Assert.assertNotNull(body.getTags(), "tags");
    }
}
