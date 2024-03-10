package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import xin.vanilla.entity.data.TimerData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 定时任务数据
 */
public class TimerDataFile extends JavaAutoSavePluginData {

    /**
     * 定时任务记录
     */
    public SerializerAwareValue<Map<Long, List<TimerData>>> timer = typedValue("timer",
            createKType(HashMap.class, createKType(Long.class), createKType(List.class, createKType(TimerData.class))));

    public TimerDataFile() {
        super("timer_data");
    }

    public Map<Long, List<TimerData>> getTimer() {
        return timer.get();
    }

    public void setTimer(Map<Long, List<TimerData>> timer) {
        this.timer.set(timer);
    }
}
