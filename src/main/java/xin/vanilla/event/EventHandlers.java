package xin.vanilla.event;

import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.*;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.AtAll;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.common.annotation.KanriInsEvent;
import xin.vanilla.common.annotation.KeywordInsEvent;
import xin.vanilla.common.annotation.TimedInsEvent;
import xin.vanilla.entity.config.instruction.KanriInstructions;
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
                                        values.addAll((Set<String>) obj);
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
                if (!VanillaUtils.hasPermissionOrMore(insEvent.getBot(), insEvent.getGroup(), insEvent.getSender().getId(), senderLevel)) {
                    continue;
                }

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
                    RegUtils reg = (RegUtils) regMethod.invoke(new RegExpConfig(), prefix);
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
                        } catch (IllegalStateException | IllegalArgumentException e) {
                            groups = new long[]{};
                        }

                        try {
                            String qqString = reg.getMatcher().group("qq");
                            if (qqString.equals(AtAll.INSTANCE.toString()) || insEvent.getBase().getAtAll().contains(qqString)) {
                                qqs = insEvent.getBase().getAtAllId().stream().limit(1).mapToLong(Long::parseLong).toArray();
                            } else {
                                qqs = VanillaUtils.getQQFromString(qqString);
                            }
                        } catch (IllegalStateException | IllegalArgumentException e) {
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
                // TODO 解析关键词指令

            } else if (method.isAnnotationPresent(TimedInsEvent.class)) {
                // TODO 解析定时任务指令

            }

        }
    }

    /**
     * 监听群消息
     */
    @EventHandler
    public void onGroupMessage(@NotNull GroupMessageEvent event) {
        new GroupMsgEvent(event).run();
    }

    /**
     * 监听好友消息
     */
    @EventHandler
    public void onFriendMessage(@NotNull FriendMessageEvent event) {
        new FriendMsgEvent(event).run();
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
     * 获取最底层的异常
     */
    private Throwable getBaseException(Throwable exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            cause = exception.getCause();
            if (cause != null) exception = cause;
        }
        return exception;
    }

}
