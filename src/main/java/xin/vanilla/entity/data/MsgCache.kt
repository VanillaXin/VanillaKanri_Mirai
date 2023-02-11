package xin.vanilla.entity.data

import kotlinx.serialization.Serializable
import lombok.Data
import lombok.experimental.Accessors
import java.util.*

@Data
@Accessors(chain = true)
@Serializable
class MsgCache {
    var id: Int = 0
    var nos = ""
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
    var msg: String = ""
    var ids = intArrayOf()
    var internalIds = intArrayOf()
}
