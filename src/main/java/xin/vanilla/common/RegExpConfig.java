package xin.vanilla.common;

import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.AtAll;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.config.instruction.BaseInstructions;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.entity.config.instruction.TimedTaskInstructions;
import xin.vanilla.util.RegUtils;
import xin.vanilla.util.StringUtils;

import java.util.HashSet;

import static xin.vanilla.util.RegUtils.REG_SEPARATOR;

public class RegExpConfig {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;

    private static final KanriInstructions kanri = Va.getGlobalConfig().getInstructions().getKanri();
    private static final KeywordInstructions keyword = Va.getGlobalConfig().getInstructions().getKeyword();
    private static final TimedTaskInstructions timed = Va.getGlobalConfig().getInstructions().getTimed();
    private static final BaseInstructions base = Va.getGlobalConfig().getInstructions().getBase();


    public static final String QQ_CODE = "(?:(?:" +
            StringUtils.escapeExprSpecialWord(new At(2333333333L).toString()).replace("2333333333", "\\d{6,10}")
            + "|" + StringUtils.escapeExprSpecialWord(AtAll.INSTANCE.toString())
            + "|\\d{6,10})" + REG_SEPARATOR + "?)+";

    public static final String GROUP_CODE = "<(?:(?:\\d{6,10}"
            + "|" + RegUtils.processGroup(base.getThat())
            + "|" + RegUtils.processGroup(base.getGlobal()) + ")" + REG_SEPARATOR + "?)*?>";

    /**
     * RCON 指令返回内容: /list
     */
    public static final RegUtils RCON_RESULT_LIST =
            RegUtils.start().append("There").separator().append("are").separator()
                    .groupIgByName("num", "\\d{1,2}").separator()
                    .append("of").separator().append("a").separator().append("max").separator().append("of").separator()
                    .groupIgByName("max", "\\d{1,2}").separator()
                    .append("players").separator().append("online:").separator()
                    .groupIgByName("player", "[\\S ]*?").separator();

    public static RegUtils defaultRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 设置群管理员指令
     */
    public static RegUtils adminRegExp(String prefix) {
        //  ad add <QQ>
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("operation", base.getAdd(), base.getDelete()).separator()
                .groupIgByName("qq", QQ_CODE).end();
    }

    /**
     * 设置副管指令
     */
    public static RegUtils deputyAdminRegExp(String prefix) {
        //  dad add <QQ>
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("operation", base.getAdd(), base.getDelete(), base.getSelect()).separator("?")
                .groupIgByName("qq", QQ_CODE).appendIg("?").end();
    }

    /**
     * 设置主管指令
     */
    public static RegUtils botAdminRegExp(String prefix) {
        //  bad add <QQ>
        return RegUtils.start().groupNon(prefix).separator()
                .groupByName("operation", base.getAdd(), base.getDelete(), base.getSelect()).separator("?")
                .groupIgByName("qq", QQ_CODE).appendIg("?").end();
    }

    /**
     * 设置超管指令
     */
    public static RegUtils superAdminRegExp(String prefix) {
        //  sad add <QQ>
        return botAdminRegExp(prefix);
    }

    /**
     * 设置主人指令
     */
    public static RegUtils botOwnerRegExp(String prefix) {
        //  owner add <QQ>
        return botAdminRegExp(prefix);
    }

    /**
     * 设置群名片指令
     */
    public static RegUtils cardRegExp(String prefix) {
        //  card <QQ> [CONTENT]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("qq", QQ_CODE).separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 设置群精华消息指令
     */
    public static RegUtils essenceRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator("?")
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 解除禁言指令
     */
    public static RegUtils loudRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("qq", new HashSet<String>() {{
                    add(QQ_CODE);
                    for (String s : base.getAtAll())
                        add(StringUtils.escapeExprSpecialWord(s));
                }}).end();
    }

    /**
     * 禁言指令
     */
    public static RegUtils muteRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("qq", new HashSet<String>() {{
                    add(QQ_CODE);
                    for (String s : base.getAtAll())
                        add(StringUtils.escapeExprSpecialWord(s));
                }}).separator("?")
                .groupIgByName("operation", "\\d{1,5}(?:\\.\\d{1,2})?").appendIg("?").end();
    }

    /**
     * 设置群头衔指令
     */
    public static RegUtils tagRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("qq", QQ_CODE).appendIg("?").separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 踢出群聊指令
     */
    public static RegUtils kickRegExp(String prefix) {
        return RegUtils.start().appendIg(kanri.getKick().replaceAll("\\s", "\\\\s")
                        .replace("[VA_CODE.QQS]", new RegUtils()
                                .groupIgByName("qq", QQ_CODE).toString())).separator("?")
                .groupIgByName("operation", "(?:0|1|真|假|是|否|true|false|y|n|Y|N)").appendIg("?")
                .groupIgByName("group", GROUP_CODE).appendIg("?")
                .end();
    }

    /**
     * 戳一戳指令
     */
    public static RegUtils tapRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("qq", QQ_CODE).separator("?")
                .groupIgByName("operation", "\\d").appendIg("?").end();
    }

    /**
     * 撤回群消息指令
     */
    public static RegUtils withdrawRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator("?")
                // .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 添加关键词回复指令
     */
    public static RegUtils keyAddRegExp(String prefix) {
        // /va key add [<group>] 精准|包含|拼音|正则 [key] rep [content]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("type", keyword.getExactly(), keyword.getContain(), keyword.getPinyin(), keyword.getRegex()).separator()
                .groupIgByName("key", ".*?").separator()
                .groupNon(keyword.getSuffix()).separator()
                .groupIgByName("rep", ".*?").end();
    }

    /**
     * 删除关键词回复指令
     */
    public static RegUtils keyDelRegExp(String prefix) {
        // /va key del [<group>] 精准|包含|拼音|正则 [key]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("type", keyword.getExactly(), keyword.getContain(), keyword.getPinyin(), keyword.getRegex()).separator()
                .groupIgByName("key", ".*?").end();
    }
}
