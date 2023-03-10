package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;

import java.util.HashMap;
import java.util.Map;

/**
 * 插件数据
 */
public class PluginDataFile extends JavaAutoSavePluginData {

    /**
     * 抽老婆记录
     */
    public SerializerAwareValue<Map<String, String>> wife = typedValue("wife",
            createKType(HashMap.class, createKType(String.class), createKType(String.class)));

    public PluginDataFile() {
        super("plugin_data");
    }

    public Map<String, String> getWife() {
        return wife.get();
    }

    public void setWife(Map<String, String> wife) {
        this.wife.set(wife);
    }
}
