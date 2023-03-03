package xin.vanilla.event;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.event.events.FriendMessageEvent;

public class FriendMsgEvent extends BaseMsgEvent {
    private final FriendMessageEvent event;
    private final Friend friend;


    public FriendMsgEvent(FriendMessageEvent event) {
        super(event.getMessage(), event.getBot(), event.getTime());
        this.event = event;
        this.friend = this.event.getSender();
        Va.getMessageCache().addMsg(this.friend, this.msg);
    }

    public void run() {
        // logger.info("好友: " + friend.getId() + " -> " + msg.serializeToMiraiCode());
    }
}
