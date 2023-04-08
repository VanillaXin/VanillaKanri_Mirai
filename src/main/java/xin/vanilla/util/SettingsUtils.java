package xin.vanilla.util;

import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;

import java.util.Set;

/**
 * 设置工具类
 * <p>判断全局/群聊设置中的开关等</p>
 */
public class SettingsUtils {
    private final static VanillaKanri Va = VanillaKanri.INSTANCE;

    /**
     * 获取 关键词条数到上限后 是否自动删除
     */
    public static boolean getKeyRadixAutoDel(long group) {
        int keyRadixAutoDel = Va.getGroupConfig().getBase(group).getKeyRadixAutoDel();
        if (keyRadixAutoDel == 0) keyRadixAutoDel = Va.getGlobalConfig().getBase().getKeyRadixAutoDel();
        return keyRadixAutoDel >= 0;
    }

    /**
     * 获取 是否自动启用普通群员添加的关键词
     */
    public static boolean getKeyAutoExamine(long group) {
        int keyRadixAutoDel = Va.getGroupConfig().getBase(group).getKeyAutoExamine();
        if (keyRadixAutoDel == 0) keyRadixAutoDel = Va.getGlobalConfig().getBase().getKeyAutoExamine();
        return keyRadixAutoDel >= 0;
    }

    /**
     * 获取后台管理群
     */
    @NotNull
    public static Set<Long> getBackGroup(long group) {
        Set<Long> groups = Va.getGlobalConfig().getBase().getBackGroup();
        if (group != 0)
            groups.addAll(Va.getGroupConfig().getBase(group).getBackGroup());
        return groups;
    }

    /**
     * 获取关键词基数
     */
    public static int getKeyRadix(long group) {
        int keyRadix = Va.getGroupConfig().getBase(group).getKeyRadix();
        if (keyRadix < 1) keyRadix = Va.getGlobalConfig().getBase().getKeyRadix();
        return keyRadix;
    }
}
