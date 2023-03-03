package xin.vanilla;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import net.mamoe.mirai.internal.deps.io.ktor.client.engine.ProxyBuilder;
import net.mamoe.mirai.message.data.OfflineAudio;
import net.mamoe.mirai.utils.ExternalResource;
import org.junit.Test;
import xin.vanilla.entity.TestEntities;
import xin.vanilla.entity.TestEntity;
import xin.vanilla.entity.TestTable;
import xin.vanilla.util.Api;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.sqlite.SqliteUtil;
import xin.vanilla.util.sqlite.statement.InsertStatement;
import xin.vanilla.util.sqlite.statement.QueryStatement;
import xin.vanilla.util.sqlite.statement.Statement;

import java.io.File;
import java.io.InputStream;
import java.net.Proxy;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.*;

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
                .where(TestTable::getAddress).likeContains("a");
        System.out.println(sqliteUtil.getEntity(select, TestTable.class));
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
            String[] list = {"Human:","AI:"};
            jsonObject.put("model","text-davinci-003")
                    .put("prompt","用Java实现以下功能: 给定一个对象集合, 对象中的属性有: 姓名, 地址, 权级。以每个对象中的权级大小为概率随机抽取一个对象")
                    .put("max_tokens",4000)
                    .put("temperature",0)
                    .put("top_p",1)
                    .put("frequency_penalty",0)
                    .put("presence_penalty",0.6)
                    .put("stop",list);

            String result = HttpRequest.post("https://api.openai.com/v1/completions").setHttpProxy("localhost",10808)
                    .header("Content-Type", "application/json")
                    .header("Accept-Encoding", "gzip,deflate")
                    .header("Content-Length", "1024")
                    .header("Transfer-Encoding", "chunked")
                    .header("Authorization", "Bearer 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")
                    .body(JSONUtil.toJsonStr(jsonObject))
                    .timeout(40000)
                    .execute()
                    .body();

            JSONObject jsonObject1 = JSONUtil.parseObj(result);
            JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("choices"));
            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
            System.out.println(jsonObject2);
            System.out.println(jsonObject2.get("text"));

            String bake = (String) jsonObject2.get("text");
            bake = ReUtil.delFirst("^\n+", bake);
//                bake = StrUtil.replace(bake,"\n","");
//                StrUtil.trim(bake);
            System.out.println(bake);
        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    @Test
    public void test04(){
//        https://picture.yinux.workers.dev

        try {
            InputStream localhost = HttpRequest.post("https://picture.yinux.workers.dev").setHttpProxy("localhost", 10808).execute().bodyStream();
            byte[] bytes = IoUtil.readBytes(localhost);
            IoUtil.write(FileUtil.getOutputStream("d:/test2.jpg"),true,bytes);
        } catch (IORuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test05(){

        JSONObject jsonObject = JSONUtil.createObj();
        Map<String, Object> map = new HashMap<String, Object> ();
        List list = new ArrayList<Map<String,Object>>();
        list.add(map);
        map.put("role","user");
        map.put("content","用Java实现以下功能: 给定一个对象集合, 对象中的属性有: 姓名, 地址, 权级。以每个对象中的权级大小为概率随机抽取一个对象");
        jsonObject.put("model","gpt-3.5-turbo")
                .put("messages", list);

        try {
            String res = HttpRequest.post("https://api.openai.com/v1/chat/completions").setHttpProxy("localhost", 10808)
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer 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")
                    .body(JSONUtil.toJsonStr(jsonObject))
                    .timeout(40000)
                    .execute()
                    .body();

            JSONObject jsonObject1 = JSONUtil.parseObj(res);
            JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("choices"));
            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
            JSONObject jsonObject3 = JSONUtil.parseObj(jsonObject2.get("message"));
//            System.out.println(jsonObject3.get("content"));
//            System.out.println(jsonObject2.get("text"));

            String bake = (String) jsonObject3.get("content");
            bake = ReUtil.delFirst("^\n+", bake);
            System.out.println(bake);
        } catch (IORuntimeException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test06() {
        String appid = "20210126000681992";
        String salt = "112";
        String key = "X3WYhQFwg6O8cPWv7dTe";
        String q = "这个工具的用处类似于Apache Commons Lang中的StringUtil，之所以使用StrUtil而不是使用StringUtil是因为前者更短，而且Str这个简写我想已经深入人心了";
        String sign = SecureUtil.md5(appid + q + salt + key);
        System.out.println(sign);
        Map<String, Object> map = new HashMap<>();
        map.put("from", "auto");
        map.put("to", "jp");
        map.put("appid", appid);
        map.put("q", q);
        map.put("salt", salt);
        map.put("sign", sign);

        String body = HttpRequest.post("https://fanyi-api.baidu.com/api/trans/vip/translate")
                .form(map)
                .execute().body();
        JSONObject jsonObject1 = JSONUtil.parseObj(body);
        JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("trans_result"));
        JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
        String res = (String) jsonObject2.get("dst");
        String path = "G:\\MoeGoe\\model\\temp\\";
        String id = IdUtil.randomUUID();
        path = path + id + ".wav";
        String cmd = " C:\\Users\\86152\\.conda\\envs\\vits\\python.exe D:\\MoeGoe-master\\MoeGoe.py " + res + " " + path;
        try {
            System.out.println(cmd);
//            Process process = Runtime.getRuntime().exec("cmd /c C:\\Users\\86152\\.conda\\envs\\vits\\python.exe D:\\MoeGoe-master\\MoeGoe.py" + res + " " + path);
            Process process = Runtime.getRuntime().exec(cmd);
//            Api.sendMessage(group, "消息执行");
            process.waitFor();
//            Thread.sleep(1000 * 10);
////                Path path = Paths.get("E:\\model\\dd.wav");
//            File file = new File(path);
//            ExternalResource externalResource = ExternalResource.create(file);
//            OfflineAudio offlineAudio = group.uploadAudio(externalResource);
//            Api.sendMessage(group, offlineAudio);
            System.out.println("ok");
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
