package xin.vanilla.common;

import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.config.instruction.BaseInstructions;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.entity.config.instruction.TimedTaskInstructions;
import xin.vanilla.util.RegUtils;

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
                .groupIgByName("card", ".*?").end();
    }
}
