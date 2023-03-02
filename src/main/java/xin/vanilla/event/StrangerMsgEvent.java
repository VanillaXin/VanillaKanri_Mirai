package xin.vanilla.event;

import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.event.events.StrangerMessageEvent;

public class StrangerMsgEvent extends BaseMsgEvent {
    private final StrangerMessageEvent event;
    private final Stranger sender;

    public StrangerMsgEvent(StrangerMessageEvent event) {
        super(event.getMessage(), event.getBot(), event.getTime());
        this.event = event;
        this.sender = this.event.getSender();
        Va.getMessageCache().addMsg(this.sender, this.msg);
    }

    public void run() {
        // logger.info("陌生人: " + sender.getId() + " -> " + msg.serializeToMiraiCode());
    }
}
