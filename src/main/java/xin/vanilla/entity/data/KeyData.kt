package xin.vanilla.entity.data

import kotlinx.serialization.Serializable
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
    var msg: String = ""

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
     * 权级
     */
    var status: Int = 0

    /**
     * 获取解Va码后的消息
     */
    fun getMsgDecode(): String {
        return VanillaUtils.deVanillaCodeMsg(this.msg)
    }
}
