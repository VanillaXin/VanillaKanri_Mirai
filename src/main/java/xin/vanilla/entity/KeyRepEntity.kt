package xin.vanilla.entity

import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChainBuilder

@Serializable
class KeyRepEntity {
    /**
     * 回复内容
     */
    var rep: Message = MessageChainBuilder().build()

    /**
     * 延时时间(毫秒)
     *
     * 不应超过 1000*60*10
     */
    var delayMillis: Int = 0
        get() {
            return if (field > 1000 * 60 * 10) 1000 * 60 * 10 else field
        }

}
