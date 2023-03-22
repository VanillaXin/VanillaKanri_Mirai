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
     * 可选功能开关
     */
    var capability = hashMapOf(
        // 关键词回复
        "keyRep" to true,
        // MC RCON
        "rcon" to false,
        // 本地随机涩图
        "localRandomPic" to false,
        // 抽老婆
        "getWife" to false,
        // chatGpt
        "chatGPT" to false,
        // chatGPT Voice
        "chatGPTVoice" to false,
        // 在线随机涩图
        "onlineRandomPic" to false,
        // 在线AI画图
        "onlineAiPic" to false
    )

}
