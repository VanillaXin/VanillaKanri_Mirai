package xin.vanilla.util;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.message.data.MessageChain;
import xin.vanilla.VanillaKanri;

@SuppressWarnings("unused")
public class VanillaUtils {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;
    // 主人>超管>群主>主管>群管>副管=群副管
    /**
     * ?管理(?)
     */
    public static final int PERMISSION_LEVEL_SUPEROWNER = 100;
    /**
     * 机器人主人
     */
    public static final int PERMISSION_LEVEL_BOTOWNER = 6;
    /**
     * 机器人超管
     */
    public static final int PERMISSION_LEVEL_SUPERADMIN = 5;
    /**
     * 群主
     */
    public static final int PERMISSION_LEVEL_GROUPOWNER = 4;
    /**
     * 机器人主管
     */
    public static final int PERMISSION_LEVEL_BOTADMIN = 3;
    /**
     * 群管
     */
    public static final int PERMISSION_LEVEL_GROUPADMIN = 2;
    /**
     * 副管
     */
    public static final int PERMISSION_LEVEL_DEPUTYADMIN = 1;

    // region 判断指令格式

    /**
     * 判断是否指令消息
     */
    public static boolean isInstructionMsg(MessageChain msg) {
        return isInstructionMsg(msg, true);
    }

    /**
     * 判断是否指令消息
     * <p> 匹配规则: [顶级前缀] [群号] 二级前缀
     *
     * @param secondary 若顶级前缀为空, 是否继续判断二级指令前缀
     */
    public static boolean isInstructionMsg(MessageChain msg, boolean secondary) {
        String prefix = Va.globalConfig.INSTRUCTIONS.get().getPrefix();
        if ("".equals(prefix)) {
            if (!secondary) return true;

            // 如果顶级前缀为空则遍历二级指令前缀
            for (String prefix_ : Va.globalConfig.INSTRUCTIONS.get().getSecondaryPrefix()) {
                if ("".equals(prefix_)) continue;
                if (msg.contentToString().startsWith(prefix_ + " ")) return true;
            }
            return false;
        } else {
            String space = " ";
            if (StringUtils.isCommonMark(prefix)) space = "";
            return msg.contentToString().startsWith(prefix + space);
        }
    }

    // endregion 判断指令格式

    // region 判断权限

    /**
     * 机器人是否群主
     */
    public static boolean isGroupOwner(Group group) {
        return group.getBotPermission().getLevel() == MemberPermission.OWNER.getLevel();
    }

    /**
     * 机器人是否群管(理员)
     */
    public static boolean isGroupAdmin(Group group) {
        return group.getBotPermission().getLevel() == MemberPermission.ADMINISTRATOR.getLevel();
    }

    /**
     * 机器人是否拥有管理群的权限
     */
    public static boolean isGroupOwnerOrAdmin(Group group) {
        return isGroupOwner(group) || isGroupAdmin(group);
    }

    /**
     * 是否群主
     */
    public static boolean isGroupOwner(Group group, long qq) {
        return group.getOwner().getId() == qq;
    }

    /**
     * 比较两者群内权限
     *
     * @return boolean, true: 前者大于后者, false: 前者小于等于后者
     */
    public static boolean equalsLevel(Member a, Member b) {
        return a.getPermission().compareTo(b.getPermission()) > 0;
    }

    /**
     * 比较两者群内权限
     *
     * @return int, 1: 前者大于后者, 0: 俩者相等, -1: 前者小于后者
     */
    public static int compareLevel(Member a, Member b) {
        return a.getPermission().compareTo(b.getPermission());
    }

    /**
     * 判断是否机器人超人(不是)
     */
    public static boolean isSuperOwner(Bot bot, long qq) {
        return Va.globalConfig.PERMISSIONS.get().getSuperOwner() == qq;
    }

    /**
     * 判断是否机器人主人
     * <p>
     * 主人>超管>群主>主管>群管>副管=群副管
     */
    public static boolean isBotOwner(Bot bot, long qq) {
        return Va.globalConfig.PERMISSIONS.get().getBotOwner() == qq;
    }

    /**
     * 判断是否机器人超管
     * <p>
     * 主人>超管>群主>主管>群管>副管=群副管
     */
    public static boolean isSuperAdmin(Bot bot, long qq) {
        return Va.globalConfig.PERMISSIONS.get().getSuperAdmin().contains(qq);
    }

    /**
     * 判断是否机器人主管
     * <p>
     * 主人>超管>群主>主管>群管>副管=群副管
     */
    public static boolean isBotAdmin(Bot bot, long qq) {
        return Va.globalConfig.PERMISSIONS.get().getBotAdmin().contains(qq);
    }

    /**
     * 判断是否机器人副管
     * <p>
     * 主人>超管>群主>主管>群管>副管=群副管
     */
    public static boolean isDeputyAdmin(Bot bot, long qq) {
        return Va.globalConfig.PERMISSIONS.get().getDeputyAdmin().contains(qq);
    }

    /**
     * 判断是否群副管
     * <p>
     * 主人>超管>群主>主管>群管>副管=群副管
     */
    public static boolean isDeputyAdmin(Group group, long qq) {
        return false;
    }

    /**
     * 比较俩者机器人权限
     *
     * @return boolean, true: 前者大于后者, false: 前者小于等于后者
     */
    public static boolean equalsPermission(long a, long b) {
        return false;
    }

    /**
     * 比较俩者机器人权限
     *
     * @return int, 1: 前者大于后者, 0: 俩者相等, -1: 前者小于后者
     */
    public static int comparePermission(long a, long b) {
        return 0;
    }

    /**
     * 是否有或(有大于)给定的权限等级
     *
     * @param level 权限等级 例: PERMISSION_LEVEL_BOTADMIN
     */
    public static boolean hasPermissionOrMore(long qq, int level) {
        return false;
    }

    // endregion 判断权限
}
