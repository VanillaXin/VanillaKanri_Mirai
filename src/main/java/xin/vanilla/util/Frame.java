package xin.vanilla.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.*;
import xin.vanilla.VanillaKanri;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.entity.KeyRepEntity;
import xin.vanilla.entity.TriggerEntity;
import xin.vanilla.entity.data.TimerData;
import xin.vanilla.event.TimerMsgEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xin.vanilla.util.RegUtils.REG_SEPARATOR;

/**
 * 框架部分接口
 */
public class Frame {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;

    // region 构建图片对象

    /**
     * 通过图片链接构建图片对象
     *
     * @param url 可以是http(s)://路径 也可以是file:///路径 或者指定JsonPath的json字符串
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
        return Frame.buildPrivateChatContact(bot, qqNum, 0, true);
    }

    /**
     * 获取私聊对象
     *
     * @param fail 未获取到对象是否抛出异常
     */
    public static User buildPrivateChatContact(Bot bot, long qqNum, long groupNum, boolean fail) {
        User contact = null;
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
            if (fail)
                contact = bot.getStrangerOrFail(qqNum);
            else
                contact = bot.getStranger(qqNum);
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
                long group = 0;
                if (rep.getContact() instanceof Group) {
                    group = rep.getContact().getId();
                }
                rep.setContact(Frame.buildPrivateChatContact(rep.getContact().getBot(), qq, group, true));
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

        // 定时任务特殊码
        if (textMsg.contains("[vacode:timer:")) {

            // 解析及即时发送消息
            {
                RegUtils sendNow = new RegUtils().appendIg(".*?").append("[vacode:sendnow:")
                        .groupIgByName("msg", ".*")
                        .appendIg(":vacode:sendnow]").end();
                while (sendNow.matcher(textMsg).find()) {
                    String msg = sendNow.getMatcher().group("msg");
                    textMsg = textMsg.replace("[vacode:sendnow:" + msg + ":vacode:sendnow]", "");
                    MessageChain singleMessages = MessageChain.deserializeFromJsonString(msg);
                    KeyRepEntity keyRep = new KeyRepEntity(rep.getContact());
                    keyRep.setRep(singleMessages);
                    Frame.sendMessage(keyRep);
                }
            }

            RegUtils regUtils = new RegUtils().appendIg(".*?").append("[vacode:timer:")
                    .groupIgByName("exp"
                            , "(?:\\d{1,6}(?:\\.\\d{1,4})?(?:ms|s|m|h|d|MS|S|M|H|D|Ms|mS)?"
                            , "(?:[\\d\\*\\-,\\?LW#/]+" + REG_SEPARATOR + "){4,6}(?:[\\d\\*\\-,\\?LW#/]+))"
                            , RegExpConfig.DATE_TIME_CODE)
                    .appendIg("].*?")
                    .end();
            List<String> expList = new ArrayList<>();
            while (regUtils.matcher(textMsg).find()) {
                String exp = regUtils.getMatcher().group("exp");
                textMsg = textMsg.replace("[vacode:timer:" + exp + "]", "");
                expList.add(exp);
            }

            Bot bot = rep.getContact().getBot();
            Contact sender = Frame.buildPrivateChatContact(bot, rep.getSenderId(), rep.getContact().getId(), false);
            long groupId = rep.getContact().getBot().getGroup(rep.getContact().getId()) != null ? rep.getContact().getId() : 0;
            int level = VanillaUtils.getPermissionLevel(bot, groupId, sender.getId());

            for (String exp : expList) {
                boolean validExpression = CronExpression.isValidExpression(exp);

                TimerData timer = new TimerData();
                timer.setId(StringUtils.randString());
                timer.setBot(bot);
                timer.setBotNum(bot.getId());
                timer.setSender(Frame.buildPrivateChatContact(bot, sender.getId(), groupId, false));
                timer.setSenderNum(sender.getId());
                timer.setOnce(!(validExpression && level > 0));
                timer.setMsg(textMsg);
                timer.setGroupNum(groupId);
                timer.setCron(exp);

                // 构建任务触发器
                TriggerEntity triggerEntity = VanillaUtils.buildTriggerFromExp(new TriggerKey(timer.getId(), timer.getGroupNum() + ".trigger"), exp, level > 0);
                boolean tf = triggerEntity.getTrigger() != null;
                // 构建任务, 装载任务数据
                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put("timer", timer);
                JobDetail jobDetail = JobBuilder.newJob(TimerMsgEvent.class)
                        .withIdentity(timer.getId(), timer.getGroupNum() + ".job")
                        .usingJobData(jobDataMap)
                        .build();
                if (tf) {
                    try {
                        Va.getScheduler().scheduleJob(jobDetail, triggerEntity.getTrigger());
                        // 记录在案, 方便删除
                        if (Va.getTimerData().getTimer().containsKey(timer.getGroupNum())) {
                            Va.getTimerData().getTimer().get(timer.getGroupNum()).add(timer);
                        } else {
                            Va.getTimerData().getTimer().put(timer.getGroupNum(), new ArrayList<TimerData>() {{
                                add(timer);
                            }});
                        }
                    } catch (SchedulerException e) {
                        Va.getLogger().error(e);
                    }
                }
            }
            textMsg = "";
        }
        return textMsg;
    }

    // endregion 发送消息

}
