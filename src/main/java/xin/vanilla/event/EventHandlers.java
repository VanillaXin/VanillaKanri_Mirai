package xin.vanilla.event;

import cn.hutool.core.date.DateUtil;
import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.*;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.common.annotation.KanriInsEvent;
import xin.vanilla.common.annotation.KeywordInsEvent;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.event.events.GroupMessageEvents;
import xin.vanilla.enumeration.PermissionLevel;
import xin.vanilla.util.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static xin.vanilla.enumeration.DataCacheKey.PLUGIN_BOT_ONLINE_TIME;

@SuppressWarnings("unused")
public class EventHandlers extends SimpleListenerHost {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;

    @Override
    public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
        // 是否已启用调试模式
        if (!Va.getGlobalConfig().getBase().getCapability().getDebug()) return;

        // 处理事件处理时抛出的异常
        Event event = ((ExceptionInEventHandlerException) exception).getEvent();

        exception = getBaseException(exception);

        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        String eString = stringWriter.getBuffer().toString();

        final int ENABLE_GROUP = 1;     // 启用群
        final int ENABLE_FRIEND = 2;    // 启用好友
        final int ENABLE_BACKEND = 4;   // 启用后台

        final int PRINT_SIMPLE_EXCEPTION = 10;   // 精简异常
        final int PRINT_FULL_EXCEPTION = 20;      // 完整异常

        int type = Va.getGlobalConfig().getBase().getDebugMode();

        // 检查是否打印精简异常
        if (type >= 10 && type < 20) {
            eString = StringUtils.getByLine(eString, 1, 5, "... [num] more");
        }

        // 自定义异常格式
        Map<String, String> map = Va.getGlobalConfig().getBase().getDebugCustomException();
        if (!map.isEmpty()) {
            for (String key : map.keySet()) {
                if (eString.matches(key)) {
                    eString = eString.replaceAll(key, map.get(key));
                }
            }
        }

        type %= 10;
        if (event instanceof GroupMessageEvent && (type & ENABLE_GROUP) != 0) {
            GroupMessageEvent groupMessageEvent = (GroupMessageEvent) event;
            Api.sendMessage(groupMessageEvent.getGroup(), eString);
        }
        if (event instanceof FriendMessageEvent && (type & ENABLE_FRIEND) != 0) {
            FriendMessageEvent friendMessageEvent = (FriendMessageEvent) event;
            Api.sendMessage(friendMessageEvent.getFriend(), eString);
        }
        if (event instanceof BotEvent && (type & ENABLE_BACKEND) != 0) {
            BotEvent botEvent = (BotEvent) event;
            Set<Long> groups = SettingsUtils.getBackGroup(0);
            for (Long groupId : groups) {
                // 获取该机器人账号下的后台管理群对象
                Group backGroup = Bot.getInstance(botEvent.getBot().getId()).getGroup(groupId);
                Api.sendMessage(backGroup, eString);
            }
        }

    }

    /**
     * 监听指令消息
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onMessage(@NotNull MessageEvent event) {
        // 自增消息接收计数器
        VanillaKanri.INSTANCE.addMsgReceiveCount();

        InstructionMsgEvent insEvent = new InstructionMsgEvent(event);

        // 判断是否群管指令
        if (!insEvent.isKanriIns()) return;

        // 删除二级前缀
        if (!StringUtils.isNullOrEmpty(insEvent.getKanri().getPrefix())
                && insEvent.getIns().startsWith(insEvent.getKanri().getPrefix())) {
            if (insEvent.delPrefix(insEvent.getKanri().getPrefix())) return;
        } else if (!StringUtils.isNullOrEmpty(insEvent.getKeyword().getPrefix())
                && insEvent.getIns().startsWith(insEvent.getKeyword().getPrefix())) {
            if (insEvent.delPrefix(insEvent.getKeyword().getPrefix())) return;
        } else if (!StringUtils.isNullOrEmpty(insEvent.getTimed().getPrefix())
                && insEvent.getIns().startsWith(insEvent.getTimed().getPrefix())) {
            if (insEvent.delPrefix(insEvent.getTimed().getPrefix())) return;
        } else {
            if (insEvent.delPrefix("")) return;
        }

        // 三级前缀
        String prefix;
        int index = RegUtils.containsRegSeparator(insEvent.getIns());
        if (index >= 0) prefix = insEvent.getIns().substring(0, index);
        else prefix = insEvent.getIns();

        for (Method method : insEvent.getClass().getMethods()) {
            int back = InstructionMsgEvent.RETURN_CONTINUE;
            if (method.isAnnotationPresent(KanriInsEvent.class)) {
                KanriInsEvent annotation = method.getAnnotation(KanriInsEvent.class);

                // 判断三级前缀是否满足
                String prefixName = annotation.prefix();
                if (!StringUtils.isNullOrEmpty(prefixName)) {
                    boolean success = false;
                    for (Method kanriInsMethod : KanriInstructions.class.getMethods()) {
                        if (kanriInsMethod.getName().equalsIgnoreCase(StringUtils.METHOD_GET_PREFIX + prefixName)) {
                            try {
                                Object obj = kanriInsMethod.invoke(insEvent.getKanri());
                                if (prefixName.equalsIgnoreCase("kick")) {
                                    if (((String) obj).startsWith(prefix)) {
                                        success = true;
                                        break;
                                    }
                                } else {
                                    Set<String> values = new HashSet<>();
                                    if (obj instanceof Set) {
                                        values.addAll(((Set<?>) obj).stream().map(String::valueOf).collect(Collectors.toSet()));
                                    } else if (obj instanceof String) {
                                        values.add((String) obj);
                                    }
                                    if (values.contains(prefix)) {
                                        success = true;
                                        break;
                                    }
                                }
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                    if (!success) continue;
                }

                // 判断发送者是否拥有权限
                PermissionLevel senderLevel = annotation.sender();
                if (VanillaUtils.hasNotPermissionAndMore(insEvent.getBot(), insEvent.getGroup(), insEvent.getSender().getId(), senderLevel)) {
                    continue;
                }

                // TODO 应该修改为判断所有待操作的群是否有权限
                // 判断机器人是否有对应权限
                if (insEvent.getGroup() != null) {
                    MemberPermission[] botLevel = annotation.bot();
                    MemberPermission botPermission = insEvent.getGroup().getBotPermission();
                    if (!Arrays.stream(botLevel).collect(Collectors.toList()).contains(botPermission)) {
                        continue;
                    }
                }

                // 解析正则表达式
                String regexpName = annotation.regexp();
                try {
                    Method regMethod = RegExpConfig.class.getMethod(regexpName, String.class);
                    RegUtils reg = (RegUtils) regMethod.invoke(null, prefix);
                    if (reg.matcher(insEvent.getIns()).find()) {
                        String operation;
                        long[] groups;
                        long[] qqs;
                        try {
                            operation = reg.getMatcher().group("operation");
                        } catch (Exception e) {
                            operation = "";
                        }
                        try {
                            String groupString = reg.getMatcher().group("group");
                            if (groupString.equals(AtAll.INSTANCE.toString()) || insEvent.getBase().getAtAll().contains(groupString)) {
                                groups = insEvent.getBase().getAtAllId().stream().limit(1).mapToLong(Long::parseLong).toArray();
                            } else {
                                groups = VanillaUtils.getGroupFromString(groupString);
                            }
                        } catch (Exception e) {
                            groups = new long[]{};
                        }

                        // TODO 合理的做法应该修改为判断所有操作的群是否有对应权限
                        // 非超管及以上权限不允许同时操作多群
                        if (groups.length > 0) {
                            if (groups.length > 1 || groups[0] > 0) {
                                if (VanillaUtils.hasNotPermissionAndMore(insEvent.getBot(), null, insEvent.getSender().getId(), PermissionLevel.PERMISSION_LEVEL_SUPER_ADMIN)) {
                                    Api.sendMessage(insEvent.getGroup(), "权限不足");
                                    return;
                                }
                            }
                        }

                        try {
                            String qqString = reg.getMatcher().group("qq");
                            if (qqString.equals(AtAll.INSTANCE.toString()) || insEvent.getBase().getAtAll().contains(qqString)) {
                                qqs = insEvent.getBase().getAtAllId().stream().limit(1).mapToLong(Long::parseLong).toArray();
                            } else {
                                qqs = VanillaUtils.getQQFromString(qqString);
                            }
                        } catch (Exception e) {
                            qqs = new long[]{};
                        }

                        back = (int) method.invoke(insEvent, groups, qqs, operation);
                    }
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }

            } else if (method.isAnnotationPresent(KeywordInsEvent.class)) {
                try {
                    back = (int) method.invoke(insEvent, prefix);

                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            // 根据返回值判断是否继续执行事件
            if (back == InstructionMsgEvent.RETURN_BREAK_TRUE) {
                event.intercept();
                return;
            } else if (back == InstructionMsgEvent.RETURN_BREAK_FALSE)
                return;

            // TODO 解析定时任务指令
            // else if (method.isAnnotationPresent(TimedInsEvent.class)) {
            //
            // }

        }
    }

    /**
     * 监听群消息
     */
    @EventHandler
    public void onGroupMessage(@NotNull GroupMessageEvent event) {
        GroupMsgEvent groupMsgEvent = new GroupMsgEvent(new GroupMessageEvents(event));
        groupMsgEvent.encodeVaEvent();
        groupMsgEvent.run();
    }

    /**
     * 监听好友消息
     */
    @EventHandler
    public void onFriendMessage(@NotNull FriendMessageEvent event) {
        FriendMsgEvent friendMsgEvent = new FriendMsgEvent(event);
        friendMsgEvent.encodeVaEvent();
        friendMsgEvent.run();
    }

    /**
     * 监听群临时会话消息
     */
    @EventHandler
    public void onGroupTempMessage(@NotNull GroupTempMessageEvent event) {
        new GroupTempMsgEvent(event).run();
    }

    /**
     * 监听陌生人消息
     */
    @EventHandler
    public void onStrangerMessage(@NotNull StrangerMessageEvent event) {
        new StrangerMsgEvent(event).run();
    }

    /**
     * 监听其他客户端消息
     */
    @EventHandler
    public void onOtherClientMessage(@NotNull OtherClientMessageEvent event) {
        new OtherClientMsgEvent(event).run();
    }

    /**
     * 监听消息撤回事件
     */
    @EventHandler
    public void onMessageRecall(@NotNull MessageRecallEvent event) {
        new MsgRecallEvent(event).run();
    }

    /**
     * 监听机器人登录完成事件
     */
    @EventHandler
    public void onBotOnline(@NotNull BotOnlineEvent event) {
        long bot = event.getBot().getId();
        VanillaKanri.INSTANCE.getDataCache().put(PLUGIN_BOT_ONLINE_TIME.getKey(bot), System.currentTimeMillis());
    }

    /**
     * 监听戳一戳事件, 并转换为与语境匹配的消息事件
     */
    @EventHandler
    public void onTapEvent(@NotNull NudgeEvent event) {
        Contact subject = event.getSubject();
        UserOrBot sender = event.getFrom();
        UserOrBot target = event.getTarget();
        Bot bot = event.getBot();
        String action = event.getAction();
        String suffix = event.getSuffix();

        if (subject instanceof Group) {
            Group group = (Group) subject;
            // 获取发送者对象
            NormalMember normalMember = group.get(sender.getId());
            assert normalMember != null;

            // 构建群聊消息事件
            xin.vanilla.entity.event.GroupMessageEvent groupMessageEvent = new xin.vanilla.entity.event.GroupMessageEvent(
                    sender.getNick(),
                    normalMember.getPermission(),
                    normalMember,
                    buildTapMessage(sender, target, bot, action, suffix, MessageSourceKind.GROUP),
                    (int) DateUtil.currentSeconds()
            );

            // 触发群聊消息事件
            new GroupMsgEvent(new GroupMessageEvents(groupMessageEvent)).run();
        }
        // else if (subject instanceof Stranger) {
        //
        // }
        else if (subject instanceof Friend) {
            Friend friend = (Friend) subject;
            MessageChainBuilder singleMessages;
            FriendMessageEvent friendMessageEvent = new FriendMessageEvent(
                    friend,
                    buildTapMessage(sender, target, bot, action, suffix, MessageSourceKind.FRIEND),
                    (int) DateUtil.currentSeconds()
            );
            new FriendMsgEvent(friendMessageEvent).run();
        }
        // else if (subject instanceof Member) {
        //
        // }
    }

    /**
     * 监听被禁言事件
     */
    @EventHandler
    public void onMuteEvent(@NotNull BotMuteEvent event) {
        Group subject = event.getGroup();
        UserOrBot sender = event.getOperator();
        UserOrBot target = event.getBot();
        Bot bot = event.getBot();
        int seconds = event.getDurationSeconds();
        onMuteEvent(subject, sender, target, bot, seconds);
    }

    /**
     * 监听被禁言事件
     */
    @EventHandler
    public void onMuteEvent(@NotNull MemberMuteEvent event) {
        Group subject = event.getGroup();
        UserOrBot sender = event.getOperator();
        UserOrBot target = event.getMember();
        Bot bot = event.getBot();
        int seconds = event.getDurationSeconds();
        if (sender != null) {
            onMuteEvent(subject, sender, target, bot, seconds);
        }
    }

    /**
     * 获取最底层的异常
     */
    private Throwable getBaseException(@NotNull Throwable exception) {
        Throwable cause = exception.getCause();
        while (cause != null) {
            cause = exception.getCause();
            if (cause != null) exception = cause;
        }
        return exception;
    }

    /**
     * 构建戳一戳事件消息
     * <p>
     * <p>+代表机器人自己 -代表某人, 防止自娱自乐(</p>
     * <p><code><-tap+></code> 某人戳了机器人</p>
     * <p><code><-tap-></code> 某人戳了某人</p>
     * <p><code><+tap-></code> 机器人戳了某人</p>
     * <p><code><+tap+></code> 机器人戳了自己</p>
     */
    @NotNull
    private MessageChain buildTapMessage(@NotNull UserOrBot sender, @NotNull UserOrBot target, @NotNull Bot bot, String action, String suffix, MessageSourceKind kind) {
        MessageChainBuilder singleMessages = new MessageChainBuilder();

        // TODO 计次

        // 构建 (:vaevent:)<±tap±>
        StringBuilder prefix = new StringBuilder("(:vaevent:)");
        prefix.append("<");
        if (sender.getId() == bot.getId()) prefix.append("+");
        else prefix.append("-");
        prefix.append("tap");
        if (target.getId() == bot.getId()) prefix.append("+");
        else prefix.append("-");
        prefix.append(">");

        // 追加 (:vaevent:)<±tap±>(触发者->被戳者)
        singleMessages.append(prefix);
        singleMessages.append("(").append(String.valueOf(sender.getId())).append("->").append(String.valueOf(target.getId())).append(")\n");

        // 追加 (:vaevent:)<±tap±>(被戳者<-触发者)
        singleMessages.append(prefix);
        singleMessages.append("(").append(String.valueOf(target.getId())).append("<-").append(String.valueOf(sender.getId())).append(")\n");

        // 追加 {动作=后缀}
        singleMessages.append("{").append(action).append("=").append(suffix).append("}");

        // 追加 消息源
        singleMessages.append(new MessageSourceBuilder()
                .sender(sender.getId())
                .target(target.getId())
                .id(0)
                .internalId(0)
                .build(bot.getId(), kind));
        return singleMessages.build();
    }

    private void onMuteEvent(@NotNull Group group, @NotNull UserOrBot sender, UserOrBot target, Bot bot, int seconds) {

        // 获取发送者对象
        NormalMember normalMember = group.get(sender.getId());
        assert normalMember != null;

        // 构建群聊消息事件
        xin.vanilla.entity.event.GroupMessageEvent groupMessageEvent = new xin.vanilla.entity.event.GroupMessageEvent(
                sender.getNick(),
                normalMember.getPermission(),
                normalMember,
                buildMuteMessage(sender, target, bot, seconds),
                (int) DateUtil.currentSeconds()
        );

        // 触发群聊消息事件
        new GroupMsgEvent(new GroupMessageEvents(groupMessageEvent)).run();
    }

    /**
     * 构建禁言事件消息
     * <p>
     * <p>+代表机器人自己 -代表某人</p>
     * <p><code><-mute+></code> 某人禁言了机器人</p>
     * <p><code><-mute-></code> 某人禁言了某人</p>
     * <p><code><+mute-></code> 机器人禁言了某人</p>
     */
    private @NotNull MessageChain buildMuteMessage(@NotNull UserOrBot sender, UserOrBot target, Bot bot, int seconds) {
        MessageChainBuilder singleMessages = new MessageChainBuilder();

        StringBuilder prefix = new StringBuilder("(:vaevent:)");
        prefix.append("<");
        if (sender.getId() == bot.getId()) prefix.append("+");
        else prefix.append("-");
        prefix.append("mute");
        if (target.getId() == bot.getId()) prefix.append("+");
        else prefix.append("-");
        prefix.append(">");

        // 追加 (:vaevent:)<±mute±>(触发者->被戳者)
        singleMessages.append(prefix);
        singleMessages.append("(").append(String.valueOf(sender.getId())).append("->").append(String.valueOf(target.getId())).append(")\n");

        // 追加 (:vaevent:)<±mute±>(被戳者<-触发者)
        singleMessages.append(prefix);
        singleMessages.append("(").append(String.valueOf(target.getId())).append("<-").append(String.valueOf(sender.getId())).append(")\n");

        // 追加 {禁言时间}
        singleMessages.append("{").append(String.valueOf(seconds)).append("}");

        // 追加 消息源
        singleMessages.append(new MessageSourceBuilder()
                .sender(sender.getId())
                .target(target.getId())
                .id(0)
                .internalId(0)
                .build(bot.getId(), MessageSourceKind.GROUP));
        return singleMessages.build();
    }

}
