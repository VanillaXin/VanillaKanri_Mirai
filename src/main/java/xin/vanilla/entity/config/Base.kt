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
    var backGroup: List<Long> = mutableListOf()
}
