package xin.vanilla;

import net.mamoe.mirai.console.extension.PluginComponentStorage;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.config.GlobalConfigFile;
import xin.vanilla.config.GroupConfigFile;
import xin.vanilla.event.EventHandlers;
import xin.vanilla.mapper.MessageCache;
import xin.vanilla.mapper.impl.MessageCacheImpl;
import xin.vanilla.util.sqlite.SqliteUtil;

@SuppressWarnings("unused")
public final class VanillaKanri extends JavaPlugin {
    public static final VanillaKanri INSTANCE = new VanillaKanri();
    public final MessageCache messageCache = new MessageCacheImpl(getDataFolderPath().toString());
    /**
     * 全局设置
     */
    public final GlobalConfigFile globalConfig = new GlobalConfigFile();
    /**
     * 群独立设置
     */
    public final GroupConfigFile groupConfig = new GroupConfigFile();

    private VanillaKanri() {
        // 不几道为啥找不到配置文件
        // super(JvmPluginDescription.loadFromResource());
        super(new JvmPluginDescriptionBuilder("xin.vanilla.vanilla-kanri", "2.0.0")
                .name("香草群管")
                .author("VanillaXin")
                .build());
    }

    @Override
    public void onLoad(@NotNull PluginComponentStorage $this$onLoad) {
        super.onLoad($this$onLoad);
    }

    @Override
    public void onEnable() {
        // 若要执行简单的异步、延迟、重复任务，可使用 getScheduler() 获取到简单任务调度器。例子:
        // 延时
        getScheduler().delayed(1000L, () -> getLogger().info("Plugin loaded a second ago!"));

        // 注册事件监听
        GlobalEventChannel.INSTANCE.registerListenerHost(new EventHandlers());

        // 建立自动保存链接
        // 全局配置
        reloadPluginConfig(globalConfig);
        // 群聊配置
        reloadPluginConfig(groupConfig);
    }

    @Override
    public void onDisable() {
        // 插件创建的所有线程或异步任务都需要在 onDisable() 时关闭。
        getLogger().info("Plugin disabled!");
        // 关闭SQLite连接
        SqliteUtil.closeAll(SqliteUtil.CLOSE_MODE_COMMIT);
        super.onDisable();
    }
}
