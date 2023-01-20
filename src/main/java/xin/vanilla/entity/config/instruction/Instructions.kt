package xin.vanilla.entity.config.instruction

import kotlinx.serialization.Serializable
import lombok.Data
import lombok.experimental.Accessors

/**
 * 插件指令集合
 */
@Data
@Accessors(chain = true)
@Serializable
class Instructions {
    // -: 可以有0~1个, *: 有且只有1个, +:可以有1~∞个

    // 顶级前缀-
    var prefix: String = "/va"

    // 二级前缀列表
    var secondaryPrefix: Set<String> = HashSet()

    // 基础指令
    var base: BaseInstructions = BaseInstructions()

    // 关键词指令
    var keyword: KeywordInstructions = KeywordInstructions()

    // 群管指令
    var kanri: KanriInstructions = KanriInstructions()
}
