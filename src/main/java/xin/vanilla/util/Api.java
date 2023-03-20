package xin.vanilla.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.chat.ChatCompletion;
import com.unfbx.chatgpt.entity.chat.ChatCompletionResponse;
import com.unfbx.chatgpt.interceptor.OpenAILogger;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.KeyRepEntity;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 整合部分接口
 */
public class Api {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;


    /**
     * 翻译接口
     */
    public static String translateToJP(String command) {
        String appid = Va.getGlobalConfig().getOther().getTranslateBaiduId();
        String salt = "112";
        String key = Va.getGlobalConfig().getOther().getTranslateBaiduKey();
        String sign = SecureUtil.md5(appid + command + salt + key);
        System.out.println(sign);
        Map<String, Object> map = new HashMap<>();
        map.put("from", "auto");
        map.put("to", "jp");
        map.put("appid", appid);
        map.put("q", command);
        map.put("salt", salt);
        map.put("sign", sign);

        try (HttpResponse response = HttpRequest.post("https://fanyi-api.baidu.com/api/trans/vip/translate")
                .form(map)
                .execute()) {
            String body = response.body();
            JSONObject jsonObject1 = JSONUtil.parseObj(body);
            JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("trans_result"));
            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
            return (String) jsonObject2.get("dst");
        }
    }

    public static String aiPicture(String prompt, String unPrompt) {

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
        try (HttpResponse response = HttpRequest.post(aiDrawUrl + "/run/predict/")
                .header("Content-Type", "application/json")
                .header("authorization", key)
                .body(com.alibaba.fastjson2.JSONObject.toJSONString(jsonObject))
                .timeout(100000)
                .execute()) {
            String body = response.body();
            JSONObject jsonObject1 = JSONUtil.parseObj(body);
            JSONArray jsonArray1 = JSONUtil.parseArray(jsonObject1.get("data"));
            JSONArray jsonArray2 = JSONUtil.parseArray(jsonArray1.get(0));
            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray2.get(0));

            return (String) jsonObject2.get("name");
        }
    }

    @NotNull
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

        try (HttpResponse response = HttpRequest.post(chatGPTUrl)
                .setHttpProxy("localhost", 10808)
                .header("Content-Type", "application/json")
                .header("Authorization", key)
                .body(JSONUtil.toJsonStr(jsonObject))
                .timeout(40000)
                .execute()) {
            String body = response.body();

            JSONObject jsonObject1 = JSONUtil.parseObj(body);
            JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("choices"));
            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
            JSONObject jsonObject3 = JSONUtil.parseObj(jsonObject2.get("message"));
            String content = (String) jsonObject3.get("content");
            return content == null ? "" : content;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String chatGPT(String nick, String msg) {
        return chatGPT(nick, Va.getGlobalConfig().getOther().getChatGPTKey(), msg);
    }

    public static String chatGPT(String nick, String key, String msg) {
        String chatGPTKey = key;
        if (StringUtils.isNullOrEmptyEx(chatGPTKey))
            chatGPTKey = Va.getGlobalConfig().getOther().getChatGPTKey();
        if (StringUtils.isNullOrEmptyEx(chatGPTKey)) return "";
        String chatGPTUrl = Va.getGlobalConfig().getOther().getChatGPTUrl();
        if (StringUtils.isNullOrEmptyEx(chatGPTUrl)) return "";

        List<String> context = Va.getGlobalConfig().getOther().getChatGPTContext();

        String proxyHost = Va.getGlobalConfig().getOther().getSystemProxyHost();
        int proxyPort = Va.getGlobalConfig().getOther().getSystemProxyPort();

        try {
            OpenAiClient.Builder builder = OpenAiClient.builder();

            if (!StringUtils.isNullOrEmptyEx(proxyHost) && proxyPort > 0)
                builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));

            // 日志
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OpenAiClient openAiClient = builder.apiKey(chatGPTKey)
                    .apiHost(chatGPTUrl)
                    .interceptor(Collections.singletonList(httpLoggingInterceptor))
                    .build();

            List<com.unfbx.chatgpt.entity.chat.Message> messages = new ArrayList<>();

            com.unfbx.chatgpt.entity.chat.Message systemMessage = new com.unfbx.chatgpt.entity.chat.Message();
            systemMessage.setRole(com.unfbx.chatgpt.entity.chat.Message.Role.SYSTEM.getName());
            systemMessage.setContent("回复尽可能短");
            messages.add(systemMessage);

            // 设置预设文本(风格)
            for (String s : context) {
                com.unfbx.chatgpt.entity.chat.Message nature = new com.unfbx.chatgpt.entity.chat.Message();
                nature.setRole(com.unfbx.chatgpt.entity.chat.Message.Role.USER.getName());
                nature.setContent(s);
                messages.add(nature);

                com.unfbx.chatgpt.entity.chat.Message natureRep = new com.unfbx.chatgpt.entity.chat.Message();
                natureRep.setRole(com.unfbx.chatgpt.entity.chat.Message.Role.ASSISTANT.getName());
                natureRep.setContent("好的");
                messages.add(natureRep);
            }

            com.unfbx.chatgpt.entity.chat.Message userName = new com.unfbx.chatgpt.entity.chat.Message();
            userName.setRole(com.unfbx.chatgpt.entity.chat.Message.Role.USER.getName());
            userName.setContent("我的名字是: " + nick);
            messages.add(userName);

            com.unfbx.chatgpt.entity.chat.Message userMessage = new com.unfbx.chatgpt.entity.chat.Message();
            userMessage.setRole(com.unfbx.chatgpt.entity.chat.Message.Role.USER.getName());
            userMessage.setContent(msg);
            messages.add(userMessage);

            ChatCompletion chatCompletion = ChatCompletion.builder()
                    .temperature(0.8)
                    .presencePenalty(1)
                    .n(1)
                    .topP(1.0)
                    .model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
                    .maxTokens(2000)
                    .messages(messages)
                    .build();

            ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
            return chatCompletionResponse.getChoices().get(0).getMessage().getContent();
        } catch (Exception ignored) {
        }
        return Va.getGlobalConfig().getOther().getChatGPTDefaultBack();
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


    // region sendMessage

    /**
     * 发送消息
     */
    public static MessageReceipt<Contact> sendMessage(Contact contact, String message) {
        if (contact == null) return null;
        if (StringUtils.isNullOrEmpty(message)) return null;
        // 反转义事件特殊码
        if (message.contains("\\(:vaevent:\\)"))
            message = message.replace("\\(:vaevent:\\)", "(:vaevent:)");

        KeyRepEntity rep = new KeyRepEntity(contact);
        // 判断是否包含延时特殊码
        message = deVanillaCode(rep, message);

        rep.setRep(MessageUtils.newChain(new PlainText(message)));
        return sendMessage(rep);
    }

    /**
     * 发送消息
     */
    public static MessageReceipt<Contact> sendMessage(Contact contact, Message message) {
        if (contact == null) return null;
        if (StringUtils.isNullOrEmpty(message.contentToString())) return null;
        KeyRepEntity rep = new KeyRepEntity(contact);
        // 反转义事件特殊码
        return sendMessage(rep, message);
    }

    /**
     * 发送消息
     */
    public static MessageReceipt<Contact> sendMessage(KeyRepEntity rep, Message message) {
        if (StringUtils.isNullOrEmpty(message.contentToString())) return null;
        // 反转义事件特殊码
        if (message instanceof MessageChain) {
            if (message.contentToString().contains("\\(:vaevent:\\)") || message.contentToString().contains("[vacode:")) {
                MessageChain messageChain = (MessageChain) message;
                MessageChainBuilder messages = new MessageChainBuilder();
                for (SingleMessage singleMessage : messageChain) {
                    if (singleMessage instanceof PlainText) {
                        PlainText plainText = (PlainText) singleMessage;
                        String textMsg = plainText.contentToString().replace("\\(:vaevent:\\)", "(:vaevent:)");
                        textMsg = deVanillaCode(rep, textMsg);
                        messages.add(textMsg);
                    } else {
                        messages.add(singleMessage);
                    }
                }
                message = messages.build();
            }
        }
        rep.setRep(message);
        return sendMessage(rep);
    }

    private static MessageReceipt<Contact> sendMessage(KeyRepEntity rep) {
        if (rep.getDelayMillis() > 0) {
            CompletableFuture<MessageReceipt<Contact>> delayed = Va.delayed(rep.getDelayMillis(), () -> getContactMessageReceipt(rep));
            try {
                return delayed.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        } else {
            return getContactMessageReceipt(rep);
        }
    }

    @Nullable
    private static MessageReceipt<Contact> getContactMessageReceipt(KeyRepEntity rep) {
        MessageReceipt<Contact> contactMessageReceipt = null;
        try {
            contactMessageReceipt = rep.getContact().sendMessage(rep.getRep());
            Va.addMsgSendCount();
            Va.getMessageCache().addMsg(rep.getContact(), contactMessageReceipt.getSource(), rep.getRep());
        } catch (Exception e) {
            Va.getLogger().error(e);
        }
        return contactMessageReceipt;
    }

    @NotNull
    private static String deVanillaCode(KeyRepEntity rep, String textMsg) {
        // 发送至指定好友特殊码
        if (textMsg.contains("[vacode:tofriend:")) {
            RegUtils regUtils = new RegUtils()
                    .appendIg(".*?")
                    .append("[vacode:tofriend:")
                    .groupIgByName("qq", "\\d{5,10}")
                    .appendIg("].*?");
            while (regUtils.matcher(textMsg).find()) {
                long qq = Long.parseLong(regUtils.getMatcher().group("qq"));
                textMsg = textMsg.replace("[vacode:tofriend:" + qq + "]", "");
                Contact friend;
                if (rep.getContact() instanceof Group) {
                    Group group = (Group) rep.getContact();
                    friend = group.get(qq);
                } else {
                    friend = rep.getContact().getBot().getFriend(qq);
                }
                rep.setContact(friend);
            }
        }

        // 发送至指定群聊特殊码
        if (textMsg.contains("[vacode:togroup:")) {
            RegUtils regUtils = new RegUtils()
                    .appendIg(".*?")
                    .append("[vacode:togroup:")
                    .groupIgByName("group", "\\d{5,10}")
                    .appendIg("].*?");
            while (regUtils.matcher(textMsg).find()) {
                long group = Long.parseLong(regUtils.getMatcher().group("group"));
                textMsg = textMsg.replace("[vacode:togroup:" + group + "]", "");
                rep.setContact(rep.getContact().getBot().getGroup(group));
            }
        }

        // 延时特殊码
        if (textMsg.contains("[vacode:delay:")) {
            RegUtils regUtils = new RegUtils()
                    .appendIg(".*?")
                    .append("[vacode:delay:")
                    .groupIgByName("time", "\\d{1,6}")
                    .appendIg("].*?");
            while (regUtils.matcher(textMsg).find()) {
                String millis = regUtils.getMatcher().group("time");
                textMsg = textMsg.replace("[vacode:delay:" + millis + "]", "");
                rep.setDelayMillis(Integer.parseInt(millis));
            }
        }

        // ChatGPT特殊码过滤key, 防止暴露
        if (textMsg.contains(":chatgpt:")) {
            RegUtils regUtils = new RegUtils()
                    .appendIg(".*?")
                    .append(":chatgpt:")
                    .groupIgByName("key", ".*?")
                    .appendIg("].*?");
            while (regUtils.matcher(textMsg).find()) {
                String key = regUtils.getMatcher().group("key");
                textMsg = textMsg.replace(":chatgpt:" + key + "]", ":chatgpt:***]");
            }
        }
        return textMsg;
    }

    // endregion sendMessage
}
