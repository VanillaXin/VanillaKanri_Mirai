package xin.vanilla.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.KeyRepEntity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 框架部分接口
 */
public class Frame {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;

    // region 构建图片对象

    /**
     * 通过图片链接构建图片对象
     *
     * @param url 可以是http(s)://路径 也可以是file:///路径
     */
    @NotNull
    public static Image buildImageByUrl(String url, Contact contact) {
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
    public static Image buildImageByUrl(String url, Proxy proxy, Contact contact) {
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
    public static Image buildImageByUrl(String url, String proxy, Contact contact) {
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

    // endregion 构建图片对象


    // region 构建消息接收对象

    /**
     * 获取私聊对象
     */
    @NotNull
    public static Contact buildPrivateChatContact(Bot bot, long qqNum) {
        return Frame.buildPrivateChatContact(bot, qqNum, 0);
    }

    /**
     * 获取私聊对象
     */
    @NotNull
    public static Contact buildPrivateChatContact(Bot bot, long qqNum, long groupNum) {
        Contact contact = null;
        if (groupNum > 0) {
            Group group = bot.getGroup(groupNum);
            if (group != null) {
                contact = group.get(qqNum);
            }
        }
        if (contact == null) {
            contact = bot.getFriend(qqNum);
        }
        if (contact == null) {
            contact = bot.getStrangerOrFail(qqNum);
        }
        return contact;
    }

    // endregion 构建消息接收对象


    // region 发送消息

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

    // endregion 发送消息

}
