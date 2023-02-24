package xin.vanilla.entity.data

import kotlinx.serialization.Serializable
import lombok.Data
import lombok.Getter
import lombok.Setter
import lombok.experimental.Accessors
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.message.data.MessageChain

@Data
@Accessors(chain = true)
@Serializable
class ForwardMsg {
    var preview: List<String> = ArrayList()
    var title: String = String()
    var brief: String = String()
    var source: String = String()
    var summary: String = String()

    var nodeList: List<Node> = ArrayList()
    var user: Long = 0
    var group: Long = 0
    var bot: Long = 0
}

@Data
@Accessors(chain = true)
@Serializable
class Node {
    /**
     * 发送人 [User.id]
     */
    var senderId: Long = 0

    /**
     * 时间戳 秒
     */
    var time: Int = 0

    /**
     * 发送人昵称
     */
    var senderName: String = ""

    /**
     * 消息内容
     */
    var messageChain: MessageChain? = null
}