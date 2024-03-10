package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;

import java.util.HashMap;
import java.util.Map;

/**
 * 抽老婆数据
 */
public class WifeDataFile extends JavaAutoSavePluginData {

    /**
     * 抽老婆记录
     */
    public SerializerAwareValue<Map<String, String>> wife = typedValue("wife",
            createKType(HashMap.class, createKType(String.class), createKType(String.class)));

    public WifeDataFile() {
        super("wife_data");
    }

    public Map<String, String> getWife() {
        return wife.get();
    }

    public void setWife(Map<String, String> wife) {
        this.wife.set(wife);
    }
}
