package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.config.instruction.BaseInstructions;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.util.Api;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.VanillaUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static xin.vanilla.util.VanillaUtils.PERMISSION_LEVEL_GROUPADMIN;
import static xin.vanilla.util.VanillaUtils.PERMISSION_LEVEL_GROUPOWNER;

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
        String prefix = ins.substring(0, ins.indexOf(' '));

        // 添加/取消管理员
        if (kanri.getAdmin().contains(prefix)) {
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_GROUPOWNER))
                return RETURN_BREAK_FALSE;

            String reg = "^" + prefix + " " + toRegString(new HashSet<String>() {{
                addAll(base.getAdd());
                addAll(base.getDelete());
            }}) + " (" + StringUtils.REG_ATCODE + "+)$";

            Matcher m = Pattern.compile(reg).matcher(ins);

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
                if (operation) Api.sendMessage(group, "已将 " + rep.substring(0, rep.length() - 1) + " 添加为管理员");
                else Api.sendMessage(group, "已取消 " + rep.substring(0, rep.length() - 1) + " 的管理员");
                return RETURN_BREAK_TRUE;
            }
        }
        // 设置群名片
        else if (kanri.getCard().contains(prefix)) {

        }
        // 添加群精华
        else if (kanri.getEssence().contains(prefix)) {

        }
        // 解除禁言
        else if (kanri.getLoud().contains(prefix)) {

        }
        // 禁言
        else if (kanri.getMute().contains(prefix)) {

        }
        // 设置群头衔
        else if (kanri.getTag().contains(prefix)) {

        }
        // 戳一戳
        else if (kanri.getTap().contains(prefix)) {

        }
        // 撤回消息
        else if (kanri.getWithdraw().contains(prefix)) {

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
            ins = s;
            return false;
        } else if (s.startsWith(prefix)) {
            ins = s.substring(prefix.length()).trim();
            return false;
        }
        return true;
    }

    private <T> String toRegString(Set<T> set) {
        if (set == null)
            return "";
        int iMax = set.size() - 1;
        if (iMax == -1) return "";

        StringBuilder b = new StringBuilder();
        b.append("(");
        int i = 0;
        for (T t : set) {
            b.append(StringUtils.escapeExprSpecialWord(t.toString()));
            if (i == iMax)
                return b.append(')').toString();
            b.append("|");
            i++;
        }
        return b.toString();
    }
}
