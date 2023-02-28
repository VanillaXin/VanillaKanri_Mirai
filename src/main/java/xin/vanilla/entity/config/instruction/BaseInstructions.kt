package xin.vanilla.entity.config.instruction

import kotlinx.serialization.Serializable
import lombok.Data
import lombok.experimental.Accessors
import net.mamoe.mirai.message.data.AtAll

/**
 * 基础通用指令
 */
@Data
@Accessors(chain = true)
@Serializable
class BaseInstructions {
    /**
     * 添加+(如: 加入黑名单)
     */
    var add: Set<String> = mutableSetOf("add")

    /**
     * 删除+(如: 删除群管理)
     */
    var delete: Set<String> = mutableSetOf("del")

    /**
     * 查询+(如: 查询黑名单)
     */
    var select: Set<String> = mutableSetOf("select", "list", "ls", "sel")

    /**
     * 全局+(如: 全局词库)
     */
    var global: Set<String> = mutableSetOf("all")

    /**
     * 当前+(如: 当前群)
     */
    var that: Set<String> = mutableSetOf("that", "this", "here")

    /**
     * 艾特全体+
     */
    var atAll: Set<String> = mutableSetOf("@全体成员", "@全体", "@所有人", AtAll.toString())

    /**
     * 艾特全体对应的数字账号+
     */
    var atAllId: Set<String> = mutableSetOf("-2333")
}
