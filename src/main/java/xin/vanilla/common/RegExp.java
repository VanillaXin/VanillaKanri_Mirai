package xin.vanilla.common;

import xin.vanilla.util.RegUtils;

public class RegExp {
    // private static final VanillaKanri Va = VanillaKanri.INSTANCE;

    /**
     * RCON 指令返回内容: /list
     */
    public static final RegUtils RCON_RESULT_LIST =
            RegUtils.start().append("There").separator().append("are").separator()
                    .groupIgByName("num", "\\d{1,2}").separator()
                    .append("of").separator().append("a").separator().append("max").separator().append("of").separator()
                    .groupIgByName("max", "\\d{1,2}").separator()
                    .append("players").separator().append("online:").separator("{0,2}")
                    .groupIgByName("player", ".*?").end();
}
