package xin.vanilla.entity.data

import kotlinx.serialization.Serializable

@Serializable
class KeyData {
    var id: Int = 0
    var word: String = ""
    var msg: String = ""
    var bot: Long = 0
    var group: Long = 0
    var time: Long = 0
    var level: Int = 1
}
