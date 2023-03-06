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
    // /va key add [<group>] 精准|包含|拼音|正则 [key] rep [content]
    // /va key del [<group>] 精准|包含|拼音|正则 [key]

    /**
     * 关键词指令前缀*
     */
    var prefix: String = "key"

    /**
     * 关键词指令后缀*
     */
    var suffix: String = "rep"

    /**
     * 精准匹配+
     */
    var exactly: Set<String> = mutableSetOf("exactly", "perfect", "per")

    /**
     * 包含匹配+
     */
    var contain: Set<String> = mutableSetOf("contain", "include", "inc")

    /**
     * 拼音包含匹配+
     */
    var pinyin: Set<String> = mutableSetOf("pinyin", "pin")

    /**
     * 正则匹配+
     */
    var regex: Set<String> = mutableSetOf("regex", "reg", "regexp")
}
