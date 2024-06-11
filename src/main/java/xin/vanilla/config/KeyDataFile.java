package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import xin.vanilla.entity.data.KeyData;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 关键词数据
 */
public class KeyDataFile extends JavaAutoSavePluginData {

    /**
     * 关键词
     */
    public SerializerAwareValue<List<KeyData>> key = typedValue("keyword", createKType(List.class, createKType(KeyData.class)));


    public KeyDataFile() {
        super("key_data");
    }

    public List<KeyData> getKey() {
        return key.get();
    }
}
