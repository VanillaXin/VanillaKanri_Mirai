package xin.vanilla.enums

/**
 * 数据缓存键值
 */
enum class DataCacheKey(private val keyString: String, val describe: String, private val args: Int) {
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


    fun getKey(): String {
        if (this.args > 0) throw RuntimeException("该枚举至少应该接收" + args + "个参数")
        return this.keyString
    }

    fun getKey(str: String): String {
        return this.keyString + str
    }

    fun getKey(str: Long): String {
        return this.keyString + str
    }
}

