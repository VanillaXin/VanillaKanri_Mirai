package xin.vanilla.common;

import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.config.instruction.BaseInstructions;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.entity.config.instruction.TimedTaskInstructions;
import xin.vanilla.util.RegUtils;
import xin.vanilla.util.StringUtils;

import java.util.HashSet;

public class RegExpConfig {
    private final VanillaKanri Va = VanillaKanri.INSTANCE;

    private final KanriInstructions kanri = Va.getGlobalConfig().getInstructions().getKanri();
    private final KeywordInstructions keyword = Va.getGlobalConfig().getInstructions().getKeyword();
    private final TimedTaskInstructions timed = Va.getGlobalConfig().getInstructions().getTimed();
    private final BaseInstructions base = Va.getGlobalConfig().getInstructions().getBase();


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

    public RegUtils defaultRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 设置群管理员指令
     */
    public RegUtils adminRegExp(String prefix) {
        //  ad add <QQ>
        return RegUtils.start().groupNon(prefix).separator()
                .groupByName("operation", base.getAdd(), base.getDelete()).separator()
                .groupIgByName("qq", RegUtils.REG_ATCODE).end();
    }

    /**
     * 设置群名片指令
     */
    public RegUtils cardRegExp(String prefix) {
        //  card <QQ> [CONTENT]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("qq", RegUtils.REG_ATCODE).separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 设置群精华消息指令
     */
    public RegUtils essenceRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 解除禁言指令
     */
    public RegUtils loudRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("qq", new HashSet<String>() {{
                    add(RegUtils.REG_ATCODE);
                    for (String s : base.getAtAll())
                        add(StringUtils.escapeExprSpecialWord(s));
                }}).end();
    }

    /**
     * 禁言指令
     */
    public RegUtils muteRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("qq", new HashSet<String>() {{
                    add(RegUtils.REG_ATCODE);
                    for (String s : base.getAtAll())
                        add(StringUtils.escapeExprSpecialWord(s));
                }}).separator("?")
                .groupIgByName("operation", "\\d{1,5}(?:\\.\\d{1,2})?").appendIg("?").end();
    }

    /**
     * 设置群头衔指令
     */
    public RegUtils tagRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("qq", RegUtils.REG_ATCODE).appendIg("?").separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 踢出群聊指令
     */
    public RegUtils kickRegExp(String prefix) {
        return RegUtils.start().appendIg(kanri.getKick().replaceAll("\\s", "\\\\s")
                        .replace("[VA_CODE.QQS]", new RegUtils()
                                .groupIgByName("qq", RegUtils.REG_ATCODE).toString())).separator("?")
                .groupIgByName("operation", "(?:0|1|真|假|是|否|true|false|y|n|Y|N)").appendIg("?")
                .end();
    }

    /**
     * 戳一戳指令
     */
    public RegUtils tapRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("qq", RegUtils.REG_ATCODE).separator("?")
                .groupIgByName("operation", "\\d").appendIg("?").end();
    }

    /**
     * 撤回群消息指令
     */
    public RegUtils withdrawRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator("?")
                .groupIgByName("operation", ".*?").end();
    }
}
