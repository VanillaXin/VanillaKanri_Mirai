package xin.vanilla.entity.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource


class GroupMessageEvents {
    var originalEvent: net.mamoe.mirai.event.events.GroupMessageEvent? = null
    var customEvent: xin.vanilla.entity.event.GroupMessageEvent? = null

    constructor(event: xin.vanilla.entity.event.GroupMessageEvent) {
        this.customEvent = event
    }

    constructor(originalEvent: net.mamoe.mirai.event.events.GroupMessageEvent) {
        this.originalEvent = originalEvent
    }

    fun getMessage(): MessageChain? {
        return if (customEvent != null) customEvent?.message
        else originalEvent?.message
    }

    fun getBot(): Bot? {
        return if (customEvent != null) customEvent?.bot
        else originalEvent?.bot
    }

    fun getTime(): Int {
        return if (customEvent != null) customEvent?.time ?: 0
        else originalEvent?.time ?: 0
    }

    fun getGroup(): Group? {
        return if (customEvent != null) customEvent?.group
        else originalEvent?.group
    }

    fun getSender(): Member? {
        return if (customEvent != null) customEvent?.sender
        else originalEvent?.sender
    }

    fun getSubject(): Group? {
        return if (customEvent != null) customEvent?.subject
        else originalEvent?.subject
    }

    fun getSenderName(): String? {
        return if (customEvent != null) customEvent?.senderName
        else originalEvent?.senderName
    }

    fun getPermission(): MemberPermission? {
        return if (customEvent != null) customEvent?.permission
        else originalEvent?.permission
    }

    fun getSource(): OnlineMessageSource.Incoming.FromGroup? {
        return if (customEvent != null) customEvent?.source
        else originalEvent?.source
    }
}
