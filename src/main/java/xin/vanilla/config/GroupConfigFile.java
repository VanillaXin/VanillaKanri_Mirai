package xin.vanilla.config;

import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class GroupConfigFile extends JavaAutoSavePluginConfig {
    /**
     * 群副管列表
     */
    public SerializerAwareValue<Map<Long, Set<Long>>> deputyAdmin = typedValue("deputyAdmin",
            createKType(HashMap.class, createKType(Long.class), createKType(Set.class, createKType(Long.class))));


    public GroupConfigFile() {
        super("group_config");
    }
}
