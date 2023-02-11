package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.*;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.config.instruction.BaseInstructions;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.entity.data.MsgCache;
import xin.vanilla.util.Api;
import xin.vanilla.util.RegUtils;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.VanillaUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static xin.vanilla.mapper.impl.MessageCacheImpl.MSG_TYPE_GROUP;
import static xin.vanilla.util.VanillaUtils.PERMISSION_LEVEL_DEPUTYADMIN;
import static xin.vanilla.util.VanillaUtils.PERMISSION_LEVEL_SUPERADMIN;

public class InstructionMsgEvent {
    private static final int RETURN_CONTINUE = 1;
    private static final int RETURN_BREAK_TRUE = 2;
    private static final int RETURN_BREAK_FALSE = 3;

    private final VanillaKanri Va = VanillaKanri.INSTANCE;
    private final MessageEvent event;
    private final MessageChain msg;
    private Group group;
    private final User sender;
    private final Bot bot;
    private final long time;


    private final KanriInstructions kanri = Va.globalConfig.getInstructions().getKanri();
    private final KeywordInstructions keyword = Va.globalConfig.getInstructions().getKeyword();
    private final BaseInstructions base = Va.globalConfig.getInstructions().getBase();
    private String ins = "";

    public InstructionMsgEvent(MessageEvent event) {
        this.event = event;
        this.msg = this.event.getMessage();
        if (event instanceof GroupMessageEvent) this.group = ((GroupMessageEvent) this.event).getGroup();
        if (event instanceof GroupTempMessageEvent) this.group = ((GroupTempMessageEvent) this.event).getGroup();
        this.sender = this.event.getSender();
        this.bot = this.event.getBot();
        this.time = this.event.getTime();
    }

    /**
     * @return 是否不继续执行事件监听, true: 不执行, false: 执行
     */
    public boolean run() {
        int back;
        // 判断是否指令消息(仅判断顶级前缀)
        if (!VanillaUtils.isInstructionMsg(msg, false)) return false;

        // 判断发送者有无操作机器人的权限
        // if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_GROUPADMIN)) return false;

        // 判断机器人是否群管
        if (!VanillaUtils.isGroupOwnerOrAdmin(group)) return false;

        // 解析执行群管指令
        if (event instanceof GroupMessageEvent) {
            back = kanriIns();
            if (back == RETURN_BREAK_TRUE) return true;
            else if (back == RETURN_BREAK_FALSE) return false;
        }

        // 解析执行词库指令
        back = keyIns();
        if (back == RETURN_BREAK_TRUE) return true;
        else if (back == RETURN_BREAK_FALSE) return false;


        return false;
    }

    /**
     * 解析执行群管指令
     * <p>
     * TODO 注解+反射优雅实现
     */
    public int kanriIns() {
        // 判断是否群管指令
        if (delPrefix(kanri.getPrefix())) return RETURN_CONTINUE;
        int index = RegUtils.containsRegSeparator(ins);
        String prefix;
        if (index >= 0) prefix = ins.substring(0, index);
        else prefix = ins;

        // 添加/取消管理员
        if (kanri.getAdmin().contains(prefix)) {
            // 判断发送者有无操作的权限
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_SUPERADMIN))
                return RETURN_BREAK_FALSE;

            //  ad add <QQ>
            RegUtils reg = RegUtils.start().groupNon(prefix).separator()
                    .groupByName("operation", base.getAdd(), base.getDelete()).separator()
                    .groupIgByName("qq", RegUtils.REG_ATCODE).end();

            if (reg.matcher(ins).find()) {
                boolean operation = base.getAdd().contains(reg.getMatcher().group("operation"));
                StringBuilder rep = new StringBuilder();
                for (long qq : VanillaUtils.getQQFromAt(reg.getMatcher().group("qq"))) {
                    rep.append(',');
                    NormalMember normalMember = group.get(qq);
                    if (normalMember != null) {
                        normalMember.modifyAdmin(operation);
                        rep.append(qq);
                    }
                }

                if (!StringUtils.isNullOrEmpty(rep.toString())) {
                    rep.delete(0, 1);
                    if (operation)
                        Api.sendMessage(group, "已将 " + rep + " 添加为管理员");
                    else Api.sendMessage(group, "已取消 " + rep + " 的管理员");
                } else {
                    Api.sendMessage(group, "待操作对象为空");
                }
                return RETURN_BREAK_TRUE;
            }
        }
        // 设置群名片
        else if (kanri.getCard().contains(prefix)) {
            // 判断发送者有无操作的权限
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

            //  card <QQ> [CONTENT]
            RegUtils reg = RegUtils.start().groupNon(prefix).separator()
                    .groupIgByName("qq", RegUtils.REG_ATCODE).separator("?")
                    .groupIgByName("card", "(.*?)").end();

            if (reg.matcher(ins).find()) {
                StringBuilder rep = new StringBuilder();
                for (long qq : VanillaUtils.getQQFromAt(reg.getMatcher().group("qq"))) {
                    rep.append(',');
                    NormalMember normalMember = group.get(qq);
                    if (normalMember != null) {
                        normalMember.setNameCard(reg.getMatcher().group("card"));
                        rep.append(qq);
                    }
                }
                if (!StringUtils.isNullOrEmpty(rep.toString())) {
                    rep.delete(0, 1);
                    if (rep.toString().equals("")) {
                        Api.sendMessage(group, "操作失败");
                    } else {
                        if (StringUtils.isNullOrEmpty(reg.getMatcher().group("card")))
                            Api.sendMessage(group, "已清除 " + rep + " 的名片");
                        else
                            Api.sendMessage(group, "已将 " + rep + " 的名片修改为:\n" + reg.getMatcher().group("card"));
                    }
                } else {
                    Api.sendMessage(group, "待操作对象为空");
                }
            }
        }
        // 添加群精华
        else if (kanri.getEssence().contains(prefix)) {
            // 判断发送者有无操作的权限
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

            //  essence add/del/[CONTENT]
            RegUtils reg = RegUtils.start().groupNon(prefix).separator("?")
                    .groupIgByName("text", "(.*?)").end();

            if (reg.matcher(ins).find()) {
                String text = reg.getMatcher().group("text");
                if (ins.equals(prefix) || base.getAdd().contains(text)) {
                    QuoteReply quoteReply = msg.get(QuoteReply.Key);
                    if (quoteReply != null) {
                        if (group.setEssenceMessage(quoteReply.getSource())) {
                            Api.sendMessage(group, new MessageChainBuilder().append(quoteReply).append("已将该消息设为精华").build());
                        } else {
                            Api.sendMessage(group, "精华消息设置失败");
                        }
                    }
                } else if (base.getDelete().contains(text)) {
                    // 未发现取消精华消息接口
                    // QuoteReply quoteReply = msg.get(QuoteReply.Key);
                    // if (quoteReply != null) {
                    // }
                } else {
                    OnlineMessageSource.Outgoing source = Api.sendMessage(group, text).getSource();
                    if (!group.setEssenceMessage(source)) {
                        Api.sendMessage(group, "精华消息设置失败");
                    }
                }
            }
        }
        // 解除禁言
        else if (kanri.getLoud().contains(prefix)) {
            // 判断发送者有无操作的权限
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

            //  loud <QQ>
            RegUtils reg = RegUtils.start().groupNon(prefix).separator()
                    .groupIgByName("qq", new HashSet<String>() {{
                        add(RegUtils.REG_ATCODE);
                        for (String s : base.getAtAll())
                            add(StringUtils.escapeExprSpecialWord(s));
                    }}).end();
            if (reg.matcher(ins).find()) {
                StringBuilder rep = new StringBuilder();
                String qqString = reg.getMatcher().group("qq");
                if (qqString.equals(AtAll.INSTANCE.toString()) || base.getAtAll().contains(qqString)) {
                    if (group.getSettings().isMuteAll()) {
                        group.getSettings().setMuteAll(false);
                        Api.sendMessage(group, "已关闭全体禁言");
                    }
                } else {
                    for (long qq : VanillaUtils.getQQFromAt(qqString)) {
                        rep.append(',');
                        NormalMember normalMember = group.get(qq);
                        if (normalMember != null) {
                            if (normalMember.isMuted()) {
                                normalMember.unmute();
                                rep.append(qq);
                            }
                        }
                    }
                    if (!StringUtils.isNullOrEmpty(rep.toString())) {
                        if (rep.toString().equals(",")) {
                            Api.sendMessage(group, "操作失败");
                        } else {
                            Api.sendMessage(group, "已解除 " + rep.delete(0, 1) + " 的禁言");
                        }
                    } else {
                        Api.sendMessage(group, "待操作对象为空或未被禁言");
                    }
                }
            }
        }
        // 禁言
        else if (kanri.getMute().contains(prefix)) {
            // 判断发送者有无操作的权限
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

            //  mute <QQ>/<全体成员> [时间]
            RegUtils reg = RegUtils.start().groupNon(prefix).separator()
                    .groupIgByName("qq", new HashSet<String>() {{
                        add(RegUtils.REG_ATCODE);
                        for (String s : base.getAtAll())
                            add(StringUtils.escapeExprSpecialWord(s));
                    }}).separator("?")
                    .groupIgByName("time", "\\d{1,5}(?:\\.\\d{1,2})?").append("?").end();
            if (reg.matcher(ins).find()) {
                String qqString = reg.getMatcher().group("qq");
                String time = reg.getMatcher().group("time");
                if (qqString.equals(AtAll.INSTANCE.toString()) || base.getAtAll().contains(qqString)) {
                    if (!group.getSettings().isMuteAll()) {
                        group.getSettings().setMuteAll(true);
                        Api.sendMessage(group, "已开启全体禁言");
                        if (!StringUtils.isNullOrEmpty(time)) {
                            Va.getScheduler().delayed(Math.round(Float.parseFloat(time)) * 60L * 1000L, () -> {
                                if (group.getSettings().isMuteAll()) {
                                    group.getSettings().setMuteAll(false);
                                }
                            });
                            Api.sendMessage(group, "并将在 " + time + " 分钟后关闭全体禁言");
                        }
                    }
                } else if (!StringUtils.isNullOrEmpty(time)) {
                    NormalMember senderMember = group.get(sender.getId());
                    StringBuilder successMsg = new StringBuilder();
                    for (long qq : VanillaUtils.getQQFromAt(qqString)) {
                        successMsg.append(',');
                        NormalMember normalMember = group.get(qq);
                        if (normalMember != null && senderMember != null) {
                            // 比较操作者与被操作者权限
                            if (VanillaUtils.equalsPermission(bot, group, senderMember.getId(), normalMember.getId())) {
                                try {
                                    normalMember.mute(Math.round(Float.parseFloat(time)) * 60);
                                    successMsg.append(qq);
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                    }
                    if (!StringUtils.isNullOrEmpty(successMsg.toString())) {
                        successMsg.delete(0, 1);
                        if (successMsg.toString().equals("")) {
                            Api.sendMessage(group, "权限不足");
                        } else {
                            Api.sendMessage(group, "已禁言 " + successMsg + " " + time + "分钟");
                        }
                    } else {
                        Api.sendMessage(group, "待操作对象为空");
                    }
                }
            }
        }
        // 设置群头衔
        else if (kanri.getTag().contains(prefix)) {
            // 判断机器人是否群主
            if (!VanillaUtils.isGroupOwner(group)) return RETURN_BREAK_FALSE;

            //  tag <QQ> [CONTENT]
            //  tag [CONTENT]
            RegUtils reg = RegUtils.start().groupNon(prefix).separator()
                    .groupIgByName("qq", RegUtils.REG_ATCODE).append("?").separator("?")
                    .groupIgByName("tag", "(.*?)").end();
            if (reg.matcher(ins).find()) {
                String qqString = reg.getMatcher().group("qq");
                String tag = reg.getMatcher().group("tag");
                if (StringUtils.isNullOrEmpty(qqString)) {
                    NormalMember normalMember = group.get(sender.getId());
                    if (normalMember != null) {
                        normalMember.setSpecialTitle(tag);
                        Api.sendMessage(group, "已将阁下的头衔修改为:\n" + tag);
                    }
                } else {
                    StringBuilder successMsg = new StringBuilder();
                    for (long qq : VanillaUtils.getQQFromAt(qqString)) {
                        successMsg.append(',');
                        NormalMember normalMember = group.get(qq);
                        if (normalMember != null) {
                            normalMember.setSpecialTitle(tag);
                            successMsg.append(qq);
                        }
                    }
                    if (!StringUtils.isNullOrEmpty(successMsg.toString())) {
                        successMsg.delete(0, 1);
                        if (successMsg.toString().equals("")) {
                            Api.sendMessage(group, "操作失败");
                        } else {
                            Api.sendMessage(group, "已修改 " + successMsg + " 的头衔为:\n" + tag);
                        }
                    } else {
                        Api.sendMessage(group, "待操作对象为空");
                    }
                }
            }
        }
        // 戳一戳
        else if (kanri.getTap().contains(prefix)) {
            //  tap <QQ> [num]
            RegUtils reg = RegUtils.start().groupNon(prefix).separator()
                    .groupIgByName("qq", RegUtils.REG_ATCODE).separator("?")
                    .groupIgByName("num", "\\d").append("?").end();
            // TODO 次数限制
            if (reg.matcher(ins).find()) {
                String qqString = reg.getMatcher().group("qq");
                String num = reg.getMatcher().group("num");
                int i;
                try {
                    i = Integer.parseInt(num);
                } catch (NumberFormatException ignored) {
                    i = 1;
                }
                for (int j = 0; j < i; j++) {
                    Va.getScheduler().delayed(j * 5 * 1000L, () -> {
                        for (long qq : VanillaUtils.getQQFromAt(qqString)) {
                            NormalMember normalMember = group.get(qq);
                            if (normalMember != null) {
                                normalMember.nudge().sendTo(group);
                            }
                        }
                    });
                }
                if (VanillaUtils.getQQFromAt(qqString).length == 0)
                    Api.sendMessage(group, "待操作对象为空");
            }
        }
        // 撤回消息
        else if (kanri.getWithdraw().contains(prefix)) {
            // 判断发送者有无操作的权限
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

            // recall [num]
            // recall [num-num]
            // recall [num~num]
            // recall [num] [num]
            RegUtils reg = RegUtils.start().groupNon(prefix).separator("?")
                    .groupIgByName("text", "(.*?)").end();

            if (reg.matcher(ins).find()) {
                MessageSource messageSource = msg.get(MessageSource.Key);
                if (messageSource != null) try {
                    MessageSource.recall(messageSource);
                } catch (IllegalStateException ignored) {
                    Va.getLogger().info("无权撤回或已被撤回");
                }
                String text = reg.getMatcher().group("text").trim();
                QuoteReply quoteReply = msg.get(QuoteReply.Key);
                int[] sourceIds;
                if (quoteReply != null) {
                    sourceIds = quoteReply.getSource().getIds();
                    if (ins.equals(prefix)) {
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
                            int[] ids = Arrays.stream(text.replaceAll("-", "~")
                                            .split("~"))
                                    .mapToInt(s -> sourceId - Integer.parseInt(s))
                                    .toArray();
                            if (ids.length == 2) {
                                for (int id = ids[0]; id < ids[1]; id++) {
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
            }
        }
        // 踢出群成员
        else if (kanri.getKick().equals(prefix)) {
            // 判断发送者有无操作的权限
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;
            // TODO 判断踢出指令
        }
        return RETURN_CONTINUE;
    }

    /**
     * 解析执行词库指令
     */
    private int keyIns() {
        // 判断是否词库管指令
        if (delPrefix(keyword.getPrefix()))
            return RETURN_CONTINUE;
        String prefix = ins.substring(0, ins.indexOf(' '));
        // TODO

        return RETURN_CONTINUE;
    }

    private String delPrefix() {
        String prefix = Va.globalConfig.getInstructions().getPrefix();
        if (StringUtils.isNullOrEmpty(prefix)) return msg.serializeToMiraiCode().trim();
        else return msg.serializeToMiraiCode().substring(prefix.length()).trim();
    }

    /**
     * 是否包含前缀, 并删除前缀
     */
    private boolean delPrefix(String prefix) {
        String s = delPrefix();
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

    private void recall(int id) {
        MsgCache msgCache = Va.messageCache.getMsgCache(id + "|", group.getId(), MSG_TYPE_GROUP);
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
}
