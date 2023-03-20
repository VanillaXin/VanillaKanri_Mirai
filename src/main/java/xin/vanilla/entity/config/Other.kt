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
     * chatGPT url
     */
    var chatGPTUrl: String = ""

    /**
     * chatGPT 秘钥
     */
    var chatGPTKey: String = ""

    /**
     * chatGPT 预设语境
     */
    var chatGPTContext: String = ""

    /**
     * chatGPT 请求失败后的默认回复
     */
    var chatGPTDefaultBack: String = ""

    /**
     * 百度翻译key
     */
    var translateBaiduKey: String = ""

    /**
     * 百度翻译appid
     */
    var translateBaiduId: String = ""

    /**
     * python路径
     */
    var pythonPath: String = ""

    /**
     * MoGoe路径
     */
    var moeGoePath: String = ""

    /**
     * 语音缓存路径
     */
    var voiceSavePath: String = ""

    /**
     * ai绘画url
     */
    var aiDrawUrl: String = ""

    /**
     * ai绘画认证 如果没设置就没有
     */
    var aiDrawKey: String = ""

    /**
     * 系统代理地址
     */
    var systemProxyHost: String = ""

    /**
     * 系统代理端口
     */
    var systemProxyPort: Int = 0

}
