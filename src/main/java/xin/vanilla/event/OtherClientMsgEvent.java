package xin.vanilla.event;

import net.mamoe.mirai.contact.OtherClient;
import net.mamoe.mirai.event.events.OtherClientMessageEvent;

public class OtherClientMsgEvent extends BaseMsgEvent {
    private final OtherClientMessageEvent event;
    private final OtherClient client;

    public OtherClientMsgEvent(OtherClientMessageEvent event) {
        super(event.getMessage(), event.getBot(), event.getTime());
        this.event = event;
        this.client = this.event.getClient();
    }

}
