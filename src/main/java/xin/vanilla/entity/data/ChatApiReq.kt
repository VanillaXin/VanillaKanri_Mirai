package xin.vanilla.entity.data

import kotlinx.serialization.Serializable

@Serializable
class ChatApiReq {
    var prompt :String = "" ;

    var options:Map<String, String> = HashMap() ;

    var systemMessage:String = "" ;

}