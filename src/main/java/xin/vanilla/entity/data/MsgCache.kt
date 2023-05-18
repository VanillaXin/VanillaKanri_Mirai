package xin.vanilla.entity.data

import kotlinx.serialization.Serializable
import xin.vanilla.util.VanillaUtils
import java.util.*

/**
 * 消息缓存数据
 */
@Serializable
class MsgCache {
    var id: Int = 0
    var nos: String = ""
        set(value) {
            field = value
            ids =
                Arrays.stream(
                    value.substring(0, value.indexOf("|")).split(",".toRegex()).dropLastWhile { it.isEmpty() }
                        .toTypedArray()
                ).mapToInt { s: String -> s.toInt() }.toArray()
            internalIds = Arrays.stream(
                value.substring(value.indexOf("|") + 1).split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            ).mapToInt { s: String -> s.toInt() }.toArray()
        }
    var bot: Long = 0
    var sender: Long = 0
    var target: Long = 0
    var time: Long = 0
    var msgJson: String = ""
    var msgText: String = ""
        get() {
            return field.ifEmpty {
                val jsonToText = VanillaUtils.jsonToText(msgJson)
                msgText = jsonToText
                jsonToText
            }
        }
    var ids: IntArray = intArrayOf()
    var internalIds: IntArray = intArrayOf()
}
