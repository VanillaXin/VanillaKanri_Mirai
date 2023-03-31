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
    var capability = Capability()

    @Serializable
    class Capability {
        // 关键词回复
        var keyRep = true

        // MC RCON
        var rcon = false

        // 本地随机涩图
        var localRandomPic = false

        // 抽老婆
        var getWife = false

        // chatGpt
        var chatGPT = false

        // chatGPT Voice
        var chatGPTVoice = false

        // 在线随机涩图
        var onlineRandomPic = false

        // 在线AI画图
        var onlineAiPic = false

        // 查询测试
        var queryTest = false

    }

}
