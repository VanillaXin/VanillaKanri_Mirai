package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.event.events.StrangerMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

public class StrangerMsgEvent extends BaseMsgEvent {
    private final StrangerMessageEvent event;
    private final MessageChain msg;
    private final Stranger sender;
    private final Bot bot;
    private final long time;

    public StrangerMsgEvent(StrangerMessageEvent event) {
        this.event = event;
        this.msg = this.event.getMessage();
        this.sender = this.event.getSender();
        this.bot = this.event.getBot();
        this.time = this.event.getTime();
    }

    public void run() {
        logger.info("陌生人: " + sender.getId() + " -> " + msg.contentToString());
    }
}
