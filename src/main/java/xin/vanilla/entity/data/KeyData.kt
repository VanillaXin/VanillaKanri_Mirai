package xin.vanilla.entity.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.MessageChain
import xin.vanilla.util.VanillaUtils

/**
 * 关键词数据
 */
@Serializable
class KeyData {
    var id: Int = 0

    /**
     * 关键词
     */
    var word: String = ""

    /**
     * 回复词
     */
    var rep: String = ""

    /**
     * 机器人ID
     */
    var bot: Long = 0

    /**
     * 群号, -1为全局
     */
    var group: Long = 0

    /**
     * 添加时间
     */
    var time: Long = 0

    /**
     * 权级
     */
    var level: Int = 1

    /**
     * 关键词类型
     */
    var type: String = ""

    /**
     * 状态
     */
    var status: Int = 0

    /**
     * 获取解Va码后的消息
     *
     * 不执行群管操作
     */
    fun getRepDecode(): String {
        return VanillaUtils.deVanillaCodeRep(this.rep)
    }

    /**
     * 获取解Va码后的消息
     *
     * 执行群管操作
     */
    fun getRepDecode(group: Group, bot: Bot, contact: Contact, messageChain: MessageChain): String {
        return VanillaUtils.deVanillaCodeIns(this.word, this.rep, bot, group, contact, messageChain)
    }

    /**
     * 获取解Va码后的消息
     */
    fun getWordDecode(): String {
        return VanillaUtils.deVanillaCodeKey(this.word)
    }
}
