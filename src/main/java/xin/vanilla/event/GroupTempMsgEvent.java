package xin.vanilla.event;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;

public class GroupTempMsgEvent extends BaseMsgEvent {
    private final GroupTempMessageEvent event;
    private final Group group;
    private final Member sender;


    public GroupTempMsgEvent(GroupTempMessageEvent event) {
        super(event.getMessage(), event.getBot(), event.getTime());
        this.event = event;
        this.group = this.event.getGroup();
        this.sender = this.event.getSender();
        Va.getMessageCache().addMsg(this.sender, this.msg);
    }

    public void run() {
        super.run();
    }
}
