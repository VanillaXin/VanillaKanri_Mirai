package xin.vanilla.util;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
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
import com.unfbx.chatgpt.interceptor.OpenAiResponseInterceptor;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.KeyRepEntity;
import xin.vanilla.entity.data.ChatApiReq;
import xin.vanilla.entity.data.ChatApiResp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

        try (HttpResponse response = HttpRequest.post("https://fanyi-api.baidu.com/api/trans/vip/translate").form(map).execute()) {
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
        try (HttpResponse response = HttpRequest.post(aiDrawUrl + "/run/predict/").header("Content-Type", "application/json").header("authorization", key).body(com.alibaba.fastjson2.JSONObject.toJSONString(jsonObject)).timeout(100000).execute()) {
            String body = response.body();
            JSONObject jsonObject1 = JSONUtil.parseObj(body);
            JSONArray jsonArray1 = JSONUtil.parseArray(jsonObject1.get("data"));
            JSONArray jsonArray2 = JSONUtil.parseArray(jsonArray1.get(0));
            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray2.get(0));

            return (String) jsonObject2.get("name");
        }
    }

    public static String vits_so_src(String text){

        String vitsUrl = Va.getGlobalConfig().getOther().getVitsUrl();
        Map<String, Object> map = new HashMap<>();
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(text);
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
        System.out.println(vitsUrl);
        try (HttpResponse response = HttpRequest.post(vitsUrl + "/run/predict/").header("Content-Type", "application/json").body(com.alibaba.fastjson2.JSONObject.toJSONString(map)).timeout(100000).execute()) {
            String body = response.body();
            JSONObject jsonObject1 = JSONUtil.parseObj(body);
            JSONArray jsonArray1 = JSONUtil.parseArray(jsonObject1.get("data"));
            JSONObject jsonObject = JSONUtil.parseObj(jsonArray1.get(1));
            String name = jsonObject.get("name").toString();

//            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray2.get(0));
           return name;

        }
    }

    public static String aiPictureV2(String prompt, String unPrompt) {


        String aiDrawUrl = Va.getGlobalConfig().getOther().getAiDrawUrl();

        String task = "task(" + IdUtil.randomUUID() + ")";
        JSONObject jsonObject = new JSONObject();
        jsonObject.set("fn_index", 196);

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
        jsonArray.add(1224);
        jsonArray.add(704);
        jsonArray.add(false);
        jsonArray.add(0.7);
        jsonArray.add(2);
        jsonArray.add("R-ESRGAN 4x+ Anime6B");
        jsonArray.add(0);
        jsonArray.add(0);
        jsonArray.add(0);
        jsonArray.add("");
        jsonArray.add("None");
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add("LoRA");
        jsonArray.add("None");
        jsonArray.add(1);
        jsonArray.add(1);
        jsonArray.add("LoRA");
        jsonArray.add("None");
        jsonArray.add(1);
        jsonArray.add(1);
        jsonArray.add("LoRA");
        jsonArray.add("None");
        jsonArray.add(1);
        jsonArray.add(1);
        jsonArray.add("LoRA");
        jsonArray.add("None");
        jsonArray.add(1);
        jsonArray.add(1);
        jsonArray.add("LoRA");
        jsonArray.add("None");
        jsonArray.add(1);
        jsonArray.add(1);
        jsonArray.add("Refresh models");
        jsonArray.add(false);
        jsonArray.add("none");
        jsonArray.add("None");
        jsonArray.add(1);
        jsonArray.add(null);
        jsonArray.add(false);
        jsonArray.add("Scale to Fit (Inner Fit)");
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(64);
        jsonArray.add(64);
        jsonArray.add(64);
        jsonArray.add(1);
        jsonArray.add(false);
        jsonArray.add(0.9);
        jsonArray.add(5);
        jsonArray.add("0.0001");
        jsonArray.add(false);
        jsonArray.add("None");
        jsonArray.add("");
        jsonArray.add(0.1);
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add(false);
        jsonArray.add("positive");
        jsonArray.add("comma");
        jsonArray.add(0);
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
        jsonArray.add(0);


        jsonObject.set("data", jsonArray);

        System.out.println(com.alibaba.fastjson2.JSONObject.toJSONString(jsonObject));

        try (HttpResponse response = HttpRequest.post(aiDrawUrl + "/run/predict/").header("Content-Type", "application/json").body(com.alibaba.fastjson2.JSONObject.toJSONString(jsonObject)).timeout(100000).execute()) {
            String body = response.body();
            JSONObject jsonObject1 = JSONUtil.parseObj(body);
            JSONArray jsonArray1 = JSONUtil.parseArray(jsonObject1.get("data"));
            JSONArray jsonArray2 = JSONUtil.parseArray(jsonArray1.get(0));
            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray2.get(0));

            return (String) jsonObject2.get("name");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        // return null;
    }

    public static String chatGPT(String nick, String msg) {
        return chatGPT(nick, Va.getGlobalConfig().getOther().getChatGPTKey(), msg);
    }

    // 使用白嫖接口调用chatGPT
    public static String chatGPT(String msg) {
        String chatGPTUrl = Va.getGlobalConfig().getOther().getChatGPTUrl();
        String context = Va.getGlobalConfig().getOther().getChatGPTContextAll();
        String parentMessageId = Va.getGlobalConfig().getOther().getParentMessageId();

        ChatApiReq chatApiReq = new ChatApiReq();
        Map m = new HashMap();
        m.put("parentMessageId", parentMessageId);
        chatApiReq.setPrompt(msg);
        chatApiReq.setOptions(m);
        chatApiReq.setSystemMessage(context);
        // Api.sendMessage(group,JSONUtil.toJsonStr(chatApiReq));
        String body = HttpRequest.post(chatGPTUrl + "/api/chat-process")
                .body(JSONUtil.toJsonStr(chatApiReq))
                .timeout(10000)
                .execute()
                .body();

        List<String> split = StrUtil.split(body, "\n");
        String join = "[" + StrUtil.join(",", split) + "]";
        List<ChatApiResp> chatApiResps = JSONUtil.toList(join, ChatApiResp.class);

        Va.setParentMessageId(chatApiResps.get(chatApiResps.size() - 1).getId());

        return chatApiResps.get(chatApiResps.size() - 1).getText();
    }


    public static String chatGPT(String nick, String key, String msg) {
        String chatGPTKey = key;
        if (StringUtils.isNullOrEmptyEx(chatGPTKey)) chatGPTKey = Va.getGlobalConfig().getOther().getChatGPTKey();
        if (StringUtils.isNullOrEmptyEx(chatGPTKey)) return "";
        String chatGPTUrl = Va.getGlobalConfig().getOther().getChatGPTUrl();
        if (StringUtils.isNullOrEmptyEx(chatGPTUrl)) return "";

        List<String> context = Va.getGlobalConfig().getOther().getChatGPTContext();

        String proxyHost = Va.getGlobalConfig().getOther().getSystemProxyHost();
        int proxyPort = Va.getGlobalConfig().getOther().getSystemProxyPort();

        try {
            // 日志
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new OpenAILogger());
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(httpLoggingInterceptor)// 自定义日志输出
                    .addInterceptor(new OpenAiResponseInterceptor())// 自定义返回值拦截
                    .connectTimeout(30, TimeUnit.SECONDS)// 自定义超时时间
                    .writeTimeout(30, TimeUnit.SECONDS)// 自定义超时时间
                    .readTimeout(60, TimeUnit.SECONDS);// 自定义超时时间
            if (!StringUtils.isNullOrEmptyEx(proxyHost) && proxyPort > 0)// 自定义代理
                okHttpClientBuilder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));

            OpenAiClient openAiClient = OpenAiClient.builder()
                    .okHttpClient(okHttpClientBuilder.build())
                    .apiKey(Collections.singletonList(chatGPTKey))
                    .apiHost(chatGPTUrl).build();

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
            }

            com.unfbx.chatgpt.entity.chat.Message userMessage = new com.unfbx.chatgpt.entity.chat.Message();
            userMessage.setRole(com.unfbx.chatgpt.entity.chat.Message.Role.USER.getName());
            userMessage.setContent(nick + ":" + msg);
            messages.add(userMessage);

            ChatCompletion chatCompletion = ChatCompletion.builder()
                    .temperature(0.8).presencePenalty(1).n(1).topP(1.0)
                    .model(ChatCompletion.Model.GPT_3_5_TURBO.getName())
                    .maxTokens(2000).messages(messages).build();

            ChatCompletionResponse chatCompletionResponse = openAiClient.chatCompletion(chatCompletion);
            return chatCompletionResponse.getChoices().get(0).getMessage().getContent();
        } catch (Exception ignored) {
        }
        return Va.getGlobalConfig().getOther().getChatGPTDefaultBack();
    }

    /**
     * 通过图片链接构建图片对象
     *
     * @param url 可以是http(s)://路径 也可以是file:///路径
     */
    @NotNull
    public static Image uploadImageByUrl(String url, Contact contact) {
        ExternalResource resource;
        if (url.startsWith("http")) {
            try (HttpResponse response = HttpRequest.get(url).setFollowRedirects(true).setMaxRedirectCount(3).execute()) {
                try (InputStream inputStream = response.bodyStream()) {
                    resource = ExternalResource.Companion.create(inputStream);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (url.startsWith("file:///")) {
            File file = new File(url.substring("file:///".length()));
            if (file.isFile()) {
                resource = ExternalResource.Companion.create(file);
            } else {
                List<Path> files;
                try (Stream<Path> paths = Files.walk(file.toPath())) {
                    files = paths.filter(Files::isRegularFile).collect(Collectors.toList());
                    Collections.shuffle(files);
                    resource = ExternalResource.Companion.create(files.get(0).toFile());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            throw new RuntimeException("图片路径不合法");
        }
        Image image = ExternalResource.uploadAsImage(resource, contact);
        try {
            resource.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    /**
     * 通过图片链接构建图片对象
     */
    @NotNull
    public static Image uploadImageByUrl(String url, Proxy proxy, Contact contact) {
        ExternalResource resource;
        try (HttpResponse response = HttpRequest.get(url).setFollowRedirects(true).setMaxRedirectCount(3).setProxy(proxy).execute()) {
            try (InputStream inputStream = response.bodyStream()) {
                resource = ExternalResource.Companion.create(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Image image = ExternalResource.uploadAsImage(resource, contact);
        try {
            resource.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    /**
     * 通过图片链接构建图片对象
     */
    @NotNull
    public static Image uploadImageByUrl(String url, String proxy, Contact contact) {
        ExternalResource resource;
        String[] split = proxy.split(":");
        try (HttpResponse response = HttpRequest.get(url).setFollowRedirects(true).setMaxRedirectCount(3).setHttpProxy(split[0], Integer.parseInt(split[1])).execute()) {
            try (InputStream inputStream = response.bodyStream()) {
                resource = ExternalResource.Companion.create(inputStream);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Image image = ExternalResource.uploadAsImage(resource, contact);
        try {
            resource.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

    // region sendMessage

    /**
     * 发送消息
     */
    public static MessageReceipt<Contact> sendMessage(Contact contact, String message) {
        if (contact == null) return null;
        if (StringUtils.isNullOrEmpty(message)) return null;
        // 反转义事件特殊码
        if (message.contains("\\(:vaevent:\\)") || message.contains("(:vaevent:)"))
            message = message.replaceAll("\\(:vaevent:\\)", "(:☢:)").replaceAll("\\\\(:vaevent:\\\\)", "(:vaevent:)");

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
            MessageChain messageChain = (MessageChain) message;
            String msgJson = MessageChain.serializeToJsonString(messageChain);
            if (message.contentToString().contains("(:vaevent:)") || message.contentToString().contains("\\(:vaevent:\\)") || message.contentToString().contains("[vacode:")) {
                String textMsg = msgJson.replaceAll("\\(:vaevent:\\)", "(:☢:)").replaceAll("\\\\(:vaevent:\\\\)", "(:vaevent:)");
                textMsg = deVanillaCode(rep, textMsg);
                message = MessageChain.deserializeFromJsonString(textMsg);
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
            RegUtils regUtils = new RegUtils().appendIg(".*?").append("[vacode:tofriend:").groupIgByName("qq", "\\d{5,10}").appendIg("].*?");
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
            RegUtils regUtils = new RegUtils().appendIg(".*?").append("[vacode:togroup:").groupIgByName("group", "\\d{5,10}").appendIg("].*?");
            while (regUtils.matcher(textMsg).find()) {
                long group = Long.parseLong(regUtils.getMatcher().group("group"));
                textMsg = textMsg.replace("[vacode:togroup:" + group + "]", "");
                rep.setContact(rep.getContact().getBot().getGroup(group));
            }
        }

        // 延时特殊码
        if (textMsg.contains("[vacode:delay:")) {
            RegUtils regUtils = new RegUtils().appendIg(".*?").append("[vacode:delay:").groupIgByName("time", "\\d{1,6}").appendIg("].*?");
            while (regUtils.matcher(textMsg).find()) {
                String millis = regUtils.getMatcher().group("time");
                textMsg = textMsg.replace("[vacode:delay:" + millis + "]", "");
                rep.setDelayMillis(Integer.parseInt(millis));
            }
        }

        // ChatGPT特殊码过滤key, 防止暴露
        if (textMsg.contains(":chatgpt:")) {
            RegUtils regUtils = new RegUtils().append(":chatgpt:").groupIgByName("key", "[\\w\\-]+?").append("]");
            while (regUtils.matcher(textMsg).find()) {
                String key = regUtils.getMatcher().group("key");
                textMsg = textMsg.replaceAll(regUtils.build(), ":chatgpt:***]");
            }
        }
        return textMsg;
    }

    // endregion sendMessage
}
