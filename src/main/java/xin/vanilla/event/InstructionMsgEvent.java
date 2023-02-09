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
import xin.vanilla.util.Api;
import xin.vanilla.util.RegUtils;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.VanillaUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static xin.vanilla.util.VanillaUtils.*;

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
        if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_GROUPADMIN)) return false;

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
     */
    public int kanriIns() {
        // 判断是否群管指令
        if (delPrefix(kanri.getPrefix())) return RETURN_CONTINUE;
        int index = RegUtils.containsRegSeparator(ins);
        if (index < 0) return RETURN_CONTINUE;
        String prefix = ins.substring(0, index);

        // 添加/取消管理员
        if (kanri.getAdmin().contains(prefix)) {
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_GROUPOWNER))
                return RETURN_BREAK_FALSE;

            //  ad add <QQ>
            String reg = "^" + prefix + RegUtils.REG_SEPARATOR + toRegString(new HashSet<String>() {{
                addAll(base.getAdd());
                addAll(base.getDelete());
            }}) + RegUtils.REG_SEPARATOR + "(" + RegUtils.REG_ATCODE + "+)$";

            Matcher m = Pattern.compile(reg, Pattern.DOTALL).matcher(ins);

            if (m.find()) {
                boolean operation = base.getAdd().contains(m.group(1));
                StringBuilder rep = new StringBuilder();
                for (long qq : VanillaUtils.getQQFromAt(m.group(2))) {
                    NormalMember normalMember = group.get(qq);
                    if (normalMember != null) {
                        normalMember.modifyAdmin(operation);
                        rep.append(qq).append(',');
                    }
                }

                if (!StringUtils.isNullOrEmpty(rep.toString())) {
                    if (operation)
                        Api.sendMessage(group, "已将 " + rep.substring(0, rep.length() - 1) + " 添加为管理员");
                    else Api.sendMessage(group, "已取消 " + rep.substring(0, rep.length() - 1) + " 的管理员");
                } else {
                    Api.sendMessage(group, "待操作对象为空");
                }
                return RETURN_BREAK_TRUE;
            }
        }
        // 设置群名片
        else if (kanri.getCard().contains(prefix)) {
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

            //  card <QQ> [CONTENT]
            String reg = "^" + prefix + RegUtils.REG_SEPARATOR + "(" + RegUtils.REG_ATCODE + "+)" + RegUtils.REG_SEPARATOR + "?" + "(.*?)$";

            Matcher m = Pattern.compile(reg, Pattern.DOTALL).matcher(ins);

            if (m.find()) {
                StringBuilder rep = new StringBuilder();
                for (long qq : VanillaUtils.getQQFromAt(m.group(1))) {
                    NormalMember normalMember = group.get(qq);
                    if (normalMember != null) {
                        normalMember.setNameCard(m.group(2));
                        rep.append(qq).append(',');
                    }
                }
                if (!StringUtils.isNullOrEmpty(rep.toString())) {
                    if (StringUtils.isNullOrEmpty(m.group(2)))
                        Api.sendMessage(group, "已清除 " + rep.substring(0, rep.length() - 1) + " 的名片");
                    else
                        Api.sendMessage(group, "已将 " + rep.substring(0, rep.length() - 1) + " 的名片修改为:\n" + m.group(2));
                } else {
                    Api.sendMessage(group, "待操作对象为空");
                }
            }
        }
        // 添加群精华
        else if (kanri.getEssence().contains(prefix)) {
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

            //  essence add/del/[CONTENT]
            String reg = "^" + prefix + RegUtils.REG_SEPARATOR + "(.*?)$";

            Matcher m = Pattern.compile(reg, Pattern.DOTALL).matcher(ins);

            if (m.find()) {
                String con = m.group(1);
                if (base.getAdd().contains(con)) {
                    QuoteReply quoteReply = msg.get(QuoteReply.Key);
                    if (quoteReply != null) {
                        if (group.setEssenceMessage(quoteReply.getSource())) {
                            Api.sendMessage(group, new MessageChainBuilder().append(quoteReply).append("已将该消息设为精华").build());
                        } else {
                            Api.sendMessage(group, "精华消息设置失败");
                        }
                    }
                } else if (base.getDelete().contains(con)) {
                    // 未发现取消精华消息接口
                    // QuoteReply quoteReply = msg.get(QuoteReply.Key);
                    // if (quoteReply != null) {
                    // }
                } else {
                    OnlineMessageSource.Outgoing source = Api.sendMessage(group, con).getSource();
                    if (!group.setEssenceMessage(source)) {
                        Api.sendMessage(group, "精华消息设置失败");
                    }
                }
                Va.getLogger().info("精华: " + con);
            }
        }
        // 解除禁言
        else if (kanri.getLoud().contains(prefix)) {
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

            //  loud <QQ>
            RegUtils reg = RegUtils.start().groupNon(prefix).separator().groupIgByName("qq", RegUtils.REG_ATCODE).end();
            if (reg.matcher(ins).find()) {
                StringBuilder rep = new StringBuilder();
                String qqString = reg.getMatcher().group("qq");
                if (qqString.equals(AtAll.INSTANCE.toString())) {
                    if (group.getSettings().isMuteAll()) {
                        group.getSettings().setMuteAll(false);
                    }
                    rep.append("全体成员,");
                } else {
                    for (long qq : VanillaUtils.getQQFromAt(qqString)) {
                        NormalMember normalMember = group.get(qq);
                        if (normalMember != null) {
                            if (normalMember.isMuted()) {
                                normalMember.mute(0);
                                rep.append(qq).append(',');
                            }
                        }
                    }
                }
                if (!StringUtils.isNullOrEmpty(rep.toString())) {
                    Api.sendMessage(group, "已解除 " + rep.substring(0, rep.length() - 1) + " 的禁言");
                } else {
                    Api.sendMessage(group, "待操作对象为空或未被禁言");
                }
            }
        }
        // 禁言
        else if (kanri.getMute().contains(prefix)) {
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

            //  mute <QQ>/<全体成员> [时间]
            RegUtils reg = RegUtils.start().groupNon(prefix).separator()
                    .groupIgByName("qq", RegUtils.REG_ATCODE, "@全体成员").separator()
                    .groupIgByName("time", "\\d{1,5}(?:\\.\\d{1,2})?").end();
            if (reg.matcher(ins).find()) {
                StringBuilder rep = new StringBuilder();
                String qqString = reg.getMatcher().group("qq");
                if (qqString.equals(AtAll.INSTANCE.toString()) || qqString.equals("@全体成员")) {
                    if (group.getSettings().isMuteAll()) {
                        group.getSettings().setMuteAll(true);
                    }
                    rep.append("全体成员,");
                } else {
                    int time = Math.round(Float.parseFloat(reg.getMatcher().group("time")) * 60);
                    for (long qq : VanillaUtils.getQQFromAt(qqString)) {
                        NormalMember normalMember = group.get(qq);
                        if (normalMember != null) {
                            try {
                                normalMember.mute(time);
                                rep.append(qq).append(',');
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
                if (!StringUtils.isNullOrEmpty(rep.toString())) {
                    if (rep.toString().equals("全体成员,")) {
                        Api.sendMessage(group, "已开启全体禁言");
                    } else {
                        Api.sendMessage(group, "已禁言 " + rep.substring(0, rep.length() - 1) + " " + reg.getMatcher().group("time") + "分钟");
                    }
                } else {
                    Api.sendMessage(group, "待操作对象为空");
                }
            }
        }
        // 设置群头衔
        else if (kanri.getTag().contains(prefix)) {
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

        }
        // 戳一戳
        else if (kanri.getTap().contains(prefix)) {
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

        }
        // 撤回消息
        else if (kanri.getWithdraw().contains(prefix)) {
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTYADMIN))
                return RETURN_BREAK_FALSE;

        }
        // 踢出群成员
        else if (kanri.getKick().equals(prefix)) {
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
}
