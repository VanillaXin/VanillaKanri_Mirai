package xin.vanilla.entity.config.instruction

import kotlinx.serialization.Serializable
import lombok.Data
import lombok.experimental.Accessors

/**
 * 关键词词库操作指令
 */
@Data
@Accessors(chain = true)
@Serializable
class KeywordInstructions {
    // 关键词指令前缀*
    var prefix: String = "key"
}
