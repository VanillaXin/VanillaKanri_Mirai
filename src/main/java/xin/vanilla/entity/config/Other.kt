package xin.vanilla.entity.config

import kotlinx.serialization.Serializable

/**
 * 其他配置
 *
 * 比较杂的设置配置都丢这里
 */
@Serializable
class Other {
    /**
     * 涩图路径
     */
    var hentaiPath: String = ""

    /**
     * MC RCON 配置
     *
     * IP地址
     */
    var mcRconIp: String = "127.0.0.1"

    /**
     * MC RCON 配置
     *
     * 端口
     */
    var mcRconPort: Int = 25575

    /**
     * MC RCON 配置
     *
     * 密码
     */
    var mcRconPsw: String = "password"

}
