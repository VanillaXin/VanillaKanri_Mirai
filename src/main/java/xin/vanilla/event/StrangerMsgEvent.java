package xin.vanilla.event;

import net.mamoe.mirai.event.events.StrangerMessageEvent;

public class StrangerMsgEvent extends BaseMsgEvent {
    private final StrangerMessageEvent event;

    public StrangerMsgEvent(StrangerMessageEvent event) {
        this.event = event;
    }

    public void run() {
        logger.info("陌生人: " + event.getSender().getId() + " -> " + event.getMessage().contentToString());
    }
}
