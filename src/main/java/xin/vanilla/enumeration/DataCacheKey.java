package xin.vanilla.enumeration;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * 数据缓存键值
 */
public enum DataCacheKey {
    /**
     * 插件发送消息计数
     */
    PLUGIN_MSG_SEND_COUNT("plugin.msgSendCount", "插件消息发送计数", 0),
    /**
     * 插件接收消息计数
     */
    PLUGIN_MSG_RECEIVE_COUNT("plugin.msgReceiveCount", "插件消息接收计数", 0),
    /**
     * 插件运行时长
     */
    PLUGIN_ENABLE_TIME("plugin.enableTime", "插件启用时刻", 0),
    /**
     * 机器人在线时长
     */
    PLUGIN_BOT_ONLINE_TIME("plugin.botOnlineTime.", "机器人登录成功时刻", 1);

    private final String key;
    private final String name;
    private final int args;

    DataCacheKey(String key, String name, int args) {
        this.key = key;
        this.name = name;
        this.args = args;
    }

    public String getKey() {
        if (this.args > 0) throw new RuntimeException("该枚举至少应该接收" + args + "个参数");
        return this.key;
    }

    @NotNull
    @Contract(pure = true)
    public String getKey(String str) {
        return this.key + str;
    }

    @NotNull
    @Contract(pure = true)
    public String getKey(long str) {
        return this.key + str;
    }
}

