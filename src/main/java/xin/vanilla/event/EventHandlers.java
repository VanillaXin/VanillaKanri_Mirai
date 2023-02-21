package xin.vanilla.event;

import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.event.*;
import net.mamoe.mirai.event.events.*;
import org.jetbrains.annotations.NotNull;
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
import java.util.HashSet;
import java.util.Set;

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
    public ListeningStatus onMessage(@NotNull MessageEvent event) {
        InstructionMsgEvent insEvent = new InstructionMsgEvent(event);

        // 未完成, 完成后再移除该判断语句
        if (false) {
            // 删除顶级前缀以及二级前缀
            if (!StringUtils.isNullOrEmpty(insEvent.getKanri().getPrefix())
                    && insEvent.getIns().startsWith(insEvent.getKanri().getPrefix())) {
                if (insEvent.delPrefix(insEvent.getKanri().getPrefix())) return ListeningStatus.STOPPED;
            } else if (!StringUtils.isNullOrEmpty(insEvent.getKeyword().getPrefix())
                    && insEvent.getIns().startsWith(insEvent.getKeyword().getPrefix())) {
                if (insEvent.delPrefix(insEvent.getKeyword().getPrefix())) return ListeningStatus.STOPPED;
            } else if (!StringUtils.isNullOrEmpty(insEvent.getTimed().getPrefix())
                    && insEvent.getIns().startsWith(insEvent.getTimed().getPrefix())) {
                if (insEvent.delPrefix(insEvent.getTimed().getPrefix())) return ListeningStatus.STOPPED;
            } else {
                if (insEvent.delPrefix("")) return ListeningStatus.STOPPED;
            }

            // 三级前缀
            String prefix;
            int index = RegUtils.containsRegSeparator(insEvent.getIns());
            if (index >= 0) prefix = insEvent.getIns().substring(0, index);
            else prefix = insEvent.getIns();

            for (Method method : insEvent.getClass().getMethods()) {
                if (method.isAnnotationPresent(KanriInsEvent.class)) {
                    System.out.println(method);
                    KanriInsEvent annotation = method.getAnnotation(KanriInsEvent.class);

                    // 判断三级前缀是否满足
                    String prefixName = annotation.prefix();
                    if (!StringUtils.isNullOrEmpty(prefixName)) {
                        boolean success = false;
                        for (Method method1 : KanriInstructions.class.getMethods()) {
                            if (method1.getName().equalsIgnoreCase(StringUtils.METHOD_GET_PREFIX + prefixName)) {
                                try {
                                    Object obj = method1.invoke(insEvent.getKanri());
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
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        if (!success) return ListeningStatus.STOPPED;
                    }

                    // 判断发送者是否拥有权限
                    PermissionLevel senderLevel = annotation.sender();
                    if (!VanillaUtils.hasPermissionOrMore(insEvent.getBot(), insEvent.getGroup(), insEvent.getSender().getId(), senderLevel))
                        return ListeningStatus.LISTENING;


                    // TODO 解析注解值
                    MemberPermission[] botLevel = annotation.bot();
                    String regexpName = annotation.regexp();

                } else if (method.isAnnotationPresent(KeywordInsEvent.class)) {

                } else if (method.isAnnotationPresent(TimedInsEvent.class)) {

                }
            }
        }


        if (insEvent.run()) {
            return ListeningStatus.STOPPED;
        } else {
            return ListeningStatus.LISTENING;
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
