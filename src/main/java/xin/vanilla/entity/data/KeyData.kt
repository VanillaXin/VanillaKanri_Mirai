package xin.vanilla.entity.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.time
import xin.vanilla.entity.DecodeKeyParam
import xin.vanilla.mapper.impl.KeywordDataImpl.KEYWORD_TYPE_EXACTLY
import xin.vanilla.mapper.impl.KeywordDataImpl.KEYWORD_TYPE_REGEXP
import xin.vanilla.util.StringUtils
import xin.vanilla.util.VanillaUtils
import java.util.regex.Pattern

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
        set(value) {
            field = value
            if (pattern == null && KEYWORD_TYPE_REGEXP.equals(type)) {
                pattern = Pattern.compile(value)
            }
        }

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
    var type: String = KEYWORD_TYPE_EXACTLY
        set(value) {
            field = value
            if (pattern == null && KEYWORD_TYPE_REGEXP.equals(value) && StringUtils.isNotNullOrEmpty(word)) {
                pattern = Pattern.compile(word)
            }
        }

    /**
     * 状态
     */
    var status: Int = 0

    /**
     * 正则对象
     */
    @Transient
    var pattern: Pattern? = null

    /**
     * 获取解Va码后的消息
     *
     * 不执行群管操作
     */
    fun getRepDecode(): String {
        return VanillaUtils.deVanillaCodeRep(this.rep, false)
    }

    /**
     * 获取解Va码后的消息
     *
     * 不执行群管操作
     * @param boolean 是否仅解析非重要特殊码
     */
    fun getRepDecode(boolean: Boolean): String {
        return VanillaUtils.deVanillaCodeRep(this.rep, boolean)
    }

    /**
     * 获取解Va码后的消息
     *
     * 执行群管操作
     */
    fun getRepDecode(group: Group?, bot: Bot, contact: Contact, messageChain: MessageChain): String {
        return VanillaUtils.deVanillaCodeIns(
            DecodeKeyParam(bot, contact, group, messageChain.time, messageChain, this)
        )
    }

    /**
     * 获取解Va码后的消息
     */
    fun getWordDecode(): String {
        return VanillaUtils.deVanillaCodeKey(this.word)
    }
}
