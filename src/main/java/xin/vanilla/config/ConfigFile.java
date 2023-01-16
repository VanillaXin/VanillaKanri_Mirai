package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;

import java.util.Map;
import java.util.Set;

/**
 * 插件配置, 存储层
 */
public class ConfigFile extends JavaAutoSavePluginConfig {
    public SerializerAwareValue<String> testString = value("testString", "测试");
    public SerializerAwareValue<Integer> testInt = value("testInt", 0);

    /**
     * 基础配置
     */
    public SerializerAwareValue<Map<String, Set<String>>> base = typedValue("base",
            createKType(Map.class, createKType(String.class), createKType(Set.class, createKType(String.class))));

    /**
     * 权限配置
     * <p>可以考虑 Mirai Console 权限系统</p>
     */
    public SerializerAwareValue<Map<String, Set<String>>> permission = typedValue("permission",
            createKType(Map.class, createKType(String.class), createKType(Set.class, createKType(String.class))));

    /**
     * 指令配置
     */
    public SerializerAwareValue<Map<String, Set<String>>> instruction = typedValue("instruction",
            createKType(Map.class, createKType(String.class), createKType(Set.class, createKType(String.class))));

    public ConfigFile set(String key, Object value) {

        return this;
    }

    public Object get(String key) {
        return 1;
    }

    public ConfigFile() {
        super("config");
    }
}
