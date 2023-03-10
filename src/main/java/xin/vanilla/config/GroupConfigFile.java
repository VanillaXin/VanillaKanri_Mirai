package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;
import xin.vanilla.entity.config.Base;

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

    /**
     * 基础设置
     */
    public SerializerAwareValue<Map<Long, Base>> base = typedValue("base",
            createKType(HashMap.class, createKType(Long.class), createKType(Base.class)));

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

    public Map<Long, Base> getBase() {
        return base.get();
    }

    public Base getBase(Long group) {
        if (base.get().isEmpty() || !base.get().containsKey(group))
            return new Base();
        return base.get().get(group);
    }

    public void setBase(Map<Long, Base> base) {
        this.base.set(base);
    }

    public void setBase(Long group, Base base) {
        this.base.get().put(group, base);
    }

}
