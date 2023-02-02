package xin.vanilla.entity.mapper

import kotlinx.serialization.Serializable
import lombok.Data
import lombok.experimental.Accessors

@Data
@Accessors(chain = true)
@Serializable
class MsgCache {
    var id: Int = 0
    var nos: String = ""
    var bot: Long = 0
    var sender: Long = 0
    var target: Long = 0
    var time: Long = 0
    var msg: String = ""
}
