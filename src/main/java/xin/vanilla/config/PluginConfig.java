package xin.vanilla.config;

import xin.vanilla.entity.instruction.Instructions;

public class PluginConfig {
    // 插件指令
    public Instructions INS = new Instructions();

    public PermissionConfig PERMISSIONS = new PermissionConfig();


    public boolean init() {
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
     * 初始化指令列表
     */
    public boolean initInstructions() {
        try {
            INS = new Instructions();
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
