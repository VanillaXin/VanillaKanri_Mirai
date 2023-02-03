package xin.vanilla.util;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.MemberPermission;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import xin.vanilla.VanillaKanri;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
    /**
     * 普通群员
     */
    public static final int PERMISSION_LEVEL_MEMBER = 0;

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
        String prefix = Va.globalConfig.getInstructions().getPrefix();
        if ("".equals(prefix)) {
            if (!secondary) return true;

            // 如果顶级前缀为空则遍历二级指令前缀
            for (String prefix_ : Va.globalConfig.getInstructions().getSecondaryPrefix()) {
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
     * 判断是否超人(不是)
     */
    public static boolean isSuperOwner(Bot bot, long qq) {
        try {
            return Va.globalConfig.getSuperOwner() == qq;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * 判断是否机器人主人
     * <p>
     * 主人>超管>群主>主管>群管>副管=群副管
     */
    public static boolean isBotOwner(Bot bot, long qq) {
        try {
            return Va.globalConfig.getPermissions().get(bot.getId()).getBotOwner() == qq;
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * 判断是否机器人超管
     * <p>
     * 主人>超管>群主>主管>群管>副管=群副管
     */
    public static boolean isSuperAdmin(Bot bot, long qq) {
        try {
            return Va.globalConfig.getPermissions().get(bot.getId()).getSuperAdmin().contains(qq);
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * 判断是否机器人主管
     * <p>
     * 主人>超管>群主>主管>群管>副管=群副管
     */
    public static boolean isBotAdmin(Bot bot, long qq) {
        try {
            return Va.globalConfig.getPermissions().get(bot.getId()).getBotAdmin().contains(qq);
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * 判断是否群管理员
     * <p>
     * 主人>超管>群主>主管>群管>副管=群副管
     */
    public static boolean isGroupAdmin(Group group, long qq) {
        NormalMember normalMember = group.get(qq);
        if (normalMember == null) return false;
        else return normalMember.getPermission().getLevel() == MemberPermission.ADMINISTRATOR.getLevel();
    }

    /**
     * 判断是否机器人副管
     * <p>
     * 主人>超管>群主>主管>群管>副管=群副管
     */
    public static boolean isDeputyAdmin(Bot bot, long qq) {
        try {
            return Va.globalConfig.getPermissions().get(bot.getId()).getDeputyAdmin().contains(qq);
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * 判断是否群副管
     * <p>
     * 主人>超管>群主>主管>群管>副管=群副管
     */
    public static boolean isDeputyAdmin(Group group, long qq) {
        try {
            return Va.groupConfig.getDeputyAdmin().get(group.getId()).contains(qq);
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * 获取某人机器人权限
     *
     * @return int, 参考 PERMISSION_LEVEL_*
     */
    public static int getPermissionLevel(Bot bot, Group group, long qq) {
        int permission = PERMISSION_LEVEL_MEMBER;
        if (bot != null) {
            if (isBotOwner(bot, qq)) permission = PERMISSION_LEVEL_BOTOWNER;
            else if (isSuperAdmin(bot, qq)) permission = PERMISSION_LEVEL_SUPERADMIN;
            else if (isBotAdmin(bot, qq)) permission = PERMISSION_LEVEL_BOTADMIN;
            else if (isDeputyAdmin(bot, qq)) permission = PERMISSION_LEVEL_DEPUTYADMIN;
        }
        if (group != null) {
            if (isGroupOwner(group, qq)) permission = Math.max(permission, PERMISSION_LEVEL_GROUPOWNER);
            else if (isGroupAdmin(group, qq)) permission = Math.max(permission, PERMISSION_LEVEL_GROUPADMIN);
            else if (isDeputyAdmin(group, qq)) permission = Math.max(permission, PERMISSION_LEVEL_DEPUTYADMIN);
        }
        return permission;
    }

    /**
     * 比较俩者权限
     *
     * @return boolean, true: 前者大于后者, false: 前者小于等于后者
     */
    public static boolean equalsPermission(Bot bot, Group group, long a, long b) {
        return getPermissionLevel(bot, group, a) > getPermissionLevel(bot, group, b);
    }

    /**
     * 比较俩者权限
     *
     * @return boolean, true: 前者大于后者, false: 前者小于等于后者
     */
    public static boolean equalsPermission(Bot bot, long a, long b) {
        return equalsPermission(bot, null, a, b);
    }

    /**
     * 比较俩者权限
     *
     * @return boolean, true: 前者大于后者, false: 前者小于等于后者
     */
    public static boolean equalsPermission(Group group, long a, long b) {
        return equalsPermission(null, group, a, b);
    }

    /**
     * 比较俩者权限
     *
     * @return int, 正整数: 前者大于后者, 0: 俩者相等, 负整数: 前者小于后者
     */
    public static int comparePermission(Bot bot, Group group, long a, long b) {
        return getPermissionLevel(bot, group, a) - getPermissionLevel(bot, group, b);
    }

    /**
     * 比较俩者权限
     *
     * @return int, 正整数: 前者大于后者, 0: 俩者相等, 负整数: 前者小于后者
     */
    public static int comparePermission(Bot bot, long a, long b) {
        return comparePermission(bot, null, a, b);
    }

    /**
     * 比较俩者权限
     *
     * @return int, 正整数: 前者大于后者, 0: 俩者相等, 负整数: 前者小于后者
     */
    public static int comparePermission(Group group, long a, long b) {
        return comparePermission(null, group, a, b);
    }

    /**
     * 是否有或(有大于)给定的权限等级
     *
     * @param level 权限等级 例: PERMISSION_LEVEL_BOTADMIN
     */
    public static boolean hasPermissionOrMore(Bot bot, Group group, long qq, int level) {
        if (level == PERMISSION_LEVEL_SUPEROWNER && bot != null)
            return isSuperOwner(bot, qq);
        else return getPermissionLevel(bot, group, qq) >= level;
    }

    // endregion 判断权限

    public static long[] getQQFromAt(String qq) {
        try {
            if (qq.trim().contains(" ")) {
                String[] s = qq.trim().split(" ");
                return Arrays.stream(s).mapToLong(Long::parseLong).toArray();
            } else {
                return new long[]{Long.parseLong(qq)};
            }
        } catch (NumberFormatException ignored) {
            try {
                Set<Long> qqs = new HashSet<>();
                for (SingleMessage singleMessage : MiraiCode.deserializeMiraiCode(qq)) {
                    if (singleMessage instanceof At) {
                        qqs.add(((At) singleMessage).getTarget());
                    }
                }
                return qqs.stream().mapToLong(Long::longValue).toArray();
            } catch (Exception e) {
                return new long[0];
            }
        }
    }
}
