package xin.vanilla.event;

import net.mamoe.mirai.event.events.GroupTempMessageEvent;

public class GroupTempMsgEvent extends BaseMsgEvent {
    private final GroupTempMessageEvent event;

    public GroupTempMsgEvent(GroupTempMessageEvent event) {
        this.event = event;
    }

    public void run() {
        logger.info("临时: " + event.getGroup().getId() + ":" + event.getSender().getId() + " -> " + event.getMessage().contentToString());
    }
}
