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
    var bot: Bot,
    var sender: Contact,
    var target: Group? = null,
    var time: Int,
    var msg: MessageChain,
    var repWord: KeyData
)
