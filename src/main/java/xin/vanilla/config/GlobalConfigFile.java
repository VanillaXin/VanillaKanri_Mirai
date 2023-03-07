package xin.vanilla.config;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.data.PluginDataHolder;
import net.mamoe.mirai.console.data.PluginDataStorage;
import net.mamoe.mirai.console.data.SerializerAwareValue;
import net.mamoe.mirai.console.data.java.JavaAutoSavePluginConfig;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.entity.config.Base;
import xin.vanilla.entity.config.Other;
import xin.vanilla.entity.config.Permissions;
import xin.vanilla.entity.config.instruction.Instructions;
import xin.vanilla.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * 插件配置
 * <p>
 * 请使用getter/setter
 */
public class GlobalConfigFile extends JavaAutoSavePluginConfig {

    /**
     * 超人
     */
    public SerializerAwareValue<Long> superOwner = value("superOwner", 196468986L);

    /**
     * 基础配置
     */
    public SerializerAwareValue<Base> base = typedValue("base", createKType(Base.class));

    /**
     * 杂项配置
     */
    public SerializerAwareValue<Other> other = typedValue("other", createKType(Other.class));

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

    public GlobalConfigFile() {
        super("global_config");
    }

    @Override
    public void onInit(@NotNull PluginDataHolder owner, @NotNull PluginDataStorage storage) {
        super.onInit(owner, storage);
    }

    /**
     * 刷新二级前缀
     */
    public void refreshSecondaryPrefix() {
        Set<String> secondaryPrefix = new HashSet<>();
        // if (!StringUtils.isNullOrEmpty(this.instructions.get().getPrefix())) {
        //     this.instructions.get().setSecondaryPrefix(secondaryPrefix);
        //     return;
        // }

        if (!StringUtils.isNullOrEmpty(this.instructions.get().getKanri().getPrefix())) {
            secondaryPrefix.add(this.instructions.get().getKanri().getPrefix());
        } else {
            for (Field field : this.instructions.get().getKanri().getClass().getFields()) {
                try {
                    if (field.getType() == String.class
                            && !field.getName().equals("kick")
                            && !StringUtils.isNullOrEmpty((String) field.get(this.instructions.get().getKanri())))
                        secondaryPrefix.add((String) field.get(this.instructions.get().getKanri()));
                    else if (field.getType() == Set.class) {
                        secondaryPrefix.addAll(
                                ((Set<?>) field.get(this.instructions.get().getKanri())).stream()
                                        .map(Object::toString)
                                        .collect(Collectors.toList())
                        );
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (!StringUtils.isNullOrEmpty(this.instructions.get().getKeyword().getPrefix())) {
            secondaryPrefix.add(this.instructions.get().getKeyword().getPrefix());
        } else {
            for (Field field : this.instructions.get().getKeyword().getClass().getFields()) {
                try {
                    if (field.getType() == String.class
                            && !StringUtils.isNullOrEmpty((String) field.get(this.instructions.get().getKeyword())))
                        secondaryPrefix.add((String) field.get(this.instructions.get().getKeyword()));
                    else if (field.getType() == Set.class) {
                        secondaryPrefix.addAll(
                                ((Set<?>) field.get(this.instructions.get().getKeyword())).stream()
                                        .map(Object::toString)
                                        .collect(Collectors.toList())
                        );
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (!StringUtils.isNullOrEmpty(this.instructions.get().getTimed().getPrefix())) {
            secondaryPrefix.add(this.instructions.get().getTimed().getPrefix());
        } else {
            for (Field field : this.instructions.get().getTimed().getClass().getFields()) {
                try {
                    if (field.getType() == String.class
                            && !StringUtils.isNullOrEmpty((String) field.get(this.instructions.get().getTimed())))
                        secondaryPrefix.add((String) field.get(this.instructions.get().getTimed()));
                    else if (field.getType() == Set.class) {
                        secondaryPrefix.addAll(
                                ((Set<?>) field.get(this.instructions.get().getTimed())).stream()
                                        .map(Object::toString)
                                        .collect(Collectors.toList())
                        );
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        this.instructions.get().setSecondaryPrefix(secondaryPrefix);
    }

    /**
     * 使用键字符串获取值
     *
     * @param key 例: "instructions.prefix"
     */
    public Object get(String key) {
        return 1;
    }

    public Long getSuperOwner() {
        return superOwner.get();
    }

    public void setSuperOwner(Long superOwner) {
        this.superOwner.set(superOwner);
    }

    public Other getOther() {
        return other.get();
    }

    public void setBase(Other other) {
        this.other.set(other);
    }

    public Base getBase() {
        return base.get();
    }

    public void setBase(Base base) {
        this.base.set(base);
    }

    public Map<Long, Permissions> getPermissions() {
        if (permissions.get().isEmpty()) {
            for (Bot bot : Bot.getInstances()) {
                permissions.get().put(bot.getId(), new Permissions());
            }
        }
        return permissions.get();
    }

    public Permissions getPermissions(Long bot) {
        if (permissions.get().isEmpty() || !permissions.get().containsKey(bot)) {
            return new Permissions();
        }
        return permissions.get().get(bot);
    }

    public void setPermissions(Map<Long, Permissions> permissions) {
        this.permissions.set(permissions);
    }

    public void setPermissions(Long bot, Permissions permissions) {
        this.permissions.get().put(bot, permissions);
    }

    public Instructions getInstructions() {
        return instructions.get();
    }

    public void setInstructions(Instructions instructions) {
        this.instructions.set(instructions);
    }

}
