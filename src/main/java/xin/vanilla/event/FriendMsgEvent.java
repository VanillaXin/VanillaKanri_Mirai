package xin.vanilla.event;

import net.mamoe.mirai.event.events.FriendMessageEvent;

public class FriendMsgEvent extends BaseMsgEvent {
    private final FriendMessageEvent event;

    public FriendMsgEvent(FriendMessageEvent event) {
        this.event = event;
    }

    public void run() {
        logger.info("好友: " + event.getFriend().getId() + " -> " + event.getMessage().contentToString());
    }
}
