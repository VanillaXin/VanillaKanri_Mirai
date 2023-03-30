package xin.vanilla.entity.data

import kotlinx.serialization.Serializable


@Serializable
class ChatApiResp {
    var role: String = "";

    var id: String = ""

    var parentMessageId: String = ""

    var text: String = ""

    var detail: String = ""

}