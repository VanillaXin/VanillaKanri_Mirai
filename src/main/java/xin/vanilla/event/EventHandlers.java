package xin.vanilla.event;

import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.ExceptionInEventHandlerException;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.*;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.util.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

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

            groupMessageEvent.getGroup().sendMessage(StringUtils.getByLine(buffer.toString(), 1, 5, "... [num] more"));
        }

    }

    @EventHandler
    public void onGroupMessage(@NotNull GroupMessageEvent event) throws Exception {
        // 监听群消息
        new GroupMsgEvent(event).run();
    }

    @EventHandler
    public void onFriendMessage(@NotNull FriendMessageEvent event) throws Exception {
        // 监听好友消息
        new FriendMsgEvent(event).run();
    }

    @EventHandler
    public void onGroupTempMessage(@NotNull GroupTempMessageEvent event) throws Exception {
        // 监听群临时会话消息
        new GroupTempMsgEvent(event).run();
    }

    @EventHandler
    public void onStrangerMessage(@NotNull StrangerMessageEvent event) throws Exception {
        // 监听陌生人消息
        new StrangerMsgEvent(event).run();
    }

    @EventHandler
    public void onOtherClientMessage(@NotNull OtherClientMessageEvent event) throws Exception {
        // 监听其他客户端消息
        new OtherClientMsgEvent(event).run();
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
