package xin.vanilla

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.jvm.reloadPluginConfig
import net.mamoe.mirai.event.GlobalEventChannel
import xin.vanilla.config.GlobalConfigFile
import xin.vanilla.config.GroupConfigFile
import xin.vanilla.event.EventHandlers
import xin.vanilla.mapper.KeywordData
import xin.vanilla.mapper.MessageCache
import xin.vanilla.mapper.impl.KeywordDataImpl
import xin.vanilla.mapper.impl.MessageCacheImpl
import xin.vanilla.util.sqlite.SqliteUtil
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@SuppressWarnings("unused")
object VanillaKanri : KotlinPlugin(
    JvmPluginDescription.loadFromResource()
) {
    /**
     * 消息缓存
     */
    var messageCache: MessageCache = MessageCacheImpl(dataFolderPath.toString())

    /**
     * 关键词数据
     */
    var keywordData: KeywordData = KeywordDataImpl(dataFolderPath.toString())

    /**
     * 数据缓存
     */
    var dataCache: ConcurrentHashMap<String, Any> = ConcurrentHashMap()

    /**
     * 全局设置
     */
    var globalConfig: GlobalConfigFile = GlobalConfigFile()

    /**
     * 群独立设置
     */
    var groupConfig: GroupConfigFile = GroupConfigFile()

    /**
     * 插件启用事件
     */
    override fun onEnable() {
        // 若要执行简单的异步、延迟、重复任务，可使用
        // Java: JavaPluginScheduler().delayed(1000L, () -> {})
        // Kotlin: launch {
        //             delay(1000)
        //             println("一秒钟过去了。")
        //         }
        // 获取到简单任务调度器。例子:
        // 延时
        launch {
            delay(1000)
            logger.info("Plugin loaded a second ago!")
        }

        // 注册事件监听
        GlobalEventChannel.registerListenerHost(EventHandlers())

        // 建立自动保存链接
        // 全局配置
        reloadPluginConfig(globalConfig)
        // 群聊配置
        reloadPluginConfig(groupConfig)
    }

    override fun onDisable() {
        // 插件创建的所有线程或异步任务都需要在 onDisable() 时关闭。
        logger.info("Plugin disabled!")
        // 关闭SQLite连接
        SqliteUtil.closeAll(SqliteUtil.CLOSE_MODE_COMMIT)
        super.onDisable()
    }

    fun delayed(delayMillis: Long, runnable: Runnable): CompletableFuture<Void?> {
        return future {
            delay(delayMillis)
            runInterruptible(Dispatchers.IO) {
                runnable.run()
            }
            null
        }
    }

    fun <R> delayed(delayMillis: Long, callable: Callable<R>): CompletableFuture<R> {
        return future {
            delay(delayMillis)
            runInterruptible(Dispatchers.IO) { callable.call() }
        }
    }
}
