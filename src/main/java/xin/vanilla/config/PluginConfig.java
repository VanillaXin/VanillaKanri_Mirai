package xin.vanilla.config;

import xin.vanilla.entity.config.Permissions;
import xin.vanilla.entity.instruction.Instructions;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 插件配置数据
 */
public class PluginConfig {

    /**
     * 可自动保存的源配置信息
     */
    public ConfigFile source = new ConfigFile();

    // 插件指令
    public Instructions INSTRUCTIONS = new Instructions();

    // 插件角色
    public Permissions PERMISSIONS = new Permissions();


    public boolean init() {
        // 初始化角色
        initPermissions();
        // 初始化指令
        initInstructions();
        return true;
    }

    /**
     * 初始化基础设置
     */
    public boolean initBaseConfig() {
        try {

        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 初始化角色列表
     */
    public boolean initPermissions() {
        try {
            // 配置文件中的权限map
            Map<String, Set<String>> permissionMap = source.permission.get();
            // 插件角色对象变量列表
            Field[] fields = PERMISSIONS.getClass().getFields();
            if (permissionMap.isEmpty()) {
                // 如果配置文件为空则初始化插件配置文件中角色配置
                for (Field field : fields) {
                    permissionMap.put(field.getName(), new HashSet<String>() {{
                        if (field.get(PERMISSIONS) instanceof Set) {
                            addAll((List<String>) ((Set) field.get(PERMISSIONS)).stream().map(Object::toString).collect(Collectors.toList()));
                        } else {
                            add(field.get(PERMISSIONS).toString());
                        }
                    }});
                }
            } else {
                // 否则读取配置文件中配置并赋值给插件角色对象
                for (Field field : fields) {
                    if (permissionMap.containsKey(field.getName())) {
                        // 允许的类型: Long、Integer、Float、Double、String
                        // Set<Long>、Set<Integer>、Set<Float>、Set<Double>、Set<String>

                        // 如果是Set集合
                        if (field.get(PERMISSIONS) instanceof Set) {
                            if (field.getGenericType().getTypeName().contains("java.lang.Long")) {
                                field.set(PERMISSIONS, permissionMap.get(field.getName()).stream()
                                        .map(Long::parseLong)
                                        .collect(Collectors.toSet()));
                            } else if (field.getGenericType().getTypeName().contains("java.lang.Integer")) {
                                field.set(PERMISSIONS, permissionMap.get(field.getName()).stream()
                                        .map(Integer::parseInt)
                                        .collect(Collectors.toSet()));
                            } else if (field.getGenericType().getTypeName().contains("java.lang.Float")) {
                                field.set(PERMISSIONS, permissionMap.get(field.getName()).stream()
                                        .map(Float::parseFloat)
                                        .collect(Collectors.toSet()));
                            } else if (field.getGenericType().getTypeName().contains("java.lang.Double")) {
                                field.set(PERMISSIONS, permissionMap.get(field.getName()).stream()
                                        .map(Double::parseDouble)
                                        .collect(Collectors.toSet()));
                            } else {
                                field.set(PERMISSIONS, permissionMap.get(field.getName()));
                            }
                        } else {
                            if (field.getGenericType().getTypeName().contains("java.lang.Long")) {
                                field.set(PERMISSIONS, permissionMap.get(field.getName()).stream()
                                        .limit(1)
                                        .map(Long::parseLong)
                                        .collect(Collectors.toList())
                                        .get(0));
                            } else if (field.getGenericType().getTypeName().contains("java.lang.Integer")) {
                                field.set(PERMISSIONS, permissionMap.get(field.getName()).stream()
                                        .limit(1)
                                        .map(Integer::parseInt)
                                        .collect(Collectors.toList())
                                        .get(0));
                            } else if (field.getGenericType().getTypeName().contains("java.lang.Float")) {
                                field.set(PERMISSIONS, permissionMap.get(field.getName()).stream()
                                        .limit(1)
                                        .map(Float::parseFloat)
                                        .collect(Collectors.toList())
                                        .get(0));
                            } else if (field.getGenericType().getTypeName().contains("java.lang.Double")) {
                                field.set(PERMISSIONS, permissionMap.get(field.getName()).stream()
                                        .limit(1)
                                        .map(Double::parseDouble)
                                        .collect(Collectors.toList())
                                        .get(0));
                            } else {
                                field.set(PERMISSIONS, permissionMap.get(field.getName()).stream().limit(1).collect(Collectors.toList()).get(0));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 初始化指令列表
     */
    public boolean initInstructions() {
        try {
            INSTRUCTIONS = new Instructions();
            // TODO 初始化指令列表

        } catch (Exception e) {
            return false;
        }
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
}
