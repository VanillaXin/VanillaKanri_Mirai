package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 群聊配置
 * <p>
 * 请使用getter/setter
 */
public class GroupConfigFile extends JavaAutoSavePluginConfig {
    /**
     * 群副管列表
     */
    public SerializerAwareValue<Map<Long, Set<Long>>> deputyAdmin = typedValue("deputyAdmin",
            createKType(HashMap.class, createKType(Long.class), createKType(Set.class, createKType(Long.class))));


    public GroupConfigFile() {
        super("group_config");
    }

    public Map<Long, Set<Long>> getDeputyAdmin() {
        return deputyAdmin.get();
    }

    public Set<Long> getDeputyAdmin(Long group) {
        if (deputyAdmin.get().isEmpty() || !deputyAdmin.get().containsKey(group))
            return new HashSet<>();
        return deputyAdmin.get().get(group);
    }

    public void setDeputyAdmin(Map<Long, Set<Long>> deputyAdmin) {
        this.deputyAdmin.set(deputyAdmin);
    }

    public void setDeputyAdmin(Long group, Set<Long> deputyAdmin) {
        this.deputyAdmin.get().put(group, deputyAdmin);
    }
}
