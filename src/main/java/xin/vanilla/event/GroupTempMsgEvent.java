package xin.vanilla.event;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.common.annotation.Capability;
import xin.vanilla.entity.KeyRepEntity;
import xin.vanilla.entity.data.KeyData;
import xin.vanilla.util.Frame;
import xin.vanilla.util.VanillaUtils;

public class GroupTempMsgEvent extends BaseMsgEvent {
    private final GroupTempMessageEvent event;
    private final Group group;
    private final Member sender;


    public GroupTempMsgEvent(GroupTempMessageEvent event) {
        super(event.getMessage(), event.getBot(), event.getTime());
        this.event = event;
        this.group = this.event.getGroup();
        this.sender = this.event.getSender();
        Va.getMessageCache().addMsg(this.sender, this.msg);
    }

    /**
     * 解析关键词回复
     */
    @Capability
    private boolean keyRep() {
        // 关键词查询
        KeyData keyword = Va.getKeyword().getKeyword(VanillaUtils.messageToString(msg), bot.getId(), -group.getId());
        if (keyword.getId() > 0) {
            MessageChain rep = RegExpConfig.VaCode.exeReply(keyword.getRepDecode(group, bot, sender, msg), msg, group);
            KeyRepEntity keyRepEntity = new KeyRepEntity(group);
            keyRepEntity.setMsg(VanillaUtils.messageToString(msg));
            keyRepEntity.setSenderId(sender.getId());
            keyRepEntity.setSenderName(sender.getNick());
            return null != Frame.sendMessage(keyRepEntity, rep);
        }
        return false;
    }
}
