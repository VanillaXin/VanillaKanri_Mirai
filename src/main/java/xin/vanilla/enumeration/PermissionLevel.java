package xin.vanilla.enumeration;

public enum PermissionLevel {
    /**
     * 普通群员
     */
    PERMISSION_LEVEL_MEMBER(0, "普通群员"),
    /**
     * 副管
     */
    PERMISSION_LEVEL_DEPUTY_ADMIN(1, "副管"),
    /**
     * 群管
     */
    PERMISSION_LEVEL_GROUP_ADMIN(2, "群管"),
    /**
     * 机器人主管
     */
    PERMISSION_LEVEL_BOT_ADMIN(3, "主管"),
    /**
     * 群主
     */
    PERMISSION_LEVEL_GROUP_OWNER(4, "群主"),
    /**
     * 机器人超管
     */
    PERMISSION_LEVEL_SUPER_ADMIN(5, "超管"),
    /**
     * 机器人主人
     */
    PERMISSION_LEVEL_BOT_OWNER(6, "主人"),
    /**
     * ?管理(?)
     */
    PERMISSION_LEVEL_SUPER_OWNER(100, "保留");

    private final int level;
    private final String name;

    PermissionLevel(int level, String name) {
        this.level = level;
        this.name = name;
    }

    public int getLevel() {
        return level;
    }
}
