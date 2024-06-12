package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import xin.vanilla.entity.data.KeyData;

import java.util.ArrayList;

/**
 * 关键词数据
 */
public class KeyDataFile extends JavaAutoSavePluginData {

    /**
     * 关键词
     */
    public SerializerAwareValue<ArrayList<KeyData>> key = typedValue("keyword", createKType(ArrayList.class, createKType(KeyData.class)));


    public KeyDataFile() {
        super("key_data");
    }

    public ArrayList<KeyData> getKey() {
        return key.get();
    }
}
