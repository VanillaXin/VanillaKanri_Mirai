package xin.vanilla.event;

import net.mamoe.mirai.event.events.OtherClientMessageEvent;

public class OtherClientMsgEvent extends BaseMsgEvent {
    private final OtherClientMessageEvent event;

    public OtherClientMsgEvent(OtherClientMessageEvent event) {
        this.event = event;
    }

    public void run() {
        logger.info("客户端: " + event.getClient().getId() + " -> " + event.getMessage().contentToString());
    }
}
