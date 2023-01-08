package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import xin.vanilla.VanillaKanri;
import xin.vanilla.util.VanillaUtils;

public class InstructionMsgEvent {
    private static final int RETURN_CONTINUE = 1;
    private static final int RETURN_BREAK_TRUE = 2;
    private static final int RETURN_BREAK_FALSE = 3;

    private VanillaKanri Va = VanillaKanri.INSTANCE;
    private final MessageEvent event;
    private final MessageChain msg;
    private Group group;
    private final User sender;
    private final Bot bot;
    private final long time;

    public InstructionMsgEvent(MessageEvent event) {
        this.event = event;
        this.msg = this.event.getMessage();
        if (event instanceof GroupMessageEvent) this.group = ((GroupMessageEvent) this.event).getGroup();
        this.sender = this.event.getSender();
        this.bot = this.event.getBot();
        this.time = this.event.getTime();
    }

    /**
     * @return 是否不继续执行事件监听, true: 不执行, false: 执行
     */
    public boolean run() {
        int back;
        // 判断是否指令消息(仅判断顶级前缀)
        if (!VanillaUtils.isInstructionMsg(msg, false)) return false;

        // 判断发送者有无操作机器人的权限

        // 判断机器人是否群管
        if (!VanillaUtils.isGroupOwnerOrAdmin(group)) return false;

        // 解析执行群管指令
        back = kanriIns();
        if (back == RETURN_BREAK_TRUE) return true;
        else if (back == RETURN_BREAK_FALSE) return false;

        // 解析执行词库指令
        back = keyIns();
        if (back == RETURN_BREAK_TRUE) return true;
        else if (back == RETURN_BREAK_FALSE) return false;


        return false;
    }

    /**
     * 解析执行群管指令
     */
    public static int kanriIns() {
        // 判断是否群管指令

        return RETURN_CONTINUE;
    }

    /**
     * 解析执行词库指令
     */

    private static int keyIns() {
        // 判断是否词库管指令

        return RETURN_CONTINUE;
    }
}
