package xin.vanilla.entity.event

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource
import net.mamoe.mirai.message.data.source

class GroupMessageEvent(
    val senderName: String,
    /**
     * 发送方权限.
     */
    val permission: MemberPermission,
    /**
     * 发送人. 可能是 [NormalMember] 或 [AnonymousMember]
     */
    val sender: Member,
    val message: MessageChain,
    val time: Int
) {
    val group: Group get() = sender.group
    val bot: Bot get() = sender.bot
    val subject: Group get() = group
    val source: OnlineMessageSource.Incoming.FromGroup get() = message.source as OnlineMessageSource.Incoming.FromGroup
}
