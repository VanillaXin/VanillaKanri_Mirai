package xin.vanilla.entity.config;

import java.util.HashSet;
import java.util.Set;

/**
 * 插件角色
 */
public class Permissions {
    /**
     * 超人
     */
    public Long superOwner = 0L;

    /**
     * 主人
     */
    public Long botOwner = 0L;

    /**
     * 超管
     */
    public Set<Long> superAdmin = new HashSet<>();

    /**
     * 主管
     */
    public Set<Long> botAdmin = new HashSet<>();

    /**
     * 全局副管
     * <p>与群内单独设置的群副管权限相同</p>
     */
    public Set<Long> deputyAdmin = new HashSet<>();


}
