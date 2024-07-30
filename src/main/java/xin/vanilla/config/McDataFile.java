package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import net.mamoe.mirai.console.internal.data.CollectionUtilKt;
import xin.vanilla.entity.data.McData;
import xin.vanilla.util.CollectionUtils;

import java.util.List;

/**
 * MC服务器数据
 */
public class McDataFile extends JavaAutoSavePluginData {

    /**
     * MC服务器记录
     */
    public SerializerAwareValue<List<McData>> mc = typedValue("wife",
            createKType(List.class, createKType(McData.class)));

    public McDataFile() {
        super("mc_data");
    }

    public List<McData> getMc() {
        return mc.get();
    }

    public void setMc(List<McData> mc) {
        this.mc.set(mc);
    }
}
