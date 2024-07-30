package xin.vanilla.entity.data

import kotlinx.serialization.Serializable

/**
 * 定时任务数据
 */
@Serializable
class McData {

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
     * 服务器IP
     */
    var ip: String = ""

    /**
     * 服务器端口
     */
    var port: Short = 25565

    /**
     * 服务器分组名称
     */
    var cluster: String = "Minecraft服务器($ip$port)"

    /**
     * 服务器名称
     */
    var name: String = "Minecraft服务器($ip$port)"

    /**
     * 服务器RCON密码
     */
    var rconPwd: String = ""

}
