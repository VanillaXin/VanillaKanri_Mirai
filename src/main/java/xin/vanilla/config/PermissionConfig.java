package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;

public class PermissionConfig extends JavaAutoSavePluginConfig {
    public SerializerAwareValue<Long> botOwner = value("botOwner", 0L);


    public PermissionConfig() {
        super("permission");
    }
}
