package xin.vanilla.event;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.common.annotation.Capability;
import xin.vanilla.entity.KeyRepEntity;
import xin.vanilla.entity.data.KeyData;
import xin.vanilla.util.Frame;
import xin.vanilla.util.VanillaUtils;

public class FriendMsgEvent extends BaseMsgEvent {
    private final FriendMessageEvent event;
    private final Friend friend;


    public FriendMsgEvent(FriendMessageEvent event) {
        super(event.getMessage(), event.getBot(), event.getTime());
        this.event = event;
        this.friend = this.event.getSender();
        Va.getMessageCache().addMsg(this.friend, this.msg);
    }

    /**
     * 解析关键词回复
     */
    @Capability
    private boolean keyRep() {
        // 关键词查询
        KeyData keyword = Va.getKeyword().getKeyword(VanillaUtils.messageToString(msg), bot.getId(), -2);
        if (keyword.getId() > 0) {
            MessageChain rep = RegExpConfig.VaCode.exeReply(keyword.getRepDecode(null, bot, friend, msg), msg, friend);
            KeyRepEntity keyRepEntity = new KeyRepEntity(friend);
            keyRepEntity.setMsg(VanillaUtils.messageToString(msg));
            keyRepEntity.setSenderId(friend.getId());
            keyRepEntity.setSenderName(friend.getNick());
            return null != Frame.sendMessage(keyRepEntity, rep);
        }
        return false;
    }
}
