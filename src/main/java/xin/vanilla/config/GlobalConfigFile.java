package xin.vanilla.config;

import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;
import xin.vanilla.entity.config.Base;
import xin.vanilla.entity.config.Permissions;
import xin.vanilla.entity.config.instruction.Instructions;

import java.util.HashMap;
import java.util.Map;


/**
 * 插件配置, 存储层
 * <p>
 * 请使用getter/setter
 * <p>
 * TODO 改造getter/setter
 */
@Getter
@Setter
public class GlobalConfigFile extends JavaAutoSavePluginConfig {
    public SerializerAwareValue<String> mc_rcon_ip = value("mc_rcon_ip", "127.0.0.1");
    public SerializerAwareValue<Integer> mc_rcon_port = value("mc_rcon_port", 25575);
    public SerializerAwareValue<String> mc_rcon_psw = value("mc_rcon_psw", "password");

    /**
     * 超人
     */
    public SerializerAwareValue<Long> superOwner = value("superOwner", 0L);

    /**
     * 基础配置
     */
    public SerializerAwareValue<Base> base = typedValue("base", createKType(Base.class));

    /**
     * 权限配置
     * <p>可以考虑 Mirai Console 权限系统</p>
     */
    public SerializerAwareValue<Map<Long, Permissions>> permissions = typedValue("permissions",
            createKType(HashMap.class, createKType(Long.class), createKType(Permissions.class)));

    /**
     * 指令配置
     */
    public SerializerAwareValue<Instructions> instructions = typedValue("instructions", createKType(Instructions.class));

    /**
     * 使用键字符串获取值
     *
     * @param key 例: "instructions.prefix"
     */
    public Object get(String key) {
        return 1;
    }

    public SerializerAwareValue<Map<Long, Permissions>> getPermissions() {
        if (permissions.get().isEmpty()) {
            for (Bot bot : Bot.getInstances()) {
                permissions.get().put(bot.getId(), new Permissions());
            }
        }
        return permissions;
    }

    public GlobalConfigFile() {
        super("global_config");
    }
}
