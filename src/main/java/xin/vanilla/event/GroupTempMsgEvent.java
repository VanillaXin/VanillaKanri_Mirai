package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

public class GroupTempMsgEvent extends BaseMsgEvent {
    private final GroupTempMessageEvent event;
    private final MessageChain msg;
    private final Group group;
    private final Member sender;
    private final Bot bot;
    private final long time;
    private final boolean isBlock;

    public GroupTempMsgEvent(GroupTempMessageEvent event) {
        this.isBlock = new InstructionMsgEvent(event).run();
        this.event = event;
        this.msg = this.event.getMessage();
        this.group = this.event.getGroup();
        this.sender = this.event.getSender();
        this.bot = this.event.getBot();
        this.time = this.event.getTime();
        Va.getMessageCache().addMsg(this.sender, this.msg);
    }

    public void run() {
        if (isBlock) return;
        logger.info("临时: " + group.getId() + ":" + sender.getId() + " -> " + msg.serializeToMiraiCode());
    }
}
