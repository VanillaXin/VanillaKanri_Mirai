package xin.vanilla.entity.config.instruction

import kotlinx.serialization.Serializable
import lombok.Data
import lombok.experimental.Accessors

/**
 * 定时任务操作指令
 */
@Data
@Accessors(chain = true)
@Serializable
class TimedTaskInstructions {
    // 定时任务指令前缀*
    var prefix: String = "timer"
}
