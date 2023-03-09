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

    /**
     * 抽老婆关键词
     */
    var wifePrefix: Set<String> = mutableSetOf("抽老婆")

    /**
     * chatGTP 秘钥
     */
    var chatGPTKey: String = ""

    /**
     * 百度翻译key
     */
    var fanyi_baidu_key:String = ""

    /**
     * 百度翻译appid
     */
    var fanyi_baidu_id:String = ""

    /**
     * python路径
     */
    var pythonPath:String = ""

    /**
     * ，MoGoe路径
     */
    var moeGoePath:String = ""

    /**
     * 语音缓存路径
     */
    var voiceSavePath:String = ""


}
