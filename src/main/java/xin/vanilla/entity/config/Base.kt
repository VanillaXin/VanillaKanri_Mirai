package xin.vanilla.entity.config

import kotlinx.serialization.Serializable

/**
 * 基础配置
 */
@Serializable
class Base {
    /**
     * 后台管理群
     */
    var backGroup: Set<Long> = mutableSetOf()

    /**
     * 关键词基数
     *
     * 决定一个权级可以创建 同个关键词 回复的数量
     */
    var keyRadix: Int = 10

    /**
     * 关键词基数删除策略
     *
     * 当添加的关键词满了之后是否自动删除最旧的关键词
     *
     * 1启用 0默认 -1禁用
     */
    var keyRadixAutoDel: Int = 0

    /**
     * 自动通过权级为 1 的关键词
     *
     * 1启用 0默认 -1禁用
     */
    var keyAutoExamine: Int = 0

    /**
     * 调试模式
     */
    var debugMode: Int = 13

    /**
     * 自定义调试模式异常输出规则(正则表达式)
     */
    var debugCustomException: Map<String, String> = mutableMapOf()

    /**
     * 可选功能开关(0为关闭, 数字越大执行顺序越优先)
     */
    var capability: Map<String, Int> = mutableMapOf(
        "GroupMsgEvent.keyRep" to 99,
        "FriendMsgEvent.keyRep" to 99,
        "TimerMsgEvent.timer" to 99,
        "GroupMsgEvent.rcon" to 0,
        "GroupMsgEvent.localRandomPic" to 0,
        "GroupMsgEvent.getWife" to 1,
        "GroupMsgEvent.chatGPT" to 0,
        "GroupMsgEvent.chatGPTVoice" to 0,
        "GroupMsgEvent.onlineRandomPic" to 0,
        "GroupMsgEvent.onlineAiPic" to 0,
        "GroupMsgEvent.queryTest" to 0,
        "EventHandlers.debug" to 1,
    )

}
