package xin.vanilla.entity

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.message.data.MessageChain
import xin.vanilla.entity.data.KeyData

/**
 * 封装解析关键词时会用到的各种信息
 */
class DecodeKeyParam(
    /**
     * 机器人
     */
    var bot: Bot,
    /**
     * 消息发送人
     */
    var sender: Contact,
    /**
     * 来源群
     */
    var group: Group? = null,
    /**
     * 发送时间
     */
    var time: Int,
    /**
     * 消息发送人发送的消息
     */
    var msg: MessageChain? = null,
    /**
     * 触发的关键词数据
     */
    var repWord: KeyData
)
