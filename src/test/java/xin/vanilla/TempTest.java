package xin.vanilla;


import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TempTest {

    @Test
    public  void test(){
        Map<String, Object> map = new HashMap<>();
        JSONArray jsonArray = new JSONArray();
        jsonArray.add("Q: なぜ猫はコンピューターが嫌いですか？\n" +
                "A: マウスが使えないから！");
        jsonArray.add("Auto");
        jsonArray.add("女");
        jsonArray.add(0);
        jsonArray.add(0);
        jsonArray.add("msz2");
        jsonArray.add("wav");
        jsonArray.add(-4);
        jsonArray.add(true);
        jsonArray.add(0);
        jsonArray.add(-40);
        jsonArray.add(0.4);
        jsonArray.add(0.5);
        jsonArray.add(0);
        jsonArray.add(0);
        jsonArray.add(0.75);
        jsonArray.add("dio");
        jsonArray.add(0);
        jsonArray.add(0.05);
        jsonArray.add(100);
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(0);

        map.put("data",jsonArray);
        map.put("event_data",null);
        map.put("fn_index",8);
        System.out.println(com.alibaba.fastjson2.JSONObject.toJSONString(map));

        try (HttpResponse response = HttpRequest.post("http://127.0.0.1:7860" + "/run/predict/").header("Content-Type", "application/json").body(com.alibaba.fastjson2.JSONObject.toJSONString(map)).timeout(100000).execute()) {
            String body = response.body();
            JSONObject jsonObject1 = JSONUtil.parseObj(body);
            JSONArray jsonArray1 = JSONUtil.parseArray(jsonObject1.get("data"));
            JSONObject jsonObject = JSONUtil.parseObj(jsonArray1.get(1));
            String name = jsonObject.get("name").toString();

//            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray2.get(0));
            System.out.println(name);

        }

    }
}
