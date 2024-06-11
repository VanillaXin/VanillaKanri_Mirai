package xin.vanilla.event;

import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.event.events.StrangerMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.common.annotation.Capability;
import xin.vanilla.entity.KeyRepEntity;
import xin.vanilla.entity.data.KeyData;
import xin.vanilla.util.Frame;
import xin.vanilla.util.VanillaUtils;

public class StrangerMsgEvent extends BaseMsgEvent {
    private final StrangerMessageEvent event;
    private final Stranger sender;

    public StrangerMsgEvent(StrangerMessageEvent event) {
        super(event.getMessage(), event.getBot(), event.getTime());
        this.event = event;
        this.sender = this.event.getSender();
        Va.getMessageCache().addMsg(this.sender, this.msg);
    }

    /**
     * 解析关键词回复
     */
    @Capability
    private boolean keyRep() {
        // 关键词查询
        KeyData keyword = Va.getKeyword().getKeyword(VanillaUtils.messageToString(msg), bot.getId(), -2);
        if (keyword.getId() > 0) {
            MessageChain rep = RegExpConfig.VaCode.exeReply(keyword.getRepDecode(null, bot, sender, msg), msg, sender);
            KeyRepEntity keyRepEntity = new KeyRepEntity(sender);
            keyRepEntity.setMsg(VanillaUtils.messageToString(msg));
            keyRepEntity.setSenderId(sender.getId());
            keyRepEntity.setSenderName(sender.getNick());
            return null != Frame.sendMessage(keyRepEntity, rep);
        }
        return false;
    }
}
