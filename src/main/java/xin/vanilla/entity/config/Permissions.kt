package xin.vanilla.entity.config

import kotlinx.serialization.Serializable

/**
 * 插件角色
 */
@Serializable
class Permissions {
    /**
     * 主人
     */
    var botOwner: Long = 0L

    /**
     * 超管
     */
    var superAdmin: Set<Long> = HashSet()

    /**
     * 主管
     */
    var botAdmin: Set<Long> = HashSet()

    /**
     * 全局副管
     * <p>与群内单独设置的群副管权限相同</p>
     */
    var deputyAdmin: Set<Long> = HashSet()

}
