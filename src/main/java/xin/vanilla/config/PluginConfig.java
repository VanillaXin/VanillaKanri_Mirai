package xin.vanilla.config;

import net.mamoe.mirai.console.data.SerializerAwareValue;
import xin.vanilla.entity.config.Base;
import xin.vanilla.entity.config.Permissions;
import xin.vanilla.entity.config.instruction.BaseInstructions;
import xin.vanilla.entity.config.instruction.Instructions;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.config.instruction.KeywordInstructions;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 插件配置数据
 *
 * <p>source 为链接的自动保存配置文件类, 更新后会定时自动保存至文件</p>
 * 请只在修改配置时修改 source 中的值
 */
@Deprecated
public class PluginConfig {
    // TODO 将该类内容迁移至*ConfigFile类

    /**
     * 可自动保存的源配置信息
     * <p>
     * 请避免直接使用该对象
     */
    public GlobalConfigFile SOURCE = new GlobalConfigFile();

    /**
     * 插件基础设置
     */
    public Base BASE = new Base();

    /**
     * 插件指令设置
     */
    public Instructions INSTRUCTIONS = new Instructions();

    /**
     * 插件角色列表
     */
    public Permissions PERMISSIONS = new Permissions();

    /**
     * 刷新源配置信息
     * <p>
     * PluginConfig -> ConfigFile
     */
    public boolean refreshSource() {
        for (Field field : this.getClass().getFields()) {
            // TODO 考虑使用注解判断
            if (field.getType().getName().contains("xin.vanilla.entity.config")) {
                if (!refreshSource(field.getClass())) return false;
            }
        }
        return true;
    }

    /**
     * 刷新某个源配置信息
     * <p>
     * PluginConfig -> ConfigFile
     */
    public <T> boolean refreshSource(Class<T> clazz) {
        return refreshSource(clazz, null);
    }

    /**
     * 刷新某个源配置的具体节点信息
     * <p>
     * PluginConfig -> ConfigFile
     */
    public <T> boolean refreshSource(Class<T> clazz, String key) {
        try {
            for (Field field : this.getClass().getFields()) {
                if (field.getType() == clazz) {
                    for (Field field1 : BASE.getClass().getFields())
                        if (clazz.getName().equalsIgnoreCase(field1.getName()))
                            putSourceMap(
                                    ((SerializerAwareValue<Map<String, Set<String>>>) field1.get(SOURCE)).get()
                                    , field.get(this)
                                    , key
                            );
                }
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 初始化插件配置
     * <p>
     * ConfigFile(Not empty) -> PluginConfig
     */
    public boolean init() {
        // 初始化基础设置
        if (!initBaseConfig()) return false;
        // 初始化角色配置
        if (!initPermissions()) return false;
        // 初始化指令配置
        if (!initInstructions()) return false;


        return true;
    }

    /**
     * 初始化基础设置
     */
    public boolean initBaseConfig() {
        // try {
        //     BASE = new Base();
        //
        //     Map<String, Set<String>> baseMap = SOURCE.base.get();
        //     if (baseMap.isEmpty()) {
        //         putSourceMap(baseMap, BASE);
        //     } else {
        //         getSourceMap(baseMap, BASE);
        //     }
        // } catch (Exception e) {
        //     return false;
        // }
        return true;
    }

    /**
     * 初始化角色列表
     */
    public boolean initPermissions() {
        // try {
        //     PERMISSIONS = new Permissions();
        //
        //     // 配置文件中的权限map
        //     Map<String, Set<String>> permissionMap = SOURCE.permissions.get();
        //     if (permissionMap.isEmpty()) {
        //         // 如果配置文件为空则初始化插件配置文件中角色配置
        //         putSourceMap(permissionMap, PERMISSIONS);
        //     } else {
        //         // 否则读取配置文件中配置并赋值给插件角色对象
        //         getSourceMap(permissionMap, PERMISSIONS);
        //     }
        // } catch (Exception e) {
        //     e.printStackTrace();
        //     return false;
        // }
        return true;
    }

    /**
     * 初始化指令列表
     */
    public boolean initInstructions() {
        // try {
        //     INSTRUCTIONS = new Instructions();
        //
        //     // 配置文件中的指令map
        //     Map<String, Set<String>> instructionMap = SOURCE.instructions.get();
        //     // 插件角色对象变量列表
        //     if (instructionMap.isEmpty()) {
        //         // 如果配置文件为空则初始化文件
        //         putSourceMap(instructionMap, INSTRUCTIONS);
        //     } else {
        //         // 否则读取配置文件中配置
        //         getSourceMap(instructionMap, INSTRUCTIONS);
        //     }
        // } catch (Exception e) {
        //     return false;
        // }
        return true;
    }

    /**
     * 初始化群设置
     */
    public boolean initGroupConfig() {
        try {

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 根据实体对象中的值初始化配置源文件中配置
     *
     * @param sourceMap 配置源文件MAP
     */
    private <T> void putSourceMap(Map<String, Set<String>> sourceMap, T entity) throws IllegalAccessException {
        putSourceMap(sourceMap, entity, null);
    }

    /**
     * 根据实体对象中的值初始化配置源文件中配置
     *
     * @param sourceMap 配置源文件MAP
     */
    private <T> void putSourceMap(Map<String, Set<String>> sourceMap, T entity, String key) throws IllegalAccessException {
        Field[] fields = entity.getClass().getFields();
        for (Field field : fields) {
            if (null != key && !"".equals(key) && !field.getName().equals(key)) continue;
            if (field.getType() == Set.class) {
                sourceMap.put(field.getName(), new HashSet<String>() {{
                    addAll(((Set<?>) field.get(entity)).stream().map(Object::toString).collect(Collectors.toList()));
                }});
            } else if (field.getType() == BaseInstructions.class) {
                putSourceMap(sourceMap, field.get(entity), key);
            } else if (field.getType() == KeywordInstructions.class) {
                putSourceMap(sourceMap, field.get(entity), key);
            } else if (field.getType() == KanriInstructions.class) {
                putSourceMap(sourceMap, field.get(entity), key);
            } else {
                sourceMap.put(field.getName(), new HashSet<String>() {{
                    add(field.get(entity).toString());
                }});
            }
        }
    }

    /**
     * 将配置源文件中数据映射至实体对象
     * <p><p>
     * 允许的类型:
     * <p>1. Long、Integer、Float、Double、String、Boolean;</p>
     * <p>2. Set&lt;Long&gt;、Set&lt;Integer&gt;、Set&lt;Float&gt;、Set&lt;Double&gt;、Set&lt;String&gt;;</p>
     * <p>3. 仅包含上述 1 与 2 的 实体类.</p>
     *
     * @param sourceMap 配置源文件MAP
     */
    private <T> void getSourceMap(Map<String, Set<String>> sourceMap, T entity) throws IllegalAccessException {
        Field[] fields = entity.getClass().getFields();
        for (Field field : fields) {
            if (sourceMap.containsKey(field.getName())) {
                // 如果是Set集合
                if (field.getType() == Set.class) {
                    if (field.getGenericType().getTypeName().contains("java.lang.Long")) {
                        field.set(entity, sourceMap.get(field.getName()).stream()
                                .map(Long::parseLong)
                                .collect(Collectors.toSet()));
                    } else if (field.getGenericType().getTypeName().contains("java.lang.Integer")) {
                        field.set(entity, sourceMap.get(field.getName()).stream()
                                .map(Integer::parseInt)
                                .collect(Collectors.toSet()));
                    } else if (field.getGenericType().getTypeName().contains("java.lang.Float")) {
                        field.set(entity, sourceMap.get(field.getName()).stream()
                                .map(Float::parseFloat)
                                .collect(Collectors.toSet()));
                    } else if (field.getGenericType().getTypeName().contains("java.lang.Double")) {
                        field.set(entity, sourceMap.get(field.getName()).stream()
                                .map(Double::parseDouble)
                                .collect(Collectors.toSet()));
                    } else if (field.getGenericType().getTypeName().contains("java.lang.Boolean")) {
                        field.set(entity, sourceMap.get(field.getName()).stream()
                                .map(Boolean::parseBoolean)
                                .collect(Collectors.toSet()));
                    } else {
                        field.set(entity, sourceMap.get(field.getName()));
                    }
                } else if (field.getType() == BaseInstructions.class) {
                    BaseInstructions baseInstructions = new BaseInstructions();
                    getSourceMap(sourceMap, baseInstructions);
                    field.set(entity, baseInstructions);
                } else if (field.getType() == KeywordInstructions.class) {
                    KeywordInstructions keywordInstructions = new KeywordInstructions();
                    getSourceMap(sourceMap, keywordInstructions);
                    field.set(entity, keywordInstructions);
                } else if (field.getType() == KanriInstructions.class) {
                    KanriInstructions kanriInstructions = new KanriInstructions();
                    getSourceMap(sourceMap, kanriInstructions);
                    field.set(entity, kanriInstructions);
                } else {
                    if (field.getGenericType().getTypeName().contains("java.lang.Long")) {
                        field.set(entity, sourceMap.get(field.getName()).stream()
                                .limit(1)
                                .map(Long::parseLong)
                                .collect(Collectors.toList())
                                .get(0));
                    } else if (field.getGenericType().getTypeName().contains("java.lang.Integer")) {
                        field.set(entity, sourceMap.get(field.getName()).stream()
                                .limit(1)
                                .map(Integer::parseInt)
                                .collect(Collectors.toList())
                                .get(0));
                    } else if (field.getGenericType().getTypeName().contains("java.lang.Float")) {
                        field.set(entity, sourceMap.get(field.getName()).stream()
                                .limit(1)
                                .map(Float::parseFloat)
                                .collect(Collectors.toList())
                                .get(0));
                    } else if (field.getGenericType().getTypeName().contains("java.lang.Double")) {
                        field.set(entity, sourceMap.get(field.getName()).stream()
                                .limit(1)
                                .map(Double::parseDouble)
                                .collect(Collectors.toList())
                                .get(0));
                    } else if (field.getGenericType().getTypeName().contains("java.lang.Boolean")) {
                        field.set(entity, sourceMap.get(field.getName()).stream()
                                .limit(1)
                                .map(Boolean::parseBoolean)
                                .collect(Collectors.toList())
                                .get(0));
                    } else {
                        field.set(entity, sourceMap.get(field.getName()).stream().limit(1).collect(Collectors.toList()).get(0));
                    }
                }
            }
        }
    }
}
