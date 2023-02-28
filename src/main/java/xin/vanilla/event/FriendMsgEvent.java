package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

public class FriendMsgEvent extends BaseMsgEvent {
    private final FriendMessageEvent event;
    private final MessageChain msg;
    private final Friend friend;
    private final Bot bot;
    private final long time;

    public FriendMsgEvent(FriendMessageEvent event) {
        this.event = event;
        this.msg = this.event.getMessage();
        this.friend = this.event.getSender();
        this.bot = this.event.getBot();
        this.time = this.event.getTime();
        Va.getMessageCache().addMsg(this.friend, this.msg);
    }

    public void run() {
        // logger.info("好友: " + friend.getId() + " -> " + msg.serializeToMiraiCode());
    }
}
