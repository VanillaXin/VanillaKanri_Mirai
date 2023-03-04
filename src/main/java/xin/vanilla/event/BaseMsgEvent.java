package xin.vanilla.event;


import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.MiraiLogger;
import xin.vanilla.VanillaKanri;

public class BaseMsgEvent {
    protected static final VanillaKanri Va = VanillaKanri.INSTANCE;
    protected static final MiraiLogger logger = Va.getLogger();
    @Getter
    @Setter
    protected MessageChain msg;
    protected final Bot bot;
    protected final long time;

    BaseMsgEvent(MessageChain msg, Bot bot, long time) {
        this.msg = msg;
        this.bot = bot;
        this.time = time;
    }

    /**
     * 转义事件特殊码
     */
    protected void encodeVaEvent() {
        // 转义事件特殊码
        if (this.msg.contentToString().contains("(:vaevent:)")) {
            MessageChainBuilder messages = new MessageChainBuilder();
            for (SingleMessage singleMessage : msg) {
                if (singleMessage instanceof PlainText) {
                    PlainText plainText = (PlainText) singleMessage;
                    messages.add(plainText.contentToString().replace("(:vaevent:)", "\\(:vaevent:\\)"));
                } else {
                    messages.add(singleMessage);
                }
            }
            this.msg = messages.build();
        }
    }
}
