package xin.vanilla.entity.data

import kotlinx.serialization.Serializable

@Serializable
class ChatApiReq {
    var prompt :String = "" ;

    var options:String = "" ;

    var systemMessage:String = "" ;

}