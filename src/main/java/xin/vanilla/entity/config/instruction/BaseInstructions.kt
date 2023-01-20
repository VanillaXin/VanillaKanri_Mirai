package xin.vanilla.entity.config.instruction

import kotlinx.serialization.Serializable
import lombok.Data
import lombok.experimental.Accessors

/**
 * 基础通用指令
 */
@Data
@Accessors(chain = true)
@Serializable
class BaseInstructions {
    // 添加+(如: 加入黑名单)
    var add: Set<String> = mutableSetOf("add")

    // 删除+(如: 删除群管理)
    var delete: Set<String> = mutableSetOf("del")

    // 全局+(如: 全局词库)
    var global: Set<String> = mutableSetOf("all")
}