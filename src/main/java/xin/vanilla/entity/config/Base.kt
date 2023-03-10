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
}
