package xin.vanilla.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.data.ForwardMsg;
import xin.vanilla.entity.data.Node;
import xin.vanilla.enumeration.PermissionLevel;

import java.util.*;
import java.util.stream.Collectors;

import static xin.vanilla.enumeration.PermissionLevel.*;

@SuppressWarnings("unused")
public class VanillaUtils {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;
    // 主人>超管>群主>主管>群管>副管=群副管

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
     * 是否有或(有大于)给定的权限等级
     *
     * @param level 权限等级 例: PERMISSION_LEVEL_BOTADMIN
     */
    public static boolean hasPermissionOrMore(Bot bot, Group group, long qq, PermissionLevel level) {
        if (level == PermissionLevel.PERMISSION_LEVEL_SUPER_OWNER && bot != null)
            return isSuperOwner(bot, qq);
        else return getPermissionLevel(bot, group, qq) >= level.getLevel();
    }

    // endregion 判断权限

    public static long[] getQQFromAt(String qq) {
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
            } catch (Exception e) {
                return new long[0];
            }
        } catch (NullPointerException e) {
            return new long[0];
        }
    }

    public static void setDateCache(String key, Object value) {
        Va.getDataCache().put(key, value);
    }

    public static String getDataCacheAsString(String key) {
        if (Va.getDataCache().containsKey(key)) {
            return (String) Va.getDataCache().get(key);
        }
        return "";
    }

    public static long getDataCacheAsLong(String key) {
        if (Va.getDataCache().containsKey(key)) {
            return (long) Va.getDataCache().get(key);
        }
        return 0;
    }

    public static double getDataCacheAsDouble(String key) {
        if (Va.getDataCache().containsKey(key)) {
            return (double) Va.getDataCache().get(key);
        }
        return 0;
    }

    public static boolean getDataCacheAsBoolean(String key) {
        if (Va.getDataCache().containsKey(key)) {
            return (boolean) Va.getDataCache().get(key);
        }
        return false;
    }

    /**
     * 将消息转为Mirai码 <strong>慎用</strong>
     * <p>
     * 不转码文本消息中的特殊字符
     */
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
     * 将消息序列化为vanilla码
     */
    public static String serializeToVanillaCode(MessageChain msg, long bot, long sender, long target) {
        String msgString;

        MarketFace marketFace = msg.get(MarketFace.Key);
        ForwardMessage forwardMessage = msg.get(ForwardMessage.Key);
        Audio audio = msg.get(Audio.Key);

        if (marketFace != null) {
            msgString = marketFace.toString();
        } else if (forwardMessage != null) {
            // ForwardMsg forwardMsg = BeanCopyUtils.copyBean(forwardMessage, ForwardMsg.class);
            ForwardMsg forwardMsg = new ForwardMsg();
            forwardMsg.setPreview(forwardMessage.getPreview());
            forwardMsg.setTitle(forwardMessage.getTitle());
            forwardMsg.setBrief(forwardMessage.getBrief());
            forwardMsg.setSource(forwardMessage.getSource());
            forwardMsg.setSummary(forwardMessage.getSummary());
            forwardMsg.setNodeList(forwardMessage.getNodeList().stream().map(o -> {
                Node node = new Node();
                node.setSenderId(o.getSenderId());
                node.setSenderName(o.getSenderName());
                node.setTime(o.getTime());
                node.setMessageChain(o.getMessageChain().serializeToMiraiCode());
                return node;
            }).collect(Collectors.toList()));

            forwardMsg.setBot(bot);
            forwardMsg.setUser(sender);
            if (bot != target)
                forwardMsg.setGroup(target);
            msgString = "(:vacode:){forward}" + JSON.toJSONString(forwardMsg);

        } else if (audio != null) {
            msgString = audio.toString();
        } else {
            msgString = msg.serializeToMiraiCode();
            if (msgString.startsWith("(:vacode:)"))
                msgString = msgString.replace("(:vacode:)", "\\(:vacode:\\)");
        }
        System.out.println(msgString);
        return msgString;
    }

    /**
     * 将vanilla码反序列化为消息
     */
    public static MessageChain deserializeVanillaCode(String msg) {
        if (msg.startsWith("(:vacode:)")) {
            msg = msg.substring("(:vacode:)".length());
            if (msg.startsWith("{forward}")) {
                msg = msg.substring("{forward}".length());
                new ForwardMsg();
                ForwardMsg forwardMsg = new ForwardMsg();
                JSONObject jsonObject = JSON.parseObject(msg);
                // forwardMsg = jsonObject.toJavaObject(ForwardMsg.class);
                forwardMsg.setUser(jsonObject.getLongValue("user"));
                forwardMsg.setGroup(jsonObject.getLongValue("group"));
                forwardMsg.setBot(jsonObject.getLongValue("bot"));
                forwardMsg.setTitle(jsonObject.getString("title"));
                forwardMsg.setBrief(jsonObject.getString("brief"));
                forwardMsg.setSource(jsonObject.getString("source"));
                forwardMsg.setSummary(jsonObject.getString("summary"));

                List<String> preview = jsonObject.getList("preview", String.class);
                forwardMsg.setPreview(preview);
                JSONArray jsonArray = jsonObject.getJSONArray("nodeList");
                List<Node> nodeList = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    Node node = new Node();
                    node.setTime(jsonObject1.getIntValue("time"));
                    node.setSenderName(jsonObject1.getString("senderName"));
                    node.setSenderId(jsonObject1.getLongValue("senderId"));
                    node.setMessageChain(jsonObject1.getString("messageChain"));
                    nodeList.add(node);
                }
                forwardMsg.setNodeList(nodeList);
                ForwardMessageBuilder forwardMessageBuilder;
                if (forwardMsg.getGroup() > 0) {
                    Group group = Bot.getInstance(forwardMsg.getBot()).getGroup(forwardMsg.getGroup());
                    assert group != null;
                    forwardMessageBuilder = new ForwardMessageBuilder(group);
                } else {
                    User user = Bot.getInstance(forwardMsg.getBot()).getFriend(forwardMsg.getUser());
                    assert user != null;
                    forwardMessageBuilder = new ForwardMessageBuilder(user);
                }
                for (Node node : forwardMsg.getNodeList()) {
                    forwardMessageBuilder.add(node.getSenderId(), node.getSenderName(), MiraiCode.deserializeMiraiCode(node.getMessageChain()), node.getTime());
                }
                forwardMessageBuilder.setDisplayStrategy(new ForwardMessage.DisplayStrategy() {
                    @NotNull
                    @Override
                    public String generateBrief(@NotNull RawForwardMessage forward) {
                        return forwardMsg.getBrief();
                    }

                    @NotNull
                    @Override
                    public List<String> generatePreview(@NotNull RawForwardMessage forward) {
                        return forwardMsg.getPreview();
                    }

                    @NotNull
                    @Override
                    public String generateSource(@NotNull RawForwardMessage forward) {
                        return forwardMsg.getSource();
                    }

                    @NotNull
                    @Override
                    public String generateSummary(@NotNull RawForwardMessage forward) {
                        return forwardMsg.getSummary();
                    }

                    @NotNull
                    @Override
                    public String generateTitle(@NotNull RawForwardMessage forward) {
                        return forwardMsg.getTitle();
                    }
                });
                return new MessageChainBuilder().append(forwardMessageBuilder.build()).build();
            }
        } else if (msg.startsWith("\\(:vacode:\\)"))
            msg = msg.replace("\\(:vacode:\\)", "(:vacode:)");
        return MiraiCode.deserializeMiraiCode(msg);
    }
}
