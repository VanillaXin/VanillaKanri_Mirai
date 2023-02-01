package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.OtherClient;
import net.mamoe.mirai.event.events.OtherClientMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

public class OtherClientMsgEvent extends BaseMsgEvent {
    private final OtherClientMessageEvent event;
    private final MessageChain msg;
    private final OtherClient client;
    private final Bot bot;
    private final long time;

    public OtherClientMsgEvent(OtherClientMessageEvent event) {
        this.event = event;
        this.msg = this.event.getMessage();
        this.client = this.event.getClient();
        this.bot = this.event.getBot();
        this.time = this.event.getTime();
    }

    public void run() {
        logger.info("客户端: " + client.getId() + " -> " + msg.serializeToMiraiCode());
    }
}
