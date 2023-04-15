package xin.vanilla.util;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.enumeration.DataCacheKey;
import xin.vanilla.enumeration.PermissionLevel;

import java.util.*;

import static xin.vanilla.enumeration.PermissionLevel.*;

@SuppressWarnings("unused")
public class VanillaUtils {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;

    // region 判断权限
    // 主人>超管>群主>主管>群管>副管=群副管

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
            return Va.getGlobalConfig().getSuperOwner() == qq;
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
            return Va.getGlobalConfig().getPermissions().get(bot.getId()).getBotOwner() == qq;
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
            return Va.getGlobalConfig().getPermissions().get(bot.getId()).getSuperAdmin().contains(qq);
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
            return Va.getGlobalConfig().getPermissions().get(bot.getId()).getBotAdmin().contains(qq);
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
            return Va.getGlobalConfig().getPermissions().get(bot.getId()).getDeputyAdmin().contains(qq);
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
            return Va.getGroupConfig().getDeputyAdmin().get(group.getId()).contains(qq);
        } catch (NullPointerException e) {
            return false;
        }
    }

    /**
     * 获取某人机器人权限
     *
     * @return int, 参考 PERMISSION_LEVEL_*
     */
    public static int getPermissionLevel(long botId, long groupId, long qq) {
        if (botId > 0) {
            Bot bot = Bot.getInstance(botId);
            return getPermissionLevel(bot, bot.getGroup(groupId), qq);
        }
        return PERMISSION_LEVEL_MEMBER.getLevel();
    }

    /**
     * 获取某人机器人权限
     *
     * @return int, 参考 PERMISSION_LEVEL_*
     */
    public static int getPermissionLevel(Bot bot, long groupId, long qq) {
        if (bot != null) {
            return getPermissionLevel(bot, Bot.getInstance(bot.getId()).getGroup(groupId), qq);
        }
        return PERMISSION_LEVEL_MEMBER.getLevel();
    }

    /**
     * 获取某人机器人权限
     *
     * @return int, 参考 PERMISSION_LEVEL_*
     */
    public static int getPermissionLevel(Bot bot, Group group, long qq) {
        int permission = PERMISSION_LEVEL_MEMBER.getLevel();
        if (bot != null) {
            if (isBotOwner(bot, qq)) permission = PERMISSION_LEVEL_BOT_OWNER.getLevel();
            else if (isSuperAdmin(bot, qq)) permission = PERMISSION_LEVEL_SUPER_ADMIN.getLevel();
            else if (isBotAdmin(bot, qq)) permission = PERMISSION_LEVEL_BOT_ADMIN.getLevel();
            else if (isDeputyAdmin(bot, qq)) permission = PERMISSION_LEVEL_DEPUTY_ADMIN.getLevel();
        }
        if (group != null) {
            if (isGroupOwner(group, qq)) permission = Math.max(permission, PERMISSION_LEVEL_GROUP_OWNER.getLevel());
            else if (isGroupAdmin(group, qq))
                permission = Math.max(permission, PERMISSION_LEVEL_GROUP_ADMIN.getLevel());
            else if (isDeputyAdmin(group, qq))
                permission = Math.max(permission, PERMISSION_LEVEL_DEPUTY_ADMIN.getLevel());
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
     * 是否没有(且没有大于)给定的权限等级
     *
     * @param level 权限等级 例: PERMISSION_LEVEL_BOT_ADMIN
     */
    public static boolean hasNotPermissionAndMore(Bot bot, Group group, long qq, PermissionLevel level) {
        return !hasPermissionOrMore(bot, group, qq, level);
    }

    /**
     * 是否有(或有大于)给定的权限等级
     *
     * @param level 权限等级 例: PERMISSION_LEVEL_BOT_ADMIN
     */
    public static boolean hasPermissionOrMore(Bot bot, Group group, long qq, PermissionLevel level) {
        if (level == PermissionLevel.PERMISSION_LEVEL_SUPER_OWNER && bot != null) return isSuperOwner(bot, qq);
        else return getPermissionLevel(bot, group, qq) >= level.getLevel();
    }

    // endregion 判断权限

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
        String prefix = Va.getGlobalConfig().getInstructions().getPrefix();
        if ("".equals(prefix)) {
            if (!secondary) return true;

            // 如果顶级前缀为空则遍历二级指令前缀
            for (String prefix_ : Va.getGlobalConfig().getInstructions().getSecondaryPrefix()) {
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

    // region 缓存存取

    /**
     * 设置缓存数据
     *
     * @param key 参考 {@link DataCacheKey} 中的键值或自定义键值
     */
    public static void setDateCache(String key, Object value) {
        Va.getDataCache().put(key, value);
    }

    /**
     * 获取数据缓存
     *
     * @param key 参考 {@link DataCacheKey} 中的键值或自定义键值
     */
    public static String getDataCacheAsString(String key) {
        if (Va.getDataCache().containsKey(key)) {
            return (String) Va.getDataCache().get(key);
        }
        return "";
    }

    /**
     * 获取数据缓存
     *
     * @param key 参考 {@link DataCacheKey} 中的键值或自定义键值
     */
    public static long getDataCacheAsLong(String key) {
        if (Va.getDataCache().containsKey(key)) {
            return (long) Va.getDataCache().get(key);
        }
        return 0;
    }

    /**
     * 获取数据缓存
     *
     * @param key 参考 {@link DataCacheKey} 中的键值或自定义键值
     */
    public static double getDataCacheAsDouble(String key) {
        if (Va.getDataCache().containsKey(key)) {
            return (double) Va.getDataCache().get(key);
        }
        return 0;
    }

    /**
     * 获取数据缓存
     *
     * @param key 参考 {@link DataCacheKey} 中的键值或自定义键值
     */
    public static boolean getDataCacheAsBoolean(String key) {
        if (Va.getDataCache().containsKey(key)) {
            return (boolean) Va.getDataCache().get(key);
        }
        return false;
    }

    // endregion 缓存存取

    // region 消息转码

    /**
     * 将形如 <code>123456789 [mirai:at:234567890] [mirai:at:345678901]</code> 的
     * <p>QQ号字符串转为QQ号数组</p>
     * <p>[123456789, 234567890, 345678901]</p>
     */
    public static long[] getQQFromString(String qq) {
        try {
            qq = qq.trim();
            if (qq.contains(" ")) {
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
            } catch (Exception ignored1) {
            }
        } catch (NullPointerException ignored) {
        }
        return new long[0];
    }

    /**
     * 将形如 <code>&lt;123456789 234567890&gt;</code> 的
     * <p>群号字符串转为群号数组:</p>
     * <p>[123456789, 234567890]</p>
     */
    public static long[] getGroupFromString(String group) {
        try {
            group = group.trim().replace("<", "").replace(">", "");
            if (group.contains(" ")) {
                String[] s = group.trim().split(" ");
                return Arrays.stream(s).mapToLong(Long::parseLong).toArray();
            } else {
                return new long[]{Long.parseLong(group)};
            }
        } catch (NumberFormatException ignored) {
            if (Va.getGlobalConfig().getInstructions().getBase().getThat().contains(group)) {
                return new long[]{0};
            }
        } catch (NullPointerException ignored) {
        }
        return new long[0];
    }

    /**
     * 将消息转为Mirai码 <strong>慎用</strong>
     * <p>不转码文本消息中的特殊字符</p>
     */
    @NotNull
    public static String messageToString(MessageChain message) {
        StringBuilder stringBuilder = new StringBuilder();
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof PlainText) {
                stringBuilder.append(singleMessage.contentToString());
            } else {
                stringBuilder.append(new MessageChainBuilder().append(singleMessage).build().serializeToMiraiCode());
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 提取消息中的PlainText
     */
    public static @NotNull String messageToPlainText(@NotNull MessageChain message) {
        StringBuilder str = new StringBuilder();
        for (SingleMessage singleMessage : message) {
            if (singleMessage instanceof PlainText) {
                PlainText plainText = (PlainText) singleMessage;
                str.append(plainText.contentToString());
            }
        }
        return str.toString();
    }

    /**
     * 将消息序列化为Json
     */
    @NotNull
    public static String serializeToJsonCode(MessageChain msg) {
        return MessageChain.serializeToJsonString(msg);
    }

    /**
     * 将Json反序列化为消息
     */
    @NotNull
    public static MessageChain deserializeJsonCode(String msg) {
        return MessageChain.deserializeFromJsonString(msg);
    }

    /**
     * 词库触发(keyWord)消息转码
     */
    @NotNull
    public static String enVanillaCodeKey(@NotNull String word) {
        Map<String, String> keyCode = RegExpConfig.VaCode.EN_KEY;
        String result = word;
        for (String key : keyCode.keySet()) {
            result = result.replaceAll(key, keyCode.get(key));
        }
        return result;
    }

    /**
     * 词库触发(keyWord)消息解码
     */
    @NotNull
    @Contract(pure = true)
    public static String deVanillaCodeKey(@NotNull String word) {
        Map<String, String> keyCode = RegExpConfig.VaCode.DE_KEY;
        String result = word;
        for (String key : keyCode.keySet()) {
            result = result.replaceAll(key, keyCode.get(key));
        }
        return result;
    }

    /**
     * 词库回复(repMsg)消息转码
     */
    public static String enVanillaCodeRep(@NotNull String msg) {
        Map<String, String> repCode = RegExpConfig.VaCode.EN_REP;
        String result = msg;
        for (String key : repCode.keySet()) {
            result = result.replaceAll(key, repCode.get(key));
        }
        return result;
    }

    /**
     * 词库回复(repMsg)消息解码
     *
     * @param only 是否仅解析转义非重要特殊码
     */
    public static String deVanillaCodeRep(@NotNull String msg, boolean only) {
        Map<String, String> repCode = RegExpConfig.VaCode.DE_REP;
        String result = msg;
        for (String key : repCode.keySet()) {
            result = result.replaceAll(key, repCode.get(key));
        }
        if (only) return result;

        // 替换日期特时间殊码
        if (result.contains("[vacode:date:")) {
            Calendar now = Calendar.getInstance();
            // 當前秒
            int second = now.get(Calendar.SECOND);
            result = result.replaceAll("\\[vacode:date:s]", String.valueOf(second));
            result = result.replaceAll("\\[vacode:date:0s]", second < 10 ? "0" + second : String.valueOf(second));
            result = result.replaceAll("\\[vacode:date:s:", "[vacode:math:add:s:+")
                    .replaceAll("\\[vacode:math:add:s:\\+\\+", "[vacode:math:add:s:+");
            result = result.replaceAll("\\[vacode:math:add:s:", "[vacode:math:add:" + second);

            // 當前分
            int minute = now.get(Calendar.MINUTE);
            result = result.replaceAll("\\[vacode:date:m]", String.valueOf(minute));
            result = result.replaceAll("\\[vacode:date:0m]", minute < 10 ? "0" + minute : String.valueOf(minute));
            result = result.replaceAll("\\[vacode:date:m:", "[vacode:math:add:m:+")
                    .replaceAll("\\[vacode:math:add:m:\\+\\+", "[vacode:math:add:m:+");
            result = result.replaceAll("\\[vacode:math:add:m:", "[vacode:math:add:" + minute);

            // 當前時
            int hour = now.get(Calendar.HOUR_OF_DAY);
            result = result.replaceAll("\\[vacode:date:H]", String.valueOf(hour));
            result = result.replaceAll("\\[vacode:date:0H]", hour < 10 ? "0" + hour : String.valueOf(hour));
            result = result.replaceAll("\\[vacode:date:H:", "[vacode:math:add:H:+")
                    .replaceAll("\\[vacode:math:add:H:\\+\\+", "[vacode:math:add:H:+");
            result = result.replaceAll("\\[vacode:math:add:H:", "[vacode:math:add:" + hour);

            // 當前日
            int day = now.get(Calendar.DATE);
            result = result.replaceAll("\\[vacode:date:d]", String.valueOf(day));
            result = result.replaceAll("\\[vacode:date:0d]", day < 10 ? "0" + day : String.valueOf(day));
            result = result.replaceAll("\\[vacode:date:d:", "[vacode:math:add:d:+")
                    .replaceAll("\\[vacode:math:add:d:\\+\\+", "[vacode:math:add:d:+");
            result = result.replaceAll("\\[vacode:math:add:d:", "[vacode:math:add:" + day);

            // 當前周
            int week = now.get(Calendar.DAY_OF_WEEK) - 1;
            if (week == 0) week = 7;
            result = result.replaceAll("\\[vacode:date:E]", String.valueOf(week));
            result = result.replaceAll("\\[vacode:date:E:", "[vacode:math:add:E:+")
                    .replaceAll("\\[vacode:math:add:E:\\+\\+", "[vacode:math:add:E:+");
            result = result.replaceAll("\\[vacode:math:add:E:", "[vacode:math:add:" + week);

            // 當前月
            int month = now.get(Calendar.MONTH) + 1;
            result = result.replaceAll("\\[vacode:date:M]", String.valueOf(month));
            result = result.replaceAll("\\[vacode:date:0M]", month < 10 ? "0" + month : String.valueOf(month));
            result = result.replaceAll("\\[vacode:date:M:", "[vacode:math:add:M:+")
                    .replaceAll("\\[vacode:math:add:M:\\+\\+", "[vacode:math:add:M:+");
            result = result.replaceAll("\\[vacode:math:add:M:", "[vacode:math:add:" + month);

            // 當前年
            int year = now.get(Calendar.YEAR);
            result = result.replaceAll("\\[vacode:date:y]", String.valueOf(year));
            result = result.replaceAll("\\[vacode:date:y:", "[vacode:math:add:y:+")
                    .replaceAll("\\[vacode:math:add:y:\\+\\+", "[vacode:math:add:y:+");
            result = result.replaceAll("\\[vacode:math:add:y:", "[vacode:math:add:" + year);

            // 本月第幾周
            int weekOfMonth = now.get(Calendar.WEEK_OF_MONTH);
            result = result.replaceAll("\\[vacode:date:W]", String.valueOf(weekOfMonth));
            result = result.replaceAll("\\[vacode:date:W:", "[vacode:math:add:W:+")
                    .replaceAll("\\[vacode:math:add:W:\\+\\+", "[vacode:math:add:W:+");
            result = result.replaceAll("\\[vacode:math:add:W:", "[vacode:math:add:" + weekOfMonth);

            // 本年第幾周
            int weekOfYear = now.get(Calendar.WEEK_OF_YEAR);
            result = result.replaceAll("\\[vacode:date:w]", String.valueOf(weekOfYear));
            result = result.replaceAll("\\[vacode:date:w:", "[vacode:math:add:w:+")
                    .replaceAll("\\[vacode:math:add:w:\\+\\+", "[vacode:math:add:w:+");
            result = result.replaceAll("\\[vacode:math:add:w:", "[vacode:math:add:" + weekOfYear);

            // 本年第幾天
            int dayOfYear = now.get(Calendar.DAY_OF_YEAR);
            result = result.replaceAll("\\[vacode:date:D]", String.valueOf(dayOfYear));
            result = result.replaceAll("\\[vacode:date:D:", "[vacode:math:add:D:+")
                    .replaceAll("\\[vacode:math:add:D:\\+\\+", "[vacode:math:add:D:+");
            result = result.replaceAll("\\[vacode:math:add:D:", "[vacode:math:add:" + dayOfYear);

            // 本月總天數
            int lastDayOfMonth = DateUtil.getLastDayOfMonth(now.getTime());
            result = result.replaceAll("\\[vacode:date:days]", String.valueOf(lastDayOfMonth));
            result = result.replaceAll("\\[vacode:date:days:", "[vacode:math:add:days:+")
                    .replaceAll("\\[vacode:math:add:days:\\+\\+", "[vacode:math:add:days:+");
            result = result.replaceAll("\\[vacode:math:add:days:", "[vacode:math:add:" + lastDayOfMonth);
        }

        // 替换随机数特殊码
        if (result.contains("[vacode:rand:")) {
            RegUtils regUtils = new RegUtils().appendIg(".*?")
                    .append("[vacode:rand:")
                    .groupIgByName("num1", "-?[1-9]\\d{0,9}(?:\\.\\d{1,4})?")
                    .append(":")
                    .groupIgByName("num2", "-?[1-9]\\d{0,9}(?:\\.\\d{1,4})?")
                    .appendIg("].*?");
            while (regUtils.matcher(result).find()) {
                String numString1 = regUtils.getMatcher().group("num1");
                String numString2 = regUtils.getMatcher().group("num2");
                String ran;
                if (numString1.contains(".") || numString2.contains(".")) {
                    ran = NumberUtil.roundStr(RandomUtil.randomDouble(Double.parseDouble(numString1), Double.parseDouble(numString2) + 0.0001), 4);
                } else {
                    ran = String.valueOf(RandomUtil.randomInt(Integer.parseInt(numString1), Integer.parseInt(numString2) + 1));
                }
                result = result.replaceAll("\\[vacode:rand:" + numString1 + ":" + numString2 + "]", ran);
            }
        }

        // 替换算数特殊码
        if (result.contains("[vacode:math:add:")) {
            RegUtils regUtils = new RegUtils().appendIg(".*?")
                    .append("[vacode:math:add:")
                    .groupIgByName("num1", "-?[1-9]\\d{0,9}(?:\\.\\d{1,4})?")
                    .append("+")
                    .groupIgByName("num2", "-?[1-9]\\d{0,9}(?:\\.\\d{1,4})?")
                    .appendIg("].*?");
            while (regUtils.matcher(result).find()) {
                String numString1 = regUtils.getMatcher().group("num1");
                String numString2 = regUtils.getMatcher().group("num2");
                String sum;
                if (numString1.contains(".") || numString2.contains(".")) {
                    sum = String.valueOf(Double.parseDouble(numString1) + Double.parseDouble(numString2));
                } else {
                    sum = String.valueOf(Long.parseLong(numString1) + Long.parseLong(numString2));
                }
                result = result.replaceAll("\\[vacode:math:add:" + numString1 + "\\+" + numString2 + "]", sum);
            }
        }
        return result;
    }

    /**
     * 解析并执行群管操作
     *
     * @param group        群聊对象
     * @param sender       消息发送对象
     * @param messageChain 原消息
     */
    public static String deVanillaCodeIns(@NotNull final String word, @NotNull final String rep, Bot bot, Group group, @NotNull Contact sender, MessageChain messageChain) {
        String result;
        try {
            if (!rep.contains("[vacode:")) return rep;

            // 转义艾特
            result = rep.replaceAll("\\[vacode:@]", new At(sender.getId()).serializeToMiraiCode());
            // 取发送者qq
            result = result.replaceAll("\\[vacode:qnumber]", String.valueOf(sender.getId()));
            if (group != null) {
                // 取群号
                result = result.replaceAll("\\[vacode:gnumber]", String.valueOf(group.getId()));
                // 取群名
                result = result.replaceAll("\\[vacode:gname]", group.getName());
            }

            // 非操作特殊码解码
            result = deVanillaCodeRep(result, false);

            // 解析图片特殊码
            if (result.contains("[vacode:pic:")) {
                RegUtils regUtils = new RegUtils()
                        .append("[vacode:pic:")
                        .groupIgByName("url", "(?:(?:https?|file)://|[A-Za-z]:\\\\).*?")
                        .groupIgByName("num", ":[1-9]\\d?")
                        .appendIg("?]");
                while (regUtils.matcher(result).find()) {
                    String url = regUtils.getMatcher().group("url");
                    int num;
                    String numString;
                    try {
                        numString = regUtils.getMatcher().group("num");
                        num = Integer.parseInt(numString.substring(1));
                    } catch (Exception ignored) {
                        numString = "";
                        num = 1;
                    }
                    StringBuilder picCode = new StringBuilder();
                    for (int i = 0; i < num; i++) {
                        String image = Api.uploadImageByUrl(url, group != null ? group : sender).serializeToMiraiCode();
                        picCode.append(image);
                    }
                    result = result.replace("[vacode:pic:" + url + numString + "]", picCode);
                }
            }

            // 禁言
            result = RegExpConfig.VaCode.exeMute(result, group != null ? (NormalMember) sender : null);
            // 撤回
            result = RegExpConfig.VaCode.exeRecall(result, messageChain);
            // 踢出
            result = RegExpConfig.VaCode.exeKick(word, result, group != null ? (NormalMember) sender : null);

            // ChatGPT
            result = RegExpConfig.VaCode.exeGpt(messageToPlainText(messageChain), result, group != null ? (NormalMember) sender : null);

            // 戳一戳
            result = RegExpConfig.VaCode.exeTap(result, sender);


        } catch (Exception e) {
            e.printStackTrace();
            result = "";
        }
        return result;
    }

    // endregion 消息转码

}
