package xin.vanilla.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 整合部分接口
 */
public class Api {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;


    /**
     * 翻译接口
     */
    public static String fanyi_jp(String command) {
        String appid = Va.getGlobalConfig().getOther().getFanyi_baidu_id();
        String salt = "112";
        String key = Va.getGlobalConfig().getOther().getFanyi_baidu_key();
        String sign = SecureUtil.md5(appid + command + salt + key);
        System.out.println(sign);
        Map<String, Object> map = new HashMap<>();
        map.put("from", "auto");
        map.put("to", "jp");
        map.put("appid", appid);
        map.put("q", command);
        map.put("salt", salt);
        map.put("sign", sign);

        String body = HttpRequest.post("https://fanyi-api.baidu.com/api/trans/vip/translate")
                .form(map)
                .execute().body();
        JSONObject jsonObject1 = JSONUtil.parseObj(body);
        JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("trans_result"));
        JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
        String res = (String) jsonObject2.get("dst");
        return res;
    }

    public static String AiPicture(String prompt, String unPrompt) {

        String key = Va.getGlobalConfig().getOther().getAiDrawKey();
        String aiDrawUrl = Va.getGlobalConfig().getOther().getAiDrawUrl();

        String task = "task(" + IdUtil.randomUUID() + ")";
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("fn_index", 100);

        JSONArray jsonArray = new JSONArray();
        jsonArray.add(task);

        jsonArray.add(prompt);
        jsonArray.add(unPrompt);
        jsonArray.add("[]");
        jsonArray.add(20);
        jsonArray.add("DPM++ 2M Karras");
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(1);
        jsonArray.add(1);
        jsonArray.add(7);
        jsonArray.add(-1);
        jsonArray.add(-1);
        jsonArray.add(0);
        jsonArray.add(0);
        jsonArray.add(0);
        jsonArray.add(false);
        jsonArray.add(824);
        jsonArray.add(624);
        jsonArray.add(false);
        jsonArray.add(0.7);
        jsonArray.add(2);
        jsonArray.add("Latent");
        jsonArray.add(0);
        jsonArray.add(0);
        jsonArray.add(0);
        jsonArray.add("None");
        jsonArray.add("");
        jsonArray.add(false);
        jsonArray.add("none");
        jsonArray.add("None");
        jsonArray.add(1);
        jsonArray.add("");
        jsonArray.add(false);
        jsonArray.add("Scale to Fit (Inner Fit)");
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(64);
        jsonArray.add(64);
        jsonArray.add(64);
        jsonArray.add(0);
        jsonArray.add(1);
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add("none");
        jsonArray.add("None");
        jsonArray.add(1);
        jsonArray.add("");
        jsonArray.add(false);
        jsonArray.add("Scale to Fit (Inner Fit)");
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(64);
        jsonArray.add(64);
        jsonArray.add(64);
        jsonArray.add(0);
        jsonArray.add(1);
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add("none");
        jsonArray.add("None");
        jsonArray.add(1);
        jsonArray.add("");
        jsonArray.add(false);
        jsonArray.add("Scale to Fit (Inner Fit)");
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(64);
        jsonArray.add(64);
        jsonArray.add(64);
        jsonArray.add(0);
        jsonArray.add(1);
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add("");
        jsonArray.add("Seed");
        jsonArray.add("");
        jsonArray.add("Nothing");
        jsonArray.add("");
        jsonArray.add("Nothing");
        jsonArray.add("");
        jsonArray.add(true);
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(null);
        jsonArray.add(null);
        jsonArray.add(null);
        jsonArray.add(50);
        jsonArray.add("[]");
        jsonArray.add("");
        jsonArray.add("");
        jsonArray.add("");

        jsonObject.set("data", jsonArray);

        System.out.println(com.alibaba.fastjson2.JSONObject.toJSONString(jsonObject));
        String res = HttpRequest.post(aiDrawUrl + "/run/predict/")
                .header("Content-Type", "application/json")
                .header("authorization", key)
                .body(com.alibaba.fastjson2.JSONObject.toJSONString(jsonObject))
                .timeout(100000)
                .execute()
                .body();

        JSONObject jsonObject1 = JSONUtil.parseObj(res);
        JSONArray jsonArray1 = JSONUtil.parseArray(jsonObject1.get("data"));
        JSONArray jsonArray2 = JSONUtil.parseArray(jsonArray1.get(0));
        JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray2.get(0));

        return (String) jsonObject2.get("name");
    }

    public static String chatGPT(String command) {

        String key = Va.getGlobalConfig().getOther().getChatGPTKey();
        String chatGPTUrl = Va.getGlobalConfig().getOther().getChatGPTUrl();
        JSONObject jsonObject = JSONUtil.createObj();
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(map);
        map.put("role", "user");
        map.put("content", command);
        jsonObject.set("model", "gpt-3.5-turbo").set("messages", list);

        try {
            String res = HttpRequest.post(chatGPTUrl)
                    .setHttpProxy("localhost", 10808)
                    .header("Content-Type", "application/json")
                    .header("Authorization", key)
                    .body(JSONUtil.toJsonStr(jsonObject))
                    .timeout(40000)
                    .execute()
                    .body();

            JSONObject jsonObject1 = JSONUtil.parseObj(res);
            JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("choices"));
            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
            JSONObject jsonObject3 = JSONUtil.parseObj(jsonObject2.get("message"));
            return (String) jsonObject3.get("content");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 通过图片链接构建图片对象
     */
    @NotNull
    public static Image uploadImageByUrl(String url, Contact contact) {
        ExternalResource resource;
        try (HttpResponse response = HttpRequest.get(url).execute()) {
            try (InputStream inputStream = response.bodyStream()) {
                resource = ExternalResource.Companion.create(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ExternalResource.uploadAsImage(resource, contact);
    }

    /**
     * 通过图片链接构建图片对象
     */
    @NotNull
    public static Image uploadImageByUrl(String url, Proxy proxy, Contact contact) {
        ExternalResource resource;
        try (HttpResponse response = HttpRequest.get(url).setProxy(proxy).execute()) {
            try (InputStream inputStream = response.bodyStream()) {
                resource = ExternalResource.Companion.create(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ExternalResource.uploadAsImage(resource, contact);
    }

    /**
     * 通过图片链接构建图片对象
     */
    @NotNull
    public static Image uploadImageByUrl(String url, String proxy, Contact contact) {
        ExternalResource resource;
        String[] split = proxy.split(":");
        try (HttpResponse response = HttpRequest.get(url).setHttpProxy(split[0], Integer.parseInt(split[1])).execute()) {
            try (InputStream inputStream = response.bodyStream()) {
                resource = ExternalResource.Companion.create(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return ExternalResource.uploadAsImage(resource, contact);
    }

    /**
     * 发送消息
     */
    public static MessageReceipt<Contact> sendMessage(Contact contact, String message) {
        if (contact == null) return null;
        if (StringUtils.isNullOrEmpty(message)) return null;
        // 反转义事件特殊码
        if (message.contains("\\(:vaevent:\\)"))
            message = message.replace("\\(:vaevent:\\)", "(:vaevent:)");
        MessageReceipt<Contact> contactMessageReceipt = contact.sendMessage(message);
        Va.addMsgSendCount();
        Va.getMessageCache().addMsg(contact, contactMessageReceipt.getSource(), MessageUtils.newChain(new PlainText(message)));
        return contactMessageReceipt;
    }

    /**
     * 发送消息
     */
    public static MessageReceipt<Contact> sendMessage(Contact contact, Message message) {
        if (contact == null) return null;
        if (StringUtils.isNullOrEmpty(message.contentToString())) return null;
        // 反转义事件特殊码
        if (message instanceof MessageChain) {
            if (message.contentToString().contains("\\(:vaevent:\\)")) {
                MessageChain messageChain = (MessageChain) message;
                MessageChainBuilder messages = new MessageChainBuilder();
                for (SingleMessage singleMessage : messageChain) {
                    if (singleMessage instanceof PlainText) {
                        PlainText plainText = (PlainText) singleMessage;
                        messages.add(plainText.contentToString().replace("\\(:vaevent:\\)", "(:vaevent:)"));
                    } else {
                        messages.add(singleMessage);
                    }
                }
                message = messages.build();
            }
        }
        MessageReceipt<Contact> contactMessageReceipt = contact.sendMessage(message);
        Va.addMsgSendCount();
        Va.getMessageCache().addMsg(contact, contactMessageReceipt.getSource(), message);
        return contactMessageReceipt;
    }
}
