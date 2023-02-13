package xin.vanilla.config;

import net.mamoe.mirai.console.data.java.JavaAutoSavePluginData;
import org.jetbrains.annotations.NotNull;

public class PluginData  extends JavaAutoSavePluginData {
    public PluginData(@NotNull String saveName) {
        super(saveName);
    }
}
