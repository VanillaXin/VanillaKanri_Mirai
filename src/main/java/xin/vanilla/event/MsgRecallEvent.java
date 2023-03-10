package xin.vanilla.event;

import lombok.Getter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.UserOrBot;
import net.mamoe.mirai.event.events.MessageRecallEvent;
import net.mamoe.mirai.message.data.*;
import xin.vanilla.VanillaKanri;
import xin.vanilla.util.Api;
import xin.vanilla.util.DateUtils;
import xin.vanilla.util.SettingsUtils;
import xin.vanilla.util.StringUtils;

import java.util.Date;
import java.util.Set;

import static xin.vanilla.mapper.impl.MessageCacheImpl.MSG_TYPE_GROUP;

public class MsgRecallEvent extends BaseMsgEvent {
    private final VanillaKanri Va = VanillaKanri.INSTANCE;

    @Getter
    private final MessageRecallEvent event;
    @Getter
    private final UserOrBot sender;
    @Getter
    private Group group;
    @Getter
    private Member operator;

    public MsgRecallEvent(MessageRecallEvent event) {
        super(null, event.getBot(), event.getMessageTime());
        this.event = event;
        int[] ids = this.event.getMessageIds();
        int[] internalIds = this.event.getMessageInternalIds();
        this.sender = this.event.getAuthor();

        if (event instanceof MessageRecallEvent.GroupRecall) {
            MessageRecallEvent.GroupRecall groupRecall = (MessageRecallEvent.GroupRecall) event;
            this.group = groupRecall.getGroup();
            this.operator = groupRecall.getOperator();
            this.msg = Va.getMessageCache().getMsgChain(
                    StringUtils.toString(ids) + "|" + StringUtils.toString(internalIds)
                    , this.sender.getId()
                    , this.group.getId()
                    , MSG_TYPE_GROUP);
        } else {
            this.msg = Va.getMessageCache().getMsgChain(
                    StringUtils.toString(ids) + "|" + StringUtils.toString(internalIds)
                    , this.sender.getId()
                    , this.sender.getId()
                    , MSG_TYPE_GROUP);
        }
    }

    public void run() {
        if (sender.getId() == bot.getId()) return;
        Set<Long> groups = SettingsUtils.getBackGroup(group.getId());
        // 后台管理群撤回的消息不进行转发
        if (this.group != null && groups.contains(this.group.getId())) return;
        for (Long groupId : groups) {
            // 获取该机器人账号下的某个群对象
            Group backGroup = Bot.getInstance(bot.getId()).getGroup(groupId);
            assert backGroup != null;
            StringBuilder stringBuilder = new StringBuilder();
            if (this.group == null) {
                stringBuilder.append("好友: 『")
                        .append(this.sender.getNick())
                        .append("』(")
                        .append(this.sender.getId())
                        .append("):");
            } else {
                stringBuilder.append("群聊:『").append(this.group.getName()).append("』(").append(this.group.getId()).append(")\n")
                        .append("操作者:『").append(operator.getNick()).append("』")
                        .append("(").append(operator.getId()).append(")\n")
                        .append("发送者:『").append(sender.getNick()).append("』")
                        .append("(").append(sender.getId()).append(")\n")
                        .append("消息发送时间:『").append(DateUtils.toString(new Date(this.time * 1000L), "MM/dd HH:mm:ss")).append("』")
                        .append("(").append(this.time).append("):");
            }
            if (this.msg.get(FlashImage.Key) != null
                    || this.msg.get(PokeMessage.Key) != null
                    || this.msg.get(LightApp.Key) != null
                    || this.msg.get(MarketFace.Key) != null
                    || this.msg.get(ForwardMessage.Key) != null
                    || this.msg.get(MusicShare.Key) != null
                    || this.msg.get(Dice.Key) != null
                    || this.msg.get(RockPaperScissors.Key) != null
                    || this.msg.get(FileMessage.Key) != null
                    || this.msg.get(Audio.Key) != null) {
                Api.sendMessage(backGroup, stringBuilder.toString());
                Api.sendMessage(backGroup, this.msg);
            } else {
                Api.sendMessage(backGroup, new MessageChainBuilder()
                        .append(stringBuilder.append("\n"))
                        .append(this.msg)
                        .build()
                );
            }
        }
    }
}
