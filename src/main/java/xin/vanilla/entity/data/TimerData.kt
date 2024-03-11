package xin.vanilla.entity.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact

/**
 * 定时任务数据
 */
@Serializable
class TimerData {

    /**
     * 目标对象
     */
    @Transient
    var sender: Contact? = null

    /**
     * 机器人
     */
    @Transient
    var bot: Bot? = null

    /**
     * 唯一标识
     */
    var id: String = ""

    /**
     * 机器人
     */
    var botNum: Long = 0

    /**
     * 消息来源群号 不存在则为私聊
     */
    var groupNum: Long = 0

    /**
     * 消息发送者
     */
    var senderNum: Long = 0

    /**
     * 表达式
     */
    var cron: String = ""

    /**
     * 仅执行一次
     */
    var once: Boolean = false

    /**
     * 任务内容
     */
    var msg: String = ""

    /**
     * 首次执行时间
     */
    var firstTime: Long = 0

}
