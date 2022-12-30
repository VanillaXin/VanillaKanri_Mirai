package xin.vanilla.event;

import net.mamoe.mirai.event.events.GroupMessageEvent;

public class GroupMsgEvent extends BaseMsgEvent {
    private final GroupMessageEvent event;

    public GroupMsgEvent(GroupMessageEvent event) {
        this.event = event;
    }

    public void run() {
        logger.info("群聊: " + event.getGroup().getId() + ":" + event.getSender().getId() + " -> " + event.getMessage().contentToString());
    }
}
