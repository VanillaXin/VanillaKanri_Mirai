package xin.vanilla.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import xin.vanilla.VanillaKanri;

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
        String q = command;
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
        return res;
    }

    public static String chatGPT(String command){

        String key = Va.getGlobalConfig().getOther().getChatGPTKey();
        JSONObject jsonObject = JSONUtil.createObj();
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        list.add(map);
        map.put("role", "user");
        map.put("content", command);
        jsonObject.set("model", "gpt-3.5-turbo").set("messages", list);

        try {
            String res = HttpRequest.post("http://35.201.187.84:8888/v1/chat/completions")
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
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
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
