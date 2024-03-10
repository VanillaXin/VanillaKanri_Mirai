package xin.vanilla.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quartz.*;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.KeyRepEntity;
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
import java.util.Date;
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
        return Frame.buildPrivateChatContact(bot, qqNum, 0, true);
    }

    /**
     * 获取私聊对象
     *
     * @param fail 未获取到对象是否抛出异常
     */
    public static Contact buildPrivateChatContact(Bot bot, long qqNum, long groupNum, boolean fail) {
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

        // 定时任务特殊码(此处的)
        if (textMsg.contains("[vacode:timer:")) {
            RegUtils regUtils = new RegUtils().appendIg(".*?").append("[vacode:timer:")
                    .groupIgByName("time", "(?:\\d{1,6}(?:\\.\\d{1,4})?|(?:[\\d\\*\\-,\\?LW#/]+ ){4,6}(?:[\\d\\*\\-,\\?LW#/]+))")
                    .groupIgByName("unit", "(?:ms|s|m|h|d|MS|S|M|H|D|Ms|mS)?")
                    .appendIg("].*?");
            List<String> timeList = new ArrayList<>();
            while (regUtils.matcher(textMsg).find()) {
                String time = regUtils.getMatcher().group("time");
                String unit = regUtils.getMatcher().group("unit");
                textMsg = textMsg.replace("[vacode:timer:" + time + unit + "]", "");
                unit = StringUtils.isNullOrEmpty(unit) ? "ms" : unit;
                timeList.add(time + "=" + unit.toLowerCase());
            }
            for (String timeAndUnit : timeList) {
                String[] array = timeAndUnit.split("=");

                TimerData timer = new TimerData();
                timer.setId(NanoIdUtils.randomNanoId());
                timer.setBot(rep.getContact().getBot());
                timer.setBotNum(rep.getContact().getBot().getId());
                timer.setSender(Frame.buildPrivateChatContact(timer.getBot(), rep.getSenderId(), rep.getContact().getId(), false));
                timer.setSenderNum(rep.getSenderId());
                timer.setOnce(true);
                timer.setMsg(textMsg);
                timer.setGroupNum(rep.getContact().getBot().getGroup(rep.getContact().getId()) != null ? rep.getContact().getId() : 0);

                // 构建任务触发器
                Trigger trigger;
                // 判断是否为cron表达式
                if (CronExpression.isValidExpression(array[0])) {
                    timer.setCron(array[0]);
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(timer.getId(), timer.getGroupNum() + ".trigger")
                            .withSchedule(CronScheduleBuilder.cronSchedule(array[0]))
                            .build();
                    // 若为cron表达式则做持久化处理
                    if (Va.getTimerData().getTimer().containsKey(timer.getGroupNum())) {
                        Va.getTimerData().getTimer().get(timer.getGroupNum()).add(timer);
                    } else {
                        Va.getTimerData().getTimer().put(timer.getGroupNum(), new ArrayList<TimerData>() {{
                            add(timer);
                        }});
                    }
                } else {
                    timer.setCron(array[0] + array[1]);
                    float time = Float.parseFloat(array[0]);
                    Date startDate;
                    switch (array[1]) {
                        case "s":
                            startDate = DateUtils.addSecond(new Date(), time);
                            break;
                        case "m":
                            startDate = DateUtils.addMinute(new Date(), time);
                            break;
                        case "h":
                            startDate = DateUtils.addHour(new Date(), time);
                            break;
                        case "d":
                            startDate = DateUtils.addDay(new Date(), time);
                            break;
                        case "ms":
                        default:
                            startDate = DateUtils.addMilliSecond(new Date(), (int) time);
                    }
                    trigger = TriggerBuilder.newTrigger()
                            .withIdentity(timer.getId(), timer.getGroupNum() + ".trigger")
                            .withSchedule(SimpleScheduleBuilder.simpleSchedule())
                            .startAt(startDate)
                            .build();
                }

                // 构建任务, 装载任务数据
                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put("timer", timer);
                JobDetail jobDetail = JobBuilder.newJob(TimerMsgEvent.class)
                        .withIdentity(timer.getId(), timer.getGroupNum() + ".job")
                        .usingJobData(jobDataMap)
                        .build();

                try {
                    Va.getScheduler().scheduleJob(jobDetail, trigger);
                } catch (SchedulerException ignored) {
                }
            }
            textMsg = "";
        }
        return textMsg;
    }

    // endregion 发送消息

}
