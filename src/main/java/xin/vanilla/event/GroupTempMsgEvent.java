package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import xin.vanilla.VanillaKanri;

public class GroupTempMsgEvent extends BaseMsgEvent {
    private final GroupTempMessageEvent event;
    private final MessageChain msg;
    private final Group group;
    private final Member sender;
    private final Bot bot;
    private final long time;

    public GroupTempMsgEvent(GroupTempMessageEvent event) {
        this.event = event;
        this.msg = this.event.getMessage();
        this.group = this.event.getGroup();
        this.sender = this.event.getSender();
        this.bot = this.event.getBot();
        this.time = this.event.getTime();
        Va.messageCache.addMsg(this.sender, this.msg);
    }

    public void run() {
        logger.info("临时: " + group.getId() + ":" + sender.getId() + " -> " + msg.serializeToMiraiCode());
    }
}
