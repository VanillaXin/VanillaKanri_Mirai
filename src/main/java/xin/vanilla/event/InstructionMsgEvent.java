package xin.vanilla.event;

import lombok.Getter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;
import xin.vanilla.common.annotation.KanriInsEvent;
import xin.vanilla.entity.config.instruction.BaseInstructions;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.entity.config.instruction.TimedTaskInstructions;
import xin.vanilla.entity.data.MsgCache;
import xin.vanilla.util.Api;
import xin.vanilla.util.RegUtils;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.VanillaUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static xin.vanilla.enumeration.PermissionLevel.PERMISSION_LEVEL_DEPUTY_ADMIN;
import static xin.vanilla.enumeration.PermissionLevel.PERMISSION_LEVEL_SUPER_ADMIN;
import static xin.vanilla.mapper.impl.MessageCacheImpl.MSG_TYPE_GROUP;

@SuppressWarnings("unused")
public class InstructionMsgEvent {
    /**
     * 跳过本次消息事件
     * <p>
     * 并交给插件内其他事件解析器处理
     */
    public static final int RETURN_CONTINUE = 1;
    /**
     * 结束本次消息事件
     */
    public static final int RETURN_BREAK_TRUE = 2;
    /**
     * 跳过本次消息事件
     * <p>
     * 并交给其他插件处理
     */
    public static final int RETURN_BREAK_FALSE = 3;

    private final VanillaKanri Va = VanillaKanri.INSTANCE;

    @Getter
    private final MessageEvent event;
    @Getter
    private final MessageChain msg;
    @Getter
    private Group group;
    @Getter
    private final User sender;
    @Getter
    private final Bot bot;
    @Getter
    private final long time;

    @Getter
    private String ins;
    @Getter
    private boolean kanriIns = false;

    @Getter
    private final KanriInstructions kanri = Va.getGlobalConfig().getInstructions().getKanri();
    @Getter
    private final KeywordInstructions keyword = Va.getGlobalConfig().getInstructions().getKeyword();
    @Getter
    private final TimedTaskInstructions timed = Va.getGlobalConfig().getInstructions().getTimed();
    @Getter
    private final BaseInstructions base = Va.getGlobalConfig().getInstructions().getBase();

    public InstructionMsgEvent(MessageEvent event) {
        this.event = event;
        this.msg = this.event.getMessage();
        if (event instanceof GroupMessageEvent) this.group = ((GroupMessageEvent) this.event).getGroup();
        if (event instanceof GroupTempMessageEvent) this.group = ((GroupTempMessageEvent) this.event).getGroup();
        this.sender = this.event.getSender();
        this.bot = this.event.getBot();
        this.time = this.event.getTime();
        this.ins = delTopPrefix(VanillaUtils.messageToString(this.msg));
    }


    // region 群管指令

    /**
     * 戳一戳
     */
    @KanriInsEvent(prefix = "tap"
            , regexp = "tapRegExp")
    public int tap(@NotNull long[] groups, long[] qqs, String num) {
        if (groups.length == 0) groups = new long[]{-1};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            int i;
            try {
                i = Integer.parseInt(num);
            } catch (NumberFormatException ignored) {
                i = 1;
            }
            // 操作频率限制
            String tapTimeKey = StringUtils.getTapTimeKey(thatGroup.getId(), sender.getId());
            long last = VanillaUtils.getDataCacheAsLong(tapTimeKey);
            if (last > new Date().getTime()) {
                Api.sendMessage(thatGroup, "操作太快啦，休息一下吧。");
                return RETURN_CONTINUE;
            } else {
                VanillaUtils.setDateCache(tapTimeKey, i * 10L * 1000L + new Date().getTime());
            }

            for (int j = 0; j < i; j++) {
                Va.delayed(j * 5 * 1000L, () -> {
                    for (long qq : qqs) {
                        NormalMember normalMember = thatGroup.get(qq);
                        if (normalMember != null) {
                            normalMember.nudge().sendTo(thatGroup);
                        }
                    }
                });
            }
            if (qqs.length == 0)
                Api.sendMessage(thatGroup, "待操作对象为空");
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 修改群名片
     */
    @KanriInsEvent(prefix = "card"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER}
            , regexp = "cardRegExp")
    public int card(@NotNull long[] groups, @NotNull long[] qqs, String text) {
        if (groups.length == 0) groups = new long[]{-1};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            StringBuilder rep = new StringBuilder();
            for (long qq : qqs) {
                rep.append(',');
                NormalMember normalMember = thatGroup.get(qq);
                if (normalMember != null) {
                    normalMember.setNameCard(text);
                    rep.append(qq);
                }
            }
            if (!StringUtils.isNullOrEmpty(rep.toString())) {
                rep.delete(0, 1);
                if (rep.toString().equals("")) {
                    Api.sendMessage(thatGroup, "操作失败");
                } else {
                    if (StringUtils.isNullOrEmpty(text))
                        Api.sendMessage(thatGroup, "已清除 " + rep + " 的名片");
                    else
                        Api.sendMessage(thatGroup, "已将 " + rep + " 的名片修改为:\n" + text);
                }
            } else {
                Api.sendMessage(thatGroup, "待操作对象为空");
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 设置群专属头衔
     */
    @KanriInsEvent(prefix = "tag"
            , bot = {MemberPermission.OWNER}
            , regexp = "tagRegExp")
    public int tag(@NotNull long[] groups, long[] qqs, String tag) {
        if (groups.length == 0) groups = new long[]{-1};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            if (StringUtils.isNullOrEmpty(tag)) {
                NormalMember normalMember = thatGroup.get(sender.getId());
                if (normalMember != null) {
                    normalMember.setSpecialTitle(tag);
                    Api.sendMessage(thatGroup, "已将阁下的头衔修改为:\n" + tag);
                }
            } else {
                StringBuilder successMsg = new StringBuilder();
                for (long qq : qqs) {
                    successMsg.append(',');
                    NormalMember normalMember = thatGroup.get(qq);
                    if (normalMember != null) {
                        normalMember.setSpecialTitle(tag);
                        successMsg.append(qq);
                    }
                }
                if (!StringUtils.isNullOrEmpty(successMsg.toString())) {
                    successMsg.delete(0, 1);
                    if (successMsg.toString().equals("")) {
                        Api.sendMessage(thatGroup, "操作失败");
                    } else {
                        Api.sendMessage(thatGroup, "已修改 " + successMsg + " 的头衔为:\n" + tag);
                    }
                } else {
                    Api.sendMessage(thatGroup, "待操作对象为空");
                }
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 设置群精华消息
     */
    @KanriInsEvent(prefix = "essence"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER}
            , regexp = "essenceRegExp")
    public int essence(@NotNull long[] groups, long[] qqs, String text) {
        if (groups.length == 0) groups = new long[]{-1};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            if (kanri.getEssence().contains(ins) || base.getAdd().contains(text)) {
                QuoteReply quoteReply = msg.get(QuoteReply.Key);
                if (quoteReply != null) {
                    if (thatGroup.setEssenceMessage(quoteReply.getSource())) {
                        Api.sendMessage(thatGroup, new MessageChainBuilder().append(quoteReply).append("已将该消息设为精华").build());
                    } else {
                        Api.sendMessage(thatGroup, "精华消息设置失败");
                    }
                }
            }
            // else if (base.getDelete().contains(text)) {
            // 未发现取消精华消息接口
            // QuoteReply quoteReply = msg.get(QuoteReply.Key);
            // if (quoteReply != null) {
            // }
            // }
            else {
                OnlineMessageSource.Outgoing source = Api.sendMessage(thatGroup, text).getSource();
                if (!thatGroup.setEssenceMessage(source)) {
                    Api.sendMessage(thatGroup, "精华消息设置失败");
                }
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 解除禁言
     */
    @KanriInsEvent(prefix = "loud"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER}
            , regexp = "loudRegExp")
    public int loud(@NotNull long[] groups, @NotNull long[] qqs, String text) {
        if (groups.length == 0) groups = new long[]{-1};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            StringBuilder rep = new StringBuilder();
            if (qqs.length == 1 && base.getAtAllId().contains(String.valueOf(qqs[0]))) {
                if (thatGroup.getSettings().isMuteAll()) {
                    thatGroup.getSettings().setMuteAll(false);
                    Api.sendMessage(thatGroup, "已关闭全体禁言");
                }
            } else {
                for (long qq : qqs) {
                    rep.append(',');
                    NormalMember normalMember = thatGroup.get(qq);
                    if (normalMember != null) {
                        if (normalMember.isMuted()) {
                            normalMember.unmute();
                            rep.append(qq);
                        }
                    }
                }
                if (!StringUtils.isNullOrEmpty(rep.toString())) {
                    if (rep.toString().equals(",")) {
                        Api.sendMessage(thatGroup, "操作失败");
                    } else {
                        Api.sendMessage(thatGroup, "已解除 " + rep.delete(0, 1) + " 的禁言");
                    }
                } else {
                    Api.sendMessage(thatGroup, "待操作对象为空或未被禁言");
                }
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 禁言
     */
    @KanriInsEvent(prefix = "mute"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER}
            , regexp = "muteRegExp")
    public int mute(@NotNull long[] groups, @NotNull long[] qqs, String time) {
        if (groups.length == 0) groups = new long[]{-1};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            if (qqs.length == 1 && base.getAtAllId().contains(String.valueOf(qqs[0]))) {
                if (!thatGroup.getSettings().isMuteAll()) {
                    thatGroup.getSettings().setMuteAll(true);
                    Api.sendMessage(thatGroup, "已开启全体禁言");
                    if (!StringUtils.isNullOrEmpty(time)) {
                        Va.delayed(Math.round(Float.parseFloat(time)) * 60L * 1000L, () -> {
                            if (thatGroup.getSettings().isMuteAll()) {
                                thatGroup.getSettings().setMuteAll(false);
                            }
                        });
                        Api.sendMessage(thatGroup, "并将在 " + time + " 分钟后关闭全体禁言");
                    }
                }
            } else if (!StringUtils.isNullOrEmpty(time)) {
                NormalMember senderMember = thatGroup.get(sender.getId());
                StringBuilder successMsg = new StringBuilder();
                for (long qq : qqs) {
                    successMsg.append(',');
                    NormalMember normalMember = thatGroup.get(qq);
                    if (normalMember != null && senderMember != null) {
                        // 比较操作者与被操作者权限
                        if (VanillaUtils.equalsPermission(bot, thatGroup, senderMember.getId(), normalMember.getId())) {
                            try {
                                normalMember.mute(Math.round(Float.parseFloat(time) * 60));
                                successMsg.append(qq);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
                if (!StringUtils.isNullOrEmpty(successMsg.toString())) {
                    successMsg.delete(0, 1);
                    if (successMsg.toString().equals("")) {
                        Api.sendMessage(thatGroup, "权限不足");
                    } else {
                        Api.sendMessage(thatGroup, "已禁言 " + successMsg + " " + time + "分钟");
                    }
                } else {
                    Api.sendMessage(thatGroup, "待操作对象为空");
                }
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 撤回消息
     */
    @KanriInsEvent(prefix = "withdraw"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , regexp = "withdrawRegExp")
    public int withdraw(long[] groups, long[] qqs, String text) {
        MessageSource messageSource = msg.get(MessageSource.Key);
        if (messageSource != null) try {
            MessageSource.recall(messageSource);
        } catch (IllegalStateException ignored) {
            Va.getLogger().info("无权撤回或已被撤回");
        }
        QuoteReply quoteReply = msg.get(QuoteReply.Key);
        int[] sourceIds;
        if (quoteReply != null) {
            sourceIds = quoteReply.getSource().getIds();
            if (kanri.getWithdraw().contains(ins)) {
                try {
                    MessageSource.recall(quoteReply.getSource());
                } catch (IllegalStateException ignored) {
                    Va.getLogger().info("无权撤回或已被撤回");
                }
            }
        } else {
            assert messageSource != null;
            sourceIds = messageSource.getIds();
        }
        int sourceId = sourceIds[sourceIds.length - 1];
        if (!StringUtils.isNullOrEmpty(text)) {
            try {
                int id = sourceId - Integer.parseInt(text);
                recall(id);
            } catch (NumberFormatException ignored) {
                if (text.contains("-") || text.contains("~")) {
                    int[] ids = Arrays.stream(text.replace("-", "~")
                                    .split("~"))
                            .mapToInt(s -> sourceId - Integer.parseInt(s))
                            .toArray();
                    if (ids.length == 2) {
                        for (int id = ids[0]; id >= ids[1]; id--) {
                            recall(id);
                        }
                    }
                } else if (RegUtils.containsRegSeparator(text) > 0) {
                    int[] ids = Arrays.stream(text.replaceAll("\\s", " ")
                                    .split(" "))
                            .mapToInt(s -> sourceId - Integer.parseInt(s))
                            .toArray();
                    for (int id : ids) {
                        recall(id);
                    }
                } else {
                    Api.sendMessage(group, "表达式『" + text + "』有误");
                }
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 增删群管理员
     */
    @KanriInsEvent(prefix = "admin"
            , sender = PERMISSION_LEVEL_SUPER_ADMIN
            , bot = MemberPermission.OWNER
            , regexp = "adminRegExp")
    public int admin(@NotNull long[] groups, @NotNull long[] qqs, String text) {
        if (groups.length == 0) groups = new long[]{-1};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            boolean operation = base.getAdd().contains(text);
            StringBuilder rep = new StringBuilder();
            for (long qq : qqs) {
                rep.append(',');
                NormalMember normalMember = thatGroup.get(qq);
                if (normalMember != null) {
                    normalMember.modifyAdmin(operation);
                    rep.append(qq);
                }
            }

            if (!StringUtils.isNullOrEmpty(rep.toString())) {
                rep.delete(0, 1);
                if (operation)
                    Api.sendMessage(thatGroup, "已将 " + rep + " 添加为管理员");
                else Api.sendMessage(thatGroup, "已取消 " + rep + " 的管理员");
            } else {
                Api.sendMessage(thatGroup, "待操作对象为空");
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 踢出群成员
     */
    @KanriInsEvent(prefix = "kick"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER}
            , regexp = "kickRegExp")
    public int kick(@NotNull long[] groups, @NotNull long[] qqs, String text) {
        if (groups.length == 0) groups = new long[]{-1};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            NormalMember senderMember = thatGroup.get(sender.getId());
            StringBuilder successMsg = new StringBuilder();
            for (long qq : qqs) {
                successMsg.append(',');
                NormalMember normalMember = thatGroup.get(qq);
                if (normalMember != null && senderMember != null) {
                    // 比较操作者与被操作者权限
                    if (VanillaUtils.equalsPermission(bot, thatGroup, senderMember.getId(), normalMember.getId())) {
                        boolean bool = StringUtils.stringToBoolean(text);
                        normalMember.kick("被" + sender.getId() + "通过群管指令踢出", bool);
                        successMsg.append(qq);
                    }
                }
            }
            if (!StringUtils.isNullOrEmpty(successMsg.toString())) {
                successMsg.delete(0, 1);
                if (successMsg.toString().equals("")) {
                    Api.sendMessage(thatGroup, "权限不足");
                } else {
                    Api.sendMessage(thatGroup, "已将 " + successMsg + " 移除群聊");
                }
            } else {
                Api.sendMessage(thatGroup, "待操作对象为空");
            }
        }
        return RETURN_BREAK_TRUE;
    }

    // endregion


    // region 关键词指令
    // TODO 定义关键词指令


    // endregion


    // region 定时任务指令
    // TODO 定义定时任务指令


    // endregion


    // region 私有方法

    /**
     * 删除顶级指令前缀
     */
    private String delTopPrefix(String text) {
        String prefix = Va.getGlobalConfig().getInstructions().getPrefix();
        if (StringUtils.isNullOrEmpty(prefix)) {
            this.kanriIns = true;
        } else if (text.startsWith(prefix)) {
            this.kanriIns = true;
            return text.substring(prefix.length()).trim();
        }
        return msg.serializeToMiraiCode().trim();
    }

    /**
     * 判断是否包含前缀prefix
     * <p>
     * 并将删除前缀prefix后的结果保存至ins
     *
     * @return 是否失败
     */
    public boolean delPrefix(String prefix) {
        String s = this.ins;
        if (StringUtils.isNullOrEmpty(prefix)) {
            setIns(s);
            return StringUtils.isNullOrEmpty(this.ins);
        } else if (s.startsWith(prefix)) {
            setIns(s.substring(prefix.length()).trim());
            return StringUtils.isNullOrEmpty(this.ins);
        }
        return true;
    }

    private <T> String toRegString(Set<T> set) {
        return toRegString(Arrays.asList(set.toArray()));
    }

    private <T> String toRegString(List<T> list) {
        if (list == null)
            return "";
        int iMax = list.size() - 1;
        if (iMax == -1) return "";

        StringBuilder b = new StringBuilder();
        b.append("(");
        int i = 0;
        for (T t : list) {
            b.append(StringUtils.escapeExprSpecialWord(t.toString()));
            if (i == iMax)
                return b.append(')').toString();
            b.append("|");
            i++;
        }
        return b.toString();
    }

    private void setIns(String s) {
        //[ \f\n\r\t\v]
        this.ins = s.replace("\\f", "\f")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    /**
     * 根据id撤回群消息
     */
    private void recall(int id) {
        MsgCache msgCache = Va.getMessageCache().getMsgCache(id + "|", group.getId(), MSG_TYPE_GROUP);
        if (msgCache != null) {
            try {
                MessageSource.recall(new MessageSourceBuilder()
                        .sender(msgCache.getSender())
                        .target(msgCache.getTarget())
                        .id(msgCache.getIds())
                        .internalId(msgCache.getInternalIds())
                        .build(bot.getId(), MessageSourceKind.GROUP));
            } catch (IllegalStateException e) {
                Va.getLogger().info("无权撤回或已被撤回");
            }
        }
    }

    // endregion
}
