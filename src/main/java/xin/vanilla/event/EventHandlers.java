package xin.vanilla.event;

import cn.hutool.core.date.DateUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.*;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.common.annotation.KanriInsEvent;
import xin.vanilla.common.annotation.KeywordInsEvent;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.event.events.GroupMessageEvents;
import xin.vanilla.enumeration.PermissionLevel;
import xin.vanilla.util.Api;
import xin.vanilla.util.RegUtils;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.VanillaUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static xin.vanilla.enumeration.DataCacheKey.PLUGIN_BOT_ONLINE_TIME;

@SuppressWarnings("unused")
public class EventHandlers extends SimpleListenerHost {
    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        // 处理事件处理时抛出的异常
        Event event = ((ExceptionInEventHandlerException) exception).getEvent();

        exception = getBaseException(exception);

        if (event instanceof GroupMessageEvent) {
            GroupMessageEvent groupMessageEvent = (GroupMessageEvent) event;

            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            exception.printStackTrace(writer);
            StringBuffer buffer = stringWriter.getBuffer();

            Api.sendMessage(groupMessageEvent.getGroup(), StringUtils.getByLine(buffer.toString(), 1, 5, "... [num] more"));
        }

    }

    /**
     * 监听指令消息
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMessage(@NotNull MessageEvent event) {
        // 自增消息接收计数器
        VanillaKanri.INSTANCE.addMsgReceiveCount();

        InstructionMsgEvent insEvent = new InstructionMsgEvent(event);

        // 判断是否群管指令
        if (!insEvent.isKanriIns()) return;

        // 删除二级前缀
        if (!StringUtils.isNullOrEmpty(insEvent.getKanri().getPrefix())
                && insEvent.getIns().startsWith(insEvent.getKanri().getPrefix())) {
            if (insEvent.delPrefix(insEvent.getKanri().getPrefix())) return;
        } else if (!StringUtils.isNullOrEmpty(insEvent.getKeyword().getPrefix())
                && insEvent.getIns().startsWith(insEvent.getKeyword().getPrefix())) {
            if (insEvent.delPrefix(insEvent.getKeyword().getPrefix())) return;
        } else if (!StringUtils.isNullOrEmpty(insEvent.getTimed().getPrefix())
                && insEvent.getIns().startsWith(insEvent.getTimed().getPrefix())) {
            if (insEvent.delPrefix(insEvent.getTimed().getPrefix())) return;
        } else {
            if (insEvent.delPrefix("")) return;
        }

        // 三级前缀
        String prefix;
        int index = RegUtils.containsRegSeparator(insEvent.getIns());
        if (index >= 0) prefix = insEvent.getIns().substring(0, index);
        else prefix = insEvent.getIns();

        for (Method method : insEvent.getClass().getMethods()) {
            if (method.isAnnotationPresent(KanriInsEvent.class)) {
                KanriInsEvent annotation = method.getAnnotation(KanriInsEvent.class);

                // 判断三级前缀是否满足
                String prefixName = annotation.prefix();
                if (!StringUtils.isNullOrEmpty(prefixName)) {
                    boolean success = false;
                    for (Method kanriInsMethod : KanriInstructions.class.getMethods()) {
                        if (kanriInsMethod.getName().equalsIgnoreCase(StringUtils.METHOD_GET_PREFIX + prefixName)) {
                            try {
                                Object obj = kanriInsMethod.invoke(insEvent.getKanri());
                                if (prefixName.equalsIgnoreCase("kick")) {
                                    if (((String) obj).startsWith(prefix)) {
                                        success = true;
                                        break;
                                    }
                                } else {
                                    Set<String> values = new HashSet<>();
                                    if (obj instanceof Set) {
                                        values.addAll(((Set<?>) obj).stream().map(String::valueOf).collect(Collectors.toSet()));
                                    } else if (obj instanceof String) {
                                        values.add((String) obj);
                                    }
                                    if (values.contains(prefix)) {
                                        success = true;
                                        break;
                                    }
                                }
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    if (!success) continue;
                }

                // 判断发送者是否拥有权限
                PermissionLevel senderLevel = annotation.sender();
                if (VanillaUtils.hasNotPermissionAndMore(insEvent.getBot(), insEvent.getGroup(), insEvent.getSender().getId(), senderLevel)) {
                    continue;
                }

                // TODO 应该修改为判断所有待操作的群是否有权限
                // 判断机器人是否有对应权限
                if (insEvent.getGroup() != null) {
                    MemberPermission[] botLevel = annotation.bot();
                    MemberPermission botPermission = insEvent.getGroup().getBotPermission();
                    if (!Arrays.stream(botLevel).collect(Collectors.toList()).contains(botPermission)) {
                        continue;
                    }
                }

                // 解析正则表达式
                String regexpName = annotation.regexp();
                try {
                    Method regMethod = RegExpConfig.class.getMethod(regexpName, String.class);
                    RegUtils reg = (RegUtils) regMethod.invoke(null, prefix);
                    if (reg.matcher(insEvent.getIns()).find()) {
                        String operation;
                        long[] groups;
                        long[] qqs;
                        try {
                            operation = reg.getMatcher().group("operation");
                        } catch (IllegalStateException e) {
                            operation = "";
                        }
                        try {
                            String groupString = reg.getMatcher().group("group");
                            if (groupString.equals(AtAll.INSTANCE.toString()) || insEvent.getBase().getAtAll().contains(groupString)) {
                                groups = insEvent.getBase().getAtAllId().stream().limit(1).mapToLong(Long::parseLong).toArray();
                            } else {
                                groups = VanillaUtils.getGroupFromString(groupString);
                            }
                        } catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
                            groups = new long[]{};
                        }

                        // TODO 合理的做法应该修改为判断所有操作的群是否有对应权限
                        // 非超管及以上权限不允许同时操作多群
                        if (groups.length > 0) {
                            if (groups.length > 1 || groups[0] > 0) {
                                if (VanillaUtils.hasNotPermissionAndMore(insEvent.getBot(), null, insEvent.getSender().getId(), PermissionLevel.PERMISSION_LEVEL_SUPER_ADMIN)) {
                                    Api.sendMessage(insEvent.getGroup(), "权限不足");
                                    return;
                                }
                            }
                        }

                        try {
                            String qqString = reg.getMatcher().group("qq");
                            if (qqString.equals(AtAll.INSTANCE.toString()) || insEvent.getBase().getAtAll().contains(qqString)) {
                                qqs = insEvent.getBase().getAtAllId().stream().limit(1).mapToLong(Long::parseLong).toArray();
                            } else {
                                qqs = VanillaUtils.getQQFromString(qqString);
                            }
                        } catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
                            qqs = new long[]{};
                        }

                        int back = (int) method.invoke(insEvent, groups, qqs, operation);
                        if (back == InstructionMsgEvent.RETURN_BREAK_TRUE) {
                            event.intercept();
                            return;
                        } else if (back == InstructionMsgEvent.RETURN_BREAK_FALSE)
                            return;
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

            } else if (method.isAnnotationPresent(KeywordInsEvent.class)) {
                try {
                    int back = (int) method.invoke(insEvent, prefix);

                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            // TODO 解析定时任务指令
            // else if (method.isAnnotationPresent(TimedInsEvent.class)) {
            //
            // }

        }
    }

    /**
     * 监听群消息
     */
    @EventHandler
    public void onGroupMessage(@NotNull GroupMessageEvent event) {
        GroupMsgEvent groupMsgEvent = new GroupMsgEvent(new GroupMessageEvents(event));
        groupMsgEvent.encodeVaEvent();
        groupMsgEvent.run();
    }

    /**
     * 监听好友消息
     */
    @EventHandler
    public void onFriendMessage(@NotNull FriendMessageEvent event) {
        FriendMsgEvent friendMsgEvent = new FriendMsgEvent(event);
        friendMsgEvent.encodeVaEvent();
        friendMsgEvent.run();
    }

    /**
     * 监听群临时会话消息
     */
    @EventHandler
    public void onGroupTempMessage(@NotNull GroupTempMessageEvent event) {
        new GroupTempMsgEvent(event).run();
    }

    /**
     * 监听陌生人消息
     */
    @EventHandler
    public void onStrangerMessage(@NotNull StrangerMessageEvent event) {
        new StrangerMsgEvent(event).run();
    }

    /**
     * 监听其他客户端消息
     */
    @EventHandler
    public void onOtherClientMessage(@NotNull OtherClientMessageEvent event) {
        new OtherClientMsgEvent(event).run();
    }

    /**
     * 监听消息撤回事件
     */
    @EventHandler
    public void onMessageRecall(@NotNull MessageRecallEvent event) {
        new MsgRecallEvent(event).run();
    }

    /**
     * 监听机器人登录完成事件
     */
    @EventHandler
    public void onBotOnline(@NotNull BotOnlineEvent event) {
        long bot = event.getBot().getId();
        VanillaKanri.INSTANCE.getDataCache().put(PLUGIN_BOT_ONLINE_TIME.getKey(bot), System.currentTimeMillis());
    }

    /**
     * 监听戳一戳事件, 并转换为与语境匹配的消息事件
     */
    @EventHandler
    public void onTapEvent(@NotNull NudgeEvent event) {
        Contact subject = event.getSubject();
        UserOrBot sender = event.getFrom();
        UserOrBot target = event.getTarget();
        Bot bot = event.getBot();
        String action = event.getAction();
        String suffix = event.getSuffix();

        if (subject instanceof Group) {
            Group group = (Group) subject;
            // 获取发送者对象
            NormalMember normalMember = group.get(sender.getId());
            assert normalMember != null;

            // 构建群聊消息事件
            xin.vanilla.entity.event.GroupMessageEvent groupMessageEvent = new xin.vanilla.entity.event.GroupMessageEvent(
                    sender.getNick(),
                    normalMember.getPermission(),
                    normalMember,
                    buildTapMessage(sender, target, bot, action, suffix, MessageSourceKind.GROUP),
                    (int) DateUtil.currentSeconds()
            );

            // 触发群聊消息事件
            new GroupMsgEvent(new GroupMessageEvents(groupMessageEvent)).run();
        }
        // else if (subject instanceof Stranger) {
        //
        // }
        else if (subject instanceof Friend) {
            Friend friend = (Friend) subject;
            MessageChainBuilder singleMessages;
            FriendMessageEvent friendMessageEvent = new FriendMessageEvent(
                    friend,
                    buildTapMessage(sender, target, bot, action, suffix, MessageSourceKind.FRIEND),
                    (int) DateUtil.currentSeconds()

            );
            new FriendMsgEvent(friendMessageEvent).run();
        }
        // else if (subject instanceof Member) {
        //
        // }
    }

    /**
     * 获取最底层的异常
     */
    private Throwable getBaseException(@NotNull Throwable exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            cause = exception.getCause();
            if (cause != null) exception = cause;
        }
        return exception;
    }

    @NotNull
    private MessageChain buildTapMessage(UserOrBot sender, @NotNull UserOrBot target, @NotNull Bot bot, String action, String suffix, MessageSourceKind kind) {
        MessageChainBuilder singleMessages = new MessageChainBuilder().append("(:vaevent:)");
        if (target.getId() == bot.getId())
            singleMessages.append("<+tap+>");
        else
            singleMessages.append("<-tap->");
        singleMessages.append("(").append(String.valueOf(sender.getId())).append(":").append(String.valueOf(target.getId())).append(")");
        singleMessages.append("{").append(action).append("=").append(suffix).append("}");

        singleMessages.append(new MessageSourceBuilder()
                .sender(sender.getId())
                .target(target.getId())
                .id(0)
                .internalId(0)
                .build(bot.getId(), kind));
        return singleMessages.build();
    }
}
