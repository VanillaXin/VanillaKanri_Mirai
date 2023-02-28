package xin.vanilla.entity.config.instruction

import kotlinx.serialization.Serializable
import lombok.Data
import lombok.experimental.Accessors

/**
 * 群管操作指令
 */
@Data
@Accessors(chain = true)
@Serializable
class KanriInstructions {
    /**
     * 群管指令前缀-
     */
    var prefix: String = ""

    /**
     * 头衔+
     */
    var tag: Set<String> = mutableSetOf("tag")

    /**
     * 群名片+
     */
    var card: Set<String> = mutableSetOf("card")

    /**
     * 戳一戳+
     */
    var tap: Set<String> = mutableSetOf("tap", "slap")

    /**
     * 禁言+
     */
    var mute: Set<String> = mutableSetOf("mute", "ban")

    /**
     * 解除禁言+
     */
    var loud: Set<String> = mutableSetOf("loud")

    /**
     * 撤回+
     */
    var withdraw: Set<String> = mutableSetOf("recall", "withdraw", "rec")

    /**
     * 踢出(危险操作, 使用特殊语法)*
     */
    var kick: String = "kick [VA_CODE.QQS] out"

    /**
     * 精华消息+
     */
    var essence: Set<String> = mutableSetOf("essence", "fine")

    /**
     * 群管理+
     */
    var admin: Set<String> = mutableSetOf("groupAdmin", "gad", "admin", "ad")

    /**
     * 副管+
     */
    var deputyAdmin: Set<String> = mutableSetOf("deputyAdmin", "deputyAd", "dad")

    /**
     * 主管+
     */
    var botAdmin: Set<String> = mutableSetOf("botAdmin", "botAd", "bad")

    /**
     * 超管+
     */
    var superAdmin: Set<String> = mutableSetOf("superAdmin", "superAd", "sad")

    /**
     * 主人+
     */
    var botOwner: Set<String> = mutableSetOf("botOwner", "owner")
}
