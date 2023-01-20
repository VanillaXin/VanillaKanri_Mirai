package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;
import xin.vanilla.entity.config.Base;
import xin.vanilla.entity.config.Permissions;
import xin.vanilla.entity.config.instruction.Instructions;

import java.util.Map;
import java.util.Set;

/**
 * 插件配置, 存储层
 * <p>
 * 变量命名请与实体名一致
 */
public class GlobalConfigFile extends JavaAutoSavePluginConfig {
    public SerializerAwareValue<String> mc_rcon_ip = value("mc_rcon_ip", "127.0.0.1");
    public SerializerAwareValue<Integer> mc_rcon_port = value("mc_rcon_port", 25575);
    public SerializerAwareValue<String> mc_rcon_psw = value("mc_rcon_psw", "password");

    /**
     * 基础配置
     */
    public SerializerAwareValue<Base> BASE = typedValue("base", createKType(Base.class));

    /**
     * 权限配置
     * <p>可以考虑 Mirai Console 权限系统</p>
     */
    public SerializerAwareValue<Permissions> PERMISSIONS = typedValue("permissions", createKType(Permissions.class));

    /**
     * 指令配置
     */
    public SerializerAwareValue<Instructions> INSTRUCTIONS = typedValue("instructions", createKType(Instructions.class));

    /**
     * 使用键字符串获取值
     *
     * @param key 例: "instructions.prefix"
     */
    public Object get(String key) {
        return 1;
    }

    public GlobalConfigFile() {
        super("global_config");
    }
}
