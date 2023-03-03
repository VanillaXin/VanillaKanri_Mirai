package xin.vanilla.entity.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.MemberPermission
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.OnlineMessageSource


class GroupMessageEvents {
    private var originalEvent: net.mamoe.mirai.event.events.GroupMessageEvent? = null
    var event: xin.vanilla.entity.event.GroupMessageEvent? = null

    constructor(event: xin.vanilla.entity.event.GroupMessageEvent) {
        this.event = event
    }

    constructor(originalEvent: net.mamoe.mirai.event.events.GroupMessageEvent) {
        this.originalEvent = originalEvent
    }

    fun getMessage(): MessageChain? {
        return if (event != null) event?.message
        else originalEvent?.message
    }

    fun getBot(): Bot? {
        return if (event != null) event?.bot
        else originalEvent?.bot
    }

    fun getTime(): Int {
        return if (event != null) event?.time ?: 0
        else originalEvent?.time ?: 0
    }

    fun getGroup(): Group? {
        return if (event != null) event?.group
        else originalEvent?.group
    }

    fun getSender(): Member? {
        return if (event != null) event?.sender
        else originalEvent?.sender
    }

    fun getSubject(): Group? {
        return if (event != null) event?.subject
        else originalEvent?.subject
    }

    fun getSenderName(): String? {
        return if (event != null) event?.senderName
        else originalEvent?.senderName
    }

    fun getPermission(): MemberPermission? {
        return if (event != null) event?.permission
        else originalEvent?.permission
    }

    fun getSource(): OnlineMessageSource.Incoming.FromGroup? {
        return if (event != null) event?.source
        else originalEvent?.source
    }
}
