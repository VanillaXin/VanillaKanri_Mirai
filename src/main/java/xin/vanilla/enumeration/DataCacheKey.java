package xin.vanilla.enumeration;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * 数据缓存键值
 */
public enum DataCacheKey {
    PLUGIN_MSG_SEND_COUNT("plugin.msgSendCount"),
    PLUGIN_MSG_RECEIVE_COUNT("plugin.msgReceiveCount");

    private final String name;

    DataCacheKey(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    @NotNull
    @Contract(pure = true)
    public String getName(String name) {
        return this.name + name;
    }

    @NotNull
    @Contract(pure = true)
    public String getName(long name) {
        return this.name + name;
    }
}
