package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import xin.vanilla.entity.data.KeyData;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 关键词数据
 */
public class KeyDataFile extends JavaAutoSavePluginData {

    /**
     * 关键词
     */
    public SerializerAwareValue<CopyOnWriteArrayList<KeyData>> key = typedValue("keyword", createKType(CopyOnWriteArrayList.class, createKType(KeyData.class)));


    public KeyDataFile() {
        super("key_data");
    }

    public CopyOnWriteArrayList<KeyData> getKey() {
        return key.get();
    }
}
