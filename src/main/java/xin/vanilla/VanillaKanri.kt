package xin.vanilla

import cn.hutool.core.date.BetweenFormatter
import cn.hutool.core.date.DateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin
import net.mamoe.mirai.console.plugin.jvm.reloadPluginConfig
import net.mamoe.mirai.console.plugin.jvm.reloadPluginData
import net.mamoe.mirai.event.GlobalEventChannel
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import xin.vanilla.config.GlobalConfigFile
import xin.vanilla.config.GroupConfigFile
import xin.vanilla.config.TimerDataFile
import xin.vanilla.config.WifeDataFile
import xin.vanilla.enums.DataCacheKey.*
import xin.vanilla.event.EventHandlers
import xin.vanilla.event.TimerMsgEvent
import xin.vanilla.mapper.KeywordData
import xin.vanilla.mapper.MessageCache
import xin.vanilla.mapper.impl.KeywordDataImpl
import xin.vanilla.mapper.impl.MessageCacheImpl
import xin.vanilla.util.Frame
import xin.vanilla.util.VanillaUtils
import xin.vanilla.util.sqlite.SqliteUtil
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

@SuppressWarnings("unused")
object VanillaKanri : KotlinPlugin(
    JvmPluginDescription.loadFromResource()
) {

    // region 变量定义

    var parentMessageId: String = "chatcmpl-72YBVdsjJU4ar30QpQnp4kqN7SXu5"

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
     *
     * 仅内存
     */
    var dataCache: ConcurrentHashMap<String, Any> = ConcurrentHashMap()

    /**
     * 抽老婆数据缓存
     *
     * 持久化
     */
    var wifeData: WifeDataFile = WifeDataFile()

    /**
     * 定时任务数据缓存
     *
     * 持久化
     */
    var timerData: TimerDataFile = TimerDataFile()

    /**
     * 全局设置
     */
    var globalConfig: GlobalConfigFile = GlobalConfigFile()

    /**
     * 群独立设置
     */
    var groupConfig: GroupConfigFile = GroupConfigFile()

    /**
     * 随机数生成器
     */
    var random: SecureRandom = SecureRandom()

    /**
     * 定时任务调度器
     */
    var scheduler: Scheduler = StdSchedulerFactory.getDefaultScheduler()

    // endregion 变量定义

    // region Override

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
            delay(1)
            logger.info("插件在一毫秒前就加载好了！")
        }

        // 记录插件启用时刻
        dataCache[PLUGIN_ENABLE_TIME.getKey()] = System.currentTimeMillis()

        // 注册事件监听
        GlobalEventChannel.registerListenerHost(EventHandlers())

        // 建立自动保存链接
        // 全局配置
        reloadPluginConfig(globalConfig)
        // 群聊配置
        reloadPluginConfig(groupConfig)
        // 抽老婆数据
        reloadPluginData(wifeData)
        // 定时任务数据
        reloadPluginData(timerData)

        // 启动定时任务调度器
        scheduler.start()

        logger.info("加载定时任务")
        if (this.initTimerJob() > 0) {
            logger.info("加载定时任务完成")
        } else {
            logger.info("加载定时任务失败")
        }

    }

    override fun onDisable() {
        // 插件创建的所有线程或异步任务都需要在 onDisable() 时关闭。
        logger.info("插件被禁用了！")
        logger.info("关闭SQLite连接")
        SqliteUtil.closeAll(SqliteUtil.CLOSE_MODE_COMMIT)
        logger.info("关闭定时任务调度器")
        scheduler.shutdown()
        super.onDisable()
    }

    // endregion Override

    // region 延时任务

    fun delayed(delayMillis: Long, runnable: Runnable): CompletableFuture<Void?> {
        return future {
            delay(delayMillis)
            runInterruptible(Dispatchers.IO) { runnable.run() }
            null
        }
    }

    /**
     * 创建延时任务
     */
    fun <R> delayed(delayMillis: Long, callable: Callable<R>): CompletableFuture<R> {
        return future {
            delay(delayMillis)
            runInterruptible(Dispatchers.IO) { callable.call() }
        }
    }

    // endregion 延时任务

    // region 统计相关

    /**
     * 消息发送计数++
     */
    fun addMsgSendCount(): Long {
        val count = getMsgSendCount() + 1
        this.dataCache[PLUGIN_MSG_SEND_COUNT.getKey()] = count
        return count
    }

    /**
     * 获取消息发送计数
     */
    fun getMsgSendCount(): Long {
        val count = this.dataCache[PLUGIN_MSG_SEND_COUNT.getKey()] ?: return 0
        return count as Long
    }

    /**
     * 获取消息接收计数++
     */
    fun addMsgReceiveCount(): Long {
        val count = getMsgReceiveCount() + 1
        this.dataCache[PLUGIN_MSG_RECEIVE_COUNT.getKey()] = count
        return count
    }

    /**
     * 获取消息接收计数
     */
    fun getMsgReceiveCount(): Long {
        val count = this.dataCache[PLUGIN_MSG_RECEIVE_COUNT.getKey()] ?: return 0
        return count as Long
    }

    /**
     * 获取插件运行时长
     */
    fun getRuntimeAsString(): String {
        return DateUtil.formatBetween(
            Date((this.dataCache[PLUGIN_ENABLE_TIME.getKey()] as Long)),
            Date(),
            BetweenFormatter.Level.SECOND
        )
    }

    /**
     * 获取插件运行时长
     */
    fun getRuntimeAsLong(): Long {
        return this.dataCache[PLUGIN_ENABLE_TIME.getKey()] as Long - System.currentTimeMillis()
    }

    /**
     * 获取机器人在线时长
     */
    fun getBotOnlineTimeAsString(bot: Long): String {
        return DateUtil.formatBetween(
            Date((this.dataCache[PLUGIN_BOT_ONLINE_TIME.getKey(bot)] as Long)),
            Date(),
            BetweenFormatter.Level.SECOND
        )
    }

    /**
     * 获取机器人在线时长
     */
    fun getBotOnlineTimeAsLong(bot: Long): Long {
        return this.dataCache[PLUGIN_BOT_ONLINE_TIME.getKey(bot)] as Long - System.currentTimeMillis()
    }

    // endregion 统计相关

    // region 数据/任务初始化

    /**
     * 初始化定时任务数据
     */
    fun initTimerJob(): Int {
        var count = 0
        val timerMap = timerData.getTimer()
        val flatMap = timerMap.values.asSequence().flatMap { it.asSequence() }
        logger.info("定时任务数量: " + flatMap.count())
        logger.info("有效任务数量: " + flatMap
            .filter { o -> !(o.once && o.firstTime < System.currentTimeMillis()) }
            .count())
        logger.info("未初始化数量: " + flatMap
            .filter { o -> !(o.once && o.firstTime < System.currentTimeMillis()) }
            .filter { o -> !o.inited }
            .count())
        for (target in timerMap.keys) {
            val totalSize = timerMap[target]?.size
            // 移除已过期任务
            timerMap[target]?.removeIf { o -> o.once && o.firstTime < System.currentTimeMillis() }
            // 过滤掉已初始化任务
            val filter = timerMap[target]?.filter { o -> !o.inited }
            val validSize = filter?.size
            logger.info(String.format("%s : %s(待初始化)/%s(总数)", target, validSize, totalSize))
            for (timer in filter!!) {
                if (timer.senderNum == 0L) continue

                timer.bot = Bot.getInstanceOrNull(timer.botNum)
                if (timer.bot == null) continue

                timer.sender = Frame.buildPrivateChatContact(timer.bot, timer.senderNum, timer.groupNum, false)
                if (timer.sender == null) continue

                // 构建任务, 装载任务数据
                val jobDataMap = JobDataMap()
                jobDataMap["timer"] = timer
                val jobDetail = JobBuilder.newJob(TimerMsgEvent::class.java)
                    .withIdentity(timer.id, timer.groupNum.toString() + ".job")
                    .usingJobData(jobDataMap)
                    .build()

                // 构建任务触发器
                val triggerEntity = VanillaUtils.buildTriggerFromExp(
                    TriggerKey(timer.id, timer.groupNum.toString() + ".trigger"),
                    timer.cron,
                    !timer.once
                )

                try {
                    scheduler.scheduleJob(jobDetail, triggerEntity.trigger)
                    timer.inited = true
                    logger.info(
                        String.format(
                            "定时任务创建成功: %s - %s - %s",
                            timer.groupNum,
                            timer.id,
                            timer.cron
                        )
                    )
                    count++
                } catch (_: SchedulerException) {
                    logger.warning(
                        String.format(
                            "定时任务创建失败: %s - %s - %s",
                            timer.groupNum,
                            timer.id,
                            timer.cron
                        )
                    )
                }
            }
        }
        return count
    }

    // endregion 数据/任务初始化

}
