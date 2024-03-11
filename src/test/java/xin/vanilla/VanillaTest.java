package xin.vanilla;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import org.junit.Test;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.entity.TestEntities;
import xin.vanilla.entity.TestEntity;
import xin.vanilla.entity.TestTable;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.sqlite.SqliteUtil;
import xin.vanilla.util.sqlite.statement.InsertStatement;
import xin.vanilla.util.sqlite.statement.QueryStatement;
import xin.vanilla.util.sqlite.statement.Statement;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class VanillaTest {
    @Test
    public void testGetByLine() {
        String s = "1这是第一行\n"
                + "2这是第二行\n"
                + "3这是第三行\n"
                + "4这是第四行\n"
                + "5这是第五行\n"
                + "6这是第六行\n"
                + "7这是第七行\n";

        System.out.println(StringUtils.getByLine(s, 1, 5, ""));
        System.out.println(StringUtils.getByLine(s, 1, 5, "剩余[num]行"));
    }

    @Test
    public void testCreateTable() throws SQLException {
        SqliteUtil sqliteUtil = SqliteUtil.getInstance();
        if (!sqliteUtil.containsTable("test_table")) {
            sqliteUtil.executeSql(
                    "CREATE TABLE test_table " +
                            "(id        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , " +
                            " name      TEXT                              NOT NULL, " +
                            " age       INTEGER                           NOT NULL, " +
                            " address   CHAR(50), " +
                            " salary    REAL)");
        }
        System.out.println("是否存在`test_table`表: " + sqliteUtil.containsTable("test_table"));
    }

    @Test
    public void testInsert() throws SQLException {
        testCreateTable();
        SqliteUtil sqliteUtil = SqliteUtil.getInstance();

        String name = NanoIdUtils.randomNanoId(new SecureRandom(), "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray(), 5);
        String age = NanoIdUtils.randomNanoId(new SecureRandom(), "0123456789".toCharArray(), 2);
        String address = NanoIdUtils.randomNanoId(new SecureRandom(), "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray(), 10);

        InsertStatement insert = InsertStatement.produce("test_table")
                .put("name", name)
                .put("age", age)
                .put("address", address)
                .put("salary", Float.parseFloat(age) * 100 / 3);
        System.out.println("新增条数: " + sqliteUtil.insert(insert));

        Statement select = QueryStatement.produce().from("test_table");
        System.out.println(sqliteUtil.getEntity(select, TestTable.class));
        System.out.println(sqliteUtil.getList(select, TestTable.class));
        System.out.println(sqliteUtil.getList(select.orderBy("id").desc().limit(2), TestTable.class));

        System.out.println("最后插入主键: " + sqliteUtil.getLastInsertRowId("test_table"));
    }

    @Test
    public void testQuery() throws SQLException {
        SqliteUtil sqliteUtil = SqliteUtil.getInstance();
        Statement select = QueryStatement.produce().from("test_table")
                .where(TestTable::getId).gt(0)
                .andRegexp(TestTable::getAddress, ".*?a.*?");
        TestTable entity = sqliteUtil.getEntity(select, TestTable.class);
        System.out.println(entity);
    }

    @Test
    public void test01() {
        TestEntity testEntity = new TestEntity();
        testEntity.setName("TsukiMaaii");
        testEntity.setAge(19);
        String s = JSON.toJSONString(testEntity);

        TestEntity testEntity1 = JSON.parseObject(s, TestEntity.class);
        System.out.println(testEntity1);

    }

    @Test
    public void test02() {
        TestEntities testEntities = new TestEntities();
        testEntities.setEntities(new ArrayList<TestEntity>() {{
            add(new TestEntity("123", 456));
            add(new TestEntity("789", 1));
            add(new TestEntity("012", 2));
        }});
        testEntities.setPage(233);
        testEntities.setTag("mmp");
        String s = JSON.toJSONString(testEntities);

        TestEntities testEntities1 = JSON.parseObject(s, TestEntities.class);
        System.out.println(testEntities1);
    }


    @Test
    public void test03() {
        try {

            JSONObject jsonObject = JSONUtil.createObj();
            String[] list = {"Human:", "AI:"};
            jsonObject.set("model", "text-davinci-003")
                    .set("prompt", "用Java实现以下功能: 给定一个对象集合, 对象中的属性有: 姓名, 地址, 权级。以每个对象中的权级大小为概率随机抽取一个对象")
                    .set("max_tokens", 4000)
                    .set("temperature", 0)
                    .set("top_p", 1)
                    .set("frequency_penalty", 0)
                    .set("presence_penalty", 0.6)
                    .set("stop", list);

            try (HttpResponse response = HttpRequest.post("https://api.openai.com/v1/completions").setHttpProxy("localhost", 10808)
                    .header("Content-Type", "application/json")
                    .header("Accept-Encoding", "gzip,deflate")
                    .header("Content-Length", "1024")
                    .header("Transfer-Encoding", "chunked")
                    .header("Authorization", "Bearer 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")
                    .body(JSONUtil.toJsonStr(jsonObject))
                    .timeout(40000)
                    .execute()) {

                String result = response.body();

                JSONObject jsonObject1 = JSONUtil.parseObj(result);
                JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("choices"));
                JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
                System.out.println(jsonObject2);
                System.out.println(jsonObject2.get("text"));

                String bake = (String) jsonObject2.get("text");
                bake = ReUtil.delFirst("^\n+", bake);
                System.out.println(bake);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test04() {
        // https://picture.yinux.workers.dev

        try (HttpResponse response = HttpRequest.post("https://picture.yinux.workers.dev").setHttpProxy("localhost", 10808).execute()) {
            try (InputStream localhost = response.bodyStream()) {
                byte[] bytes = IoUtil.readBytes(localhost);
                IoUtil.write(FileUtil.getOutputStream("d:/test2.jpg"), true, bytes);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IORuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test05() {

        JSONObject jsonObject = JSONUtil.createObj();
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(map);
        map.put("role", "user");
        map.put("content", "用Java实现以下功能: 给定一个对象集合, 对象中的属性有: 姓名, 地址, 权级。以每个对象中的权级大小为概率随机抽取一个对象");
        jsonObject.set("model", "gpt-3.5-turbo")
                .set("messages", list);

        try (HttpResponse execute = HttpRequest.post("https://api.openai.com/v1/chat/completions").setHttpProxy("localhost", 10808)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer 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")
                .body(JSONUtil.toJsonStr(jsonObject))
                .timeout(40000)
                .execute()) {
            String body = execute.body();

            JSONObject jsonObject1 = JSONUtil.parseObj(body);
            JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("choices"));
            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
            JSONObject jsonObject3 = JSONUtil.parseObj(jsonObject2.get("message"));

            String bake = (String) jsonObject3.get("content");
            bake = ReUtil.delFirst("^\n+", bake);
            System.out.println(bake);
        } catch (IORuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test06() {

    }
}
