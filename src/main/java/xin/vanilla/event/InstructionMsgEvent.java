package xin.vanilla.event;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.system.oshi.CpuInfo;
import cn.hutool.system.oshi.OshiUtil;
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import lombok.Getter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.MiraiConsole;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription;
import net.mamoe.mirai.console.util.SemVersion;
import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupTempMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;
import org.quartz.*;
import oshi.hardware.GlobalMemory;
import xin.vanilla.VanillaKanri;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.common.annotation.KanriInsEvent;
import xin.vanilla.common.annotation.KeywordInsEvent;
import xin.vanilla.common.annotation.TimerInsEvent;
import xin.vanilla.entity.TriggerEntity;
import xin.vanilla.entity.config.instruction.BaseInstructions;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.entity.config.instruction.TimerTaskInstructions;
import xin.vanilla.entity.data.KeyData;
import xin.vanilla.entity.data.MsgCache;
import xin.vanilla.entity.data.TimerData;
import xin.vanilla.util.*;
import xin.vanilla.util.sqlite.PaginationList;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static xin.vanilla.enums.PermissionLevel.*;
import static xin.vanilla.mapper.impl.MessageCacheImpl.MSG_TYPE_GROUP;

@SuppressWarnings("unused")
public class InstructionMsgEvent {
    /**
     * 跳过本次消息事件
     * <p>
     * 并交给插件内其他事件解析器处理
     */
    public static final int RETURN_CONTINUE = 1;
    /**
     * 结束本次消息事件
     */
    public static final int RETURN_BREAK_TRUE = 2;
    /**
     * 跳过本次消息事件
     * <p>
     * 并交给其他插件处理
     */
    public static final int RETURN_BREAK_FALSE = 3;

    private final VanillaKanri Va = VanillaKanri.INSTANCE;

    @Getter
    private final MessageEvent event;
    @Getter
    private final MessageChain msg;
    @Getter
    private Group group;
    @Getter
    private final User sender;
    @Getter
    private final Bot bot;
    @Getter
    private final long time;

    @Getter
    private String ins;
    @Getter
    private boolean kanriIns = false;

    @Getter
    private final KanriInstructions kanri = Va.getGlobalConfig().getInstructions().getKanri();
    @Getter
    private final KeywordInstructions keyword = Va.getGlobalConfig().getInstructions().getKeyword();
    @Getter
    private final TimerTaskInstructions timer = Va.getGlobalConfig().getInstructions().getTimer();
    @Getter
    private final BaseInstructions base = Va.getGlobalConfig().getInstructions().getBase();

    public InstructionMsgEvent(MessageEvent event) {
        this.event = event;
        this.msg = this.event.getMessage();
        if (event instanceof GroupMessageEvent) this.group = ((GroupMessageEvent) this.event).getGroup();
        if (event instanceof GroupTempMessageEvent) this.group = ((GroupTempMessageEvent) this.event).getGroup();
        this.sender = this.event.getSender();
        this.bot = this.event.getBot();
        this.time = this.event.getTime();
        this.ins = delTopPrefix(VanillaUtils.messageToString(this.msg));
    }


    // region 群管指令

    /**
     * 框架/系统信息
     */
    @KanriInsEvent(prefix = "status")
    public int status(long[] groups, long[] qqs, String text) {
        SemVersion version = MiraiConsole.INSTANCE.getVersion();
        JvmPluginDescription pluginInfo = Va.getDescription();
        StringBuilder rep = new StringBuilder();
        // 获取Git当前分支的哈希值
        String hash = Va.getResource("hash");
        rep.append(pluginInfo.getName()).append(" - v").append(pluginInfo.getVersion()).append(" : ").append(hash).append("\n");
        GlobalMemory memory = OshiUtil.getMemory();

        rep.append("消息收发: ").append(Va.getMsgReceiveCount()).append("/").append(Va.getMsgSendCount()).append("\n");

        rep.append("运行时长: ").append(Va.getRuntimeAsString()).append("\n");
        rep.append("在线时长: ").append(Va.getBotOnlineTimeAsString(bot.getId())).append("\n");

        rep.append("RAM占用: ").append(StorageUnitUtil.convert(new BigDecimal(memory.getTotal() - memory.getAvailable()), StorageUnitUtil.BYTE, StorageUnitUtil.GB, 2))
                .append("/").append(StorageUnitUtil.convert(new BigDecimal(memory.getTotal()), StorageUnitUtil.BYTE, StorageUnitUtil.GB, 2)).append("GB\n");
        CpuInfo cpuInfo = OshiUtil.getCpuInfo();
        rep.append("CPU占用: ").append(cpuInfo.getUsed()).append("%\n");

        rep.append("Mirai Console - ").append(version);
        if (group != null) Frame.sendMessage(group, rep.toString());
        else Frame.sendMessage(sender, rep.toString());
        return RETURN_BREAK_TRUE;
    }

    /**
     * 戳一戳
     */
    @KanriInsEvent(prefix = "tap"
            , regexp = "tapRegExp")
    public int tap(long @NotNull [] groups, long[] qqs, String num) {
        if (groups.length == 0) groups = new long[]{0};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            int i;
            try {
                i = Integer.parseInt(num);
            } catch (NumberFormatException ignored) {
                i = 1;
            }
            // 操作频率限制
            String tapTimeKey = StringUtils.getTapTimeKey(thatGroup.getId(), sender.getId());
            long last = VanillaUtils.getDataCacheAsLong(tapTimeKey);
            if (last > new Date().getTime()) {
                Frame.sendMessage(thatGroup, "操作太快啦，休息一下吧。");
                return RETURN_CONTINUE;
            } else {
                VanillaUtils.setDateCache(tapTimeKey, i * 10L * 1000L + new Date().getTime());
            }

            for (int j = 0; j < i; j++) {
                Va.delayed(j * 5 * 1000L, () -> {
                    for (long qq : qqs) {
                        NormalMember normalMember = thatGroup.get(qq);
                        if (normalMember != null) {
                            normalMember.nudge().sendTo(thatGroup);
                        }
                    }
                });
            }
            if (qqs.length == 0)
                Frame.sendMessage(thatGroup, "待操作对象为空");
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 修改群名片
     */
    @KanriInsEvent(prefix = "card"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER}
            , regexp = "cardRegExp")
    public int card(long @NotNull [] groups, long @NotNull [] qqs, String text) {
        if (groups.length == 0) groups = new long[]{0};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            StringBuilder rep = new StringBuilder();
            for (long qq : qqs) {
                rep.append(',');
                NormalMember normalMember = thatGroup.get(qq);
                if (normalMember != null) {
                    normalMember.setNameCard(text);
                    rep.append(qq);
                }
            }
            if (!StringUtils.isNullOrEmpty(rep.toString())) {
                rep.delete(0, 1);
                if (rep.toString().isEmpty()) {
                    Frame.sendMessage(thatGroup, "操作失败");
                } else {
                    if (StringUtils.isNullOrEmpty(text))
                        Frame.sendMessage(thatGroup, "已清除 " + rep + " 的名片");
                    else
                        Frame.sendMessage(thatGroup, "已将 " + rep + " 的名片修改为:\n" + text);
                }
            } else {
                Frame.sendMessage(thatGroup, "待操作对象为空");
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 设置群专属头衔
     */
    @KanriInsEvent(prefix = "tag"
            , bot = {MemberPermission.OWNER}
            , regexp = "tagRegExp")
    public int tag(long @NotNull [] groups, long[] qqs, String tag) {
        if (groups.length == 0) groups = new long[]{0};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            if (StringUtils.isNullOrEmpty(tag)) {
                NormalMember normalMember = thatGroup.get(sender.getId());
                if (normalMember != null) {
                    normalMember.setSpecialTitle(tag);
                    Frame.sendMessage(thatGroup, "已将阁下的头衔修改为:\n" + tag);
                }
            } else {
                StringBuilder successMsg = new StringBuilder();
                for (long qq : qqs) {
                    successMsg.append(',');
                    NormalMember normalMember = thatGroup.get(qq);
                    if (normalMember != null) {
                        normalMember.setSpecialTitle(tag);
                        successMsg.append(qq);
                    }
                }
                if (!StringUtils.isNullOrEmpty(successMsg.toString())) {
                    successMsg.delete(0, 1);
                    if (successMsg.toString().isEmpty()) {
                        Frame.sendMessage(thatGroup, "操作失败");
                    } else {
                        Frame.sendMessage(thatGroup, "已修改 " + successMsg + " 的头衔为:\n" + tag);
                    }
                } else {
                    Frame.sendMessage(thatGroup, "待操作对象为空");
                }
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 设置群精华消息
     */
    @KanriInsEvent(prefix = "essence"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER}
            , regexp = "essenceRegExp")
    public int essence(long @NotNull [] groups, long[] qqs, String text) {
        if (groups.length == 0) groups = new long[]{0};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            if (kanri.getEssence().contains(ins) || base.getAdd().contains(text)) {
                QuoteReply quoteReply = msg.get(QuoteReply.Key);
                if (quoteReply != null) {
                    if (thatGroup.setEssenceMessage(quoteReply.getSource())) {
                        Frame.sendMessage(thatGroup, new MessageChainBuilder().append(quoteReply).append("已将该消息设为精华").build());
                    } else {
                        Frame.sendMessage(thatGroup, "精华消息设置失败");
                    }
                }
            }
            // else if (base.getDelete().contains(text)) {
            // 未发现取消精华消息接口
            // QuoteReply quoteReply = msg.get(QuoteReply.Key);
            // if (quoteReply != null) {
            // }
            // }
            else {
                MessageReceipt<Contact> contactMessageReceipt = Frame.sendMessage(thatGroup, MessageChain.deserializeFromMiraiCode(text, thatGroup));
                if (!thatGroup.setEssenceMessage(contactMessageReceipt.getSource())) {
                    // 设置失败就撤回消息
                    contactMessageReceipt.recall();
                    Frame.sendMessage(thatGroup, "精华消息设置失败");
                }
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 解除禁言
     */
    @KanriInsEvent(prefix = "loud"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER}
            , regexp = "loudRegExp")
    public int loud(long @NotNull [] groups, long @NotNull [] qqs, String text) {
        if (groups.length == 0) groups = new long[]{0};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            StringBuilder rep = new StringBuilder();
            if (qqs.length == 1 && base.getAtAllId().contains(String.valueOf(qqs[0]))) {
                if (thatGroup.getSettings().isMuteAll()) {
                    thatGroup.getSettings().setMuteAll(false);
                    Frame.sendMessage(thatGroup, "已关闭全体禁言");
                }
            } else {
                for (long qq : qqs) {
                    rep.append(',');
                    NormalMember normalMember = thatGroup.get(qq);
                    if (normalMember != null) {
                        if (normalMember.isMuted()) {
                            normalMember.unmute();
                            rep.append(qq);
                        }
                    }
                }
                if (!StringUtils.isNullOrEmpty(rep.toString())) {
                    if (rep.toString().equals(",")) {
                        Frame.sendMessage(thatGroup, "操作失败");
                    } else {
                        Frame.sendMessage(thatGroup, "已解除 " + rep.delete(0, 1) + " 的禁言");
                    }
                } else {
                    Frame.sendMessage(thatGroup, "待操作对象为空或未被禁言");
                }
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 禁言
     */
    @KanriInsEvent(prefix = "mute"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER}
            , regexp = "muteRegExp")
    public int mute(long @NotNull [] groups, long @NotNull [] qqs, String time) {
        if (groups.length == 0) groups = new long[]{0};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            if (qqs.length == 1 && base.getAtAllId().contains(String.valueOf(qqs[0]))) {
                if (!thatGroup.getSettings().isMuteAll()) {
                    thatGroup.getSettings().setMuteAll(true);
                    Frame.sendMessage(thatGroup, "已开启全体禁言");
                    if (!StringUtils.isNullOrEmpty(time)) {
                        Va.delayed(Math.round(Float.parseFloat(time)) * 60L * 1000L, () -> {
                            if (thatGroup.getSettings().isMuteAll()) {
                                thatGroup.getSettings().setMuteAll(false);
                            }
                        });
                        Frame.sendMessage(thatGroup, "并将在 " + time + " 分钟后关闭全体禁言");
                    }
                }
            } else if (!StringUtils.isNullOrEmpty(time)) {
                NormalMember senderMember = thatGroup.get(sender.getId());
                StringBuilder successMsg = new StringBuilder();
                for (long qq : qqs) {
                    successMsg.append(',');
                    NormalMember normalMember = thatGroup.get(qq);
                    if (normalMember != null && senderMember != null) {
                        // 比较操作者与被操作者权限
                        if (VanillaUtils.equalsPermission(bot, thatGroup, senderMember.getId(), normalMember.getId())) {
                            try {
                                normalMember.mute(Math.round(Float.parseFloat(time) * 60));
                                successMsg.append(qq);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
                if (!StringUtils.isNullOrEmpty(successMsg.toString())) {
                    successMsg.delete(0, 1);
                    if (successMsg.toString().isEmpty()) {
                        Frame.sendMessage(thatGroup, "权限不足");
                    } else {
                        Frame.sendMessage(thatGroup, "已禁言 " + successMsg + " " + time + "分钟");
                    }
                } else {
                    Frame.sendMessage(thatGroup, "待操作对象为空");
                }
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 撤回消息
     */
    @KanriInsEvent(prefix = "withdraw"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , regexp = "withdrawRegExp")
    public int withdraw(long[] groups, long[] qqs, String text) {
        MessageSource messageSource = msg.get(MessageSource.Key);
        if (messageSource != null) try {
            MessageSource.recall(messageSource);
        } catch (IllegalStateException ignored) {
            Va.getLogger().info("无权撤回或已被撤回");
        }
        QuoteReply quoteReply = msg.get(QuoteReply.Key);
        int[] sourceIds;
        if (quoteReply != null) {
            sourceIds = quoteReply.getSource().getIds();
            if (kanri.getWithdraw().contains(ins)) {
                try {
                    MessageSource.recall(quoteReply.getSource());
                } catch (IllegalStateException ignored) {
                    Va.getLogger().info("无权撤回或已被撤回");
                }
            }
        } else {
            assert messageSource != null;
            sourceIds = messageSource.getIds();
        }
        int sourceId = sourceIds[sourceIds.length - 1];
        if (!StringUtils.isNullOrEmpty(text)) {
            try {
                int id = sourceId - Integer.parseInt(text);
                recall(id);
            } catch (NumberFormatException ignored) {
                if (text.contains("-") || text.contains("~")) {
                    int[] ids = Arrays.stream(text.replace("-", "~")
                                    .split("~"))
                            .mapToInt(s -> sourceId - Integer.parseInt(s))
                            .toArray();
                    if (ids.length == 2) {
                        for (int id = ids[0]; id >= ids[1]; id--) {
                            recall(id);
                        }
                    }
                } else if (RegUtils.containsRegSeparator(text) > 0) {
                    int[] ids = Arrays.stream(text.replaceAll("\\s", " ")
                                    .split(" "))
                            .mapToInt(s -> sourceId - Integer.parseInt(s))
                            .toArray();
                    for (int id : ids) {
                        recall(id);
                    }
                } else {
                    Frame.sendMessage(group, "表达式『" + text + "』有误");
                }
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 增删群管理员
     */
    @KanriInsEvent(prefix = "admin"
            , sender = PERMISSION_LEVEL_SUPER_ADMIN
            , bot = MemberPermission.OWNER
            , regexp = "adminRegExp")
    public int admin(long @NotNull [] groups, long @NotNull [] qqs, String text) {
        if (groups.length == 0) groups = new long[]{0};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            boolean operation = base.getAdd().contains(text);
            StringBuilder rep = new StringBuilder();
            for (long qq : qqs) {
                rep.append(',');
                NormalMember normalMember = thatGroup.get(qq);
                if (normalMember != null) {
                    normalMember.modifyAdmin(operation);
                    rep.append(qq);
                }
            }

            if (!StringUtils.isNullOrEmpty(rep.toString())) {
                rep.delete(0, 1);
                if (operation)
                    Frame.sendMessage(thatGroup, "已将 " + rep + " 添加为管理员");
                else Frame.sendMessage(thatGroup, "已取消 " + rep + " 的管理员");
            } else {
                Frame.sendMessage(thatGroup, "待操作对象为空");
            }
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 增删副管
     */
    @KanriInsEvent(prefix = "deputyAdmin"
            , sender = PERMISSION_LEVEL_SUPER_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER, MemberPermission.MEMBER}
            , regexp = "deputyAdminRegExp")
    public int deputyAdmin(long @NotNull [] groups, long @NotNull [] qqs, String text) {
        if (groups.length == 0) groups = new long[]{-1};
        StringBuilder rep = new StringBuilder();
        if (qqs.length == 0 && base.getSelect().contains(text)) {
            for (long groupId : groups) {
                rep.append("\n");
                if (groupId < 0) {
                    rep.append("全局:\n");
                    rep.append(StringUtils.toString(Va.getGlobalConfig().getPermissions(bot.getId()).getDeputyAdmin()));
                }
                rep.append(groupId).append(":\n");
                rep.append(StringUtils.toString(Va.getGroupConfig().getDeputyAdmin(groupId)));
            }
            Frame.sendMessage(group, "副管列表:" + rep);
            return RETURN_BREAK_TRUE;
        }
        for (long qq : qqs) {
            rep.append(',');
            for (long groupId : groups) {
                if (groupId == 0) groupId = group.getId();
                else if (groupId < 0) groupId = -1;

                if (base.getAdd().contains(text)) {
                    if (groupId > 0) {
                        Set<Long> deputyAdmin = Va.getGroupConfig().getDeputyAdmin(group.getId());
                        deputyAdmin.add(qq);
                        Va.getGroupConfig().getDeputyAdmin().put(group.getId(), deputyAdmin);
                    } else {
                        Va.getGlobalConfig().getPermissions(bot.getId()).getDeputyAdmin().add(qq);
                    }
                } else if (base.getDelete().contains(text)) {
                    Set<Long> deputyAdmin = Va.getGroupConfig().getDeputyAdmin(group.getId());
                    deputyAdmin.remove(qq);
                    Va.getGroupConfig().getDeputyAdmin().put(group.getId(), deputyAdmin);
                    if (groupId <= 0)
                        Va.getGlobalConfig().getPermissions(bot.getId()).getDeputyAdmin().remove(qq);
                }
            }
            rep.append(qq);
        }
        if (!StringUtils.isNullOrEmpty(rep.toString())) {
            rep.delete(0, 1);
            String flag = "全局副管";
            if (groups[0] >= 0) flag = "群副管";

            if (base.getAdd().contains(text))
                Frame.sendMessage(group, "已将 " + rep + " 添加为" + flag);
            else if (base.getDelete().contains(text))
                Frame.sendMessage(group, "已取消 " + rep + " 的" + flag + "权限");
        } else {
            Frame.sendMessage(group, "待操作对象为空");
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 增删群副管
     */
    @KanriInsEvent(prefix = "deputyAdmin"
            , sender = PERMISSION_LEVEL_GROUP_OWNER
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER, MemberPermission.MEMBER}
            , regexp = "deputyAdminRegExp")
    public int groupDeputyAdmin(long @NotNull [] groups, long @NotNull [] qqs, String text) {
        if (groups.length == 0) groups = new long[]{0};
        if (groups.length > 1) return RETURN_BREAK_TRUE;
        if (groups[0] != 0 || groups[0] != this.group.getId()) return RETURN_BREAK_TRUE;

        StringBuilder rep = new StringBuilder();
        if (qqs.length == 0 && base.getSelect().contains(text)) {
            for (long groupId : groups) {
                rep.append("\n");
                rep.append(groupId).append(":\n");
                rep.append(StringUtils.toString(Va.getGroupConfig().getDeputyAdmin(groupId)));
            }
            Frame.sendMessage(group, "副管列表:" + rep);
            return RETURN_BREAK_TRUE;
        }
        for (long qq : qqs) {
            rep.append(',');
            if (base.getAdd().contains(text)) {
                Set<Long> deputyAdmin = Va.getGroupConfig().getDeputyAdmin(group.getId());
                deputyAdmin.add(qq);
                Va.getGroupConfig().getDeputyAdmin().put(group.getId(), deputyAdmin);
            } else if (base.getDelete().contains(text)) {
                Va.getGroupConfig().getDeputyAdmin(group.getId()).remove(qq);
            }
            rep.append(qq);
        }

        if (!StringUtils.isNullOrEmpty(rep.toString())) {
            rep.delete(0, 1);
            if (base.getAdd().contains(text))
                Frame.sendMessage(group, "已将 " + rep + " 添加为群副管");
            else if (base.getDelete().contains(text))
                Frame.sendMessage(group, "已取消 " + rep + " 的群副管权限");
        } else {
            Frame.sendMessage(group, "待操作对象为空");
        }

        return RETURN_BREAK_TRUE;
    }

    /**
     * 增删主管
     */
    @KanriInsEvent(prefix = "botAdmin"
            , sender = PERMISSION_LEVEL_SUPER_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER, MemberPermission.MEMBER}
            , regexp = "botAdminRegExp")
    public int botAdmin(long[] groups, long @NotNull [] qqs, String text) {
        StringBuilder rep = new StringBuilder();
        if (qqs.length == 0 && base.getSelect().contains(text)) {
            rep.append("主管列表:\n");
            rep.append(StringUtils.toString(Va.getGlobalConfig().getPermissions(bot.getId()).getBotAdmin()));
            Frame.sendMessage(group, rep.toString());
            return RETURN_BREAK_TRUE;
        }
        for (long qq : qqs) {
            rep.append(',');
            if (base.getAdd().contains(text)) {
                Va.getGlobalConfig().getPermissions(bot.getId()).getBotAdmin().add(qq);
            } else if (base.getDelete().contains(text)) {
                Va.getGlobalConfig().getPermissions(bot.getId()).getBotAdmin().remove(qq);
            }
            rep.append(qq);
        }
        if (!StringUtils.isNullOrEmpty(rep.toString())) {
            rep.delete(0, 1);

            if (base.getAdd().contains(text))
                Frame.sendMessage(group, "已将 " + rep + " 添加为主管");
            else if (base.getDelete().contains(text))
                Frame.sendMessage(group, "已取消 " + rep + " 的主管权限");
        } else {
            Frame.sendMessage(group, "待操作对象为空");
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 增删超管
     */
    @KanriInsEvent(prefix = "superAdmin"
            , sender = PERMISSION_LEVEL_BOT_OWNER
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER, MemberPermission.MEMBER}
            , regexp = "superAdminRegExp")
    public int superAdmin(long[] groups, long @NotNull [] qqs, String text) {
        StringBuilder rep = new StringBuilder();
        if (qqs.length == 0 && base.getSelect().contains(text)) {
            rep.append("超管列表:\n");
            rep.append(StringUtils.toString(Va.getGlobalConfig().getPermissions(bot.getId()).getSuperAdmin()));
            Frame.sendMessage(group, rep.toString());
            return RETURN_BREAK_TRUE;
        }
        for (long qq : qqs) {
            rep.append(',');
            if (base.getAdd().contains(text)) {
                Va.getGlobalConfig().getPermissions(bot.getId()).getSuperAdmin().add(qq);
            } else if (base.getDelete().contains(text)) {
                Va.getGlobalConfig().getPermissions(bot.getId()).getSuperAdmin().remove(qq);
            }
            rep.append(qq);
        }
        if (!StringUtils.isNullOrEmpty(rep.toString())) {
            rep.delete(0, 1);

            if (base.getAdd().contains(text))
                Frame.sendMessage(group, "已将 " + rep + " 添加为超管");
            else if (base.getDelete().contains(text))
                Frame.sendMessage(group, "已取消 " + rep + " 的超管权限");
        } else {
            Frame.sendMessage(group, "待操作对象为空");
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 增删主人
     */
    @KanriInsEvent(prefix = "botOwner"
            , sender = PERMISSION_LEVEL_SUPER_OWNER
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER, MemberPermission.MEMBER}
            , regexp = "botOwnerRegExp")
    public int botOwner(long[] groups, long @NotNull [] qqs, String text) {
        if (qqs.length == 1) {
            long qq = qqs[0];
            if (base.getAdd().contains(text)) {
                Va.getGlobalConfig().getPermissions(bot.getId()).setBotOwner(qq);
                Frame.sendMessage(group, "已将 " + qq + "设置为主人");
            } else if (base.getDelete().contains(text)) {
                if (Va.getGlobalConfig().getPermissions(bot.getId()).getBotOwner() == qq) {
                    Va.getGlobalConfig().getPermissions(bot.getId()).setBotOwner(0);
                    Frame.sendMessage(group, "已删除主人 " + qq);
                } else {
                    Frame.sendMessage(group, qq + "并不是我的主人");
                }
            }
            return RETURN_BREAK_TRUE;
        }
        Frame.sendMessage(group, "待操作对象为空");
        return RETURN_BREAK_TRUE;
    }

    /**
     * 踢出群成员
     */
    @KanriInsEvent(prefix = "kick"
            , sender = PERMISSION_LEVEL_DEPUTY_ADMIN
            , bot = {MemberPermission.ADMINISTRATOR, MemberPermission.OWNER}
            , regexp = "kickRegExp")
    public int kick(long @NotNull [] groups, long @NotNull [] qqs, String text) {
        if (groups.length == 0) groups = new long[]{0};
        for (long groupId : groups) {
            Group thatGroup;
            if (groupId <= 0) thatGroup = this.group;
            else {
                thatGroup = Bot.getInstance(this.bot.getId()).getGroup(groupId);
            }
            if (thatGroup == null) continue;

            NormalMember senderMember = thatGroup.get(sender.getId());
            StringBuilder successMsg = new StringBuilder();
            for (long qq : qqs) {
                successMsg.append(',');
                NormalMember normalMember = thatGroup.get(qq);
                if (normalMember != null && senderMember != null) {
                    // 比较操作者与被操作者权限
                    if (VanillaUtils.equalsPermission(bot, thatGroup, senderMember.getId(), normalMember.getId())) {
                        boolean bool = StringUtils.stringToBoolean(text);
                        normalMember.kick("被" + sender.getId() + "通过群管指令踢出", bool);
                        successMsg.append(qq);
                    }
                }
            }
            if (!StringUtils.isNullOrEmpty(successMsg.toString())) {
                successMsg.delete(0, 1);
                if (successMsg.toString().isEmpty()) {
                    Frame.sendMessage(thatGroup, "权限不足");
                } else {
                    Frame.sendMessage(thatGroup, "已将 " + successMsg + " 移除群聊");
                }
            } else {
                Frame.sendMessage(thatGroup, "待操作对象为空");
            }
        }
        return RETURN_BREAK_TRUE;
    }

    // endregion


    // region 关键词指令

    /**
     * 添加关键词回复
     */
    @KeywordInsEvent
    public int keyAdd(String prefix) {
        if (!base.getAdd().contains(prefix)) return RETURN_CONTINUE;
        RegUtils reg = RegExpConfig.keyAddRegExp(prefix);
        if (reg.matcher(this.ins).find()) {
            String type, key, rep;
            long[] groups = getGroups(reg);

            type = reg.getMatcher().group("type");
            key = reg.getMatcher().group("key");
            rep = reg.getMatcher().group("rep");

            MessageChain keyFormat;
            MessageChain repFormat = MessageChain.deserializeFromMiraiCode(rep.replaceAll("\\[vacode:", "[☣:").replaceAll(":chatgpt:.*?]", ":chatgpt:***]"), group);

            if (keyword.getContain().contains(type)) {
                keyFormat = MessageChain.deserializeFromMiraiCode(".*?" + key.replaceAll("\\[vacode:", "[☣:").replaceAll(":chatgpt:.*?]", ":chatgpt:***]") + ".*?", group);
            } else if (keyword.getPinyin().contains(type)) {
                key = PinyinHelper.toPinyin(key, PinyinStyleEnum.NORMAL).trim();
                keyFormat = MessageChain.deserializeFromMiraiCode(".*?" + key.replaceAll("\\[vacode:", "[☣:").replaceAll(":chatgpt:.*?]", ":chatgpt:***]") + ".*?", group);
            } else if (keyword.getRegex().contains(type)) {
                keyFormat = MessageChain.deserializeFromMiraiCode(key.replaceAll("\\[vacode:", "[☣:").replaceAll(":chatgpt:.*?]", ":chatgpt:***]"), group);
            } else {
                keyFormat = MessageChain.deserializeFromMiraiCode("^" + key.replaceAll("\\[vacode:", "[☣:").replaceAll(":chatgpt:.*?]", ":chatgpt:***]") + "$", group);
            }

            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group)
                    .add(sender, new PlainText(VanillaUtils.messageToString(msg).replaceAll("\\[vacode:chatgpt:.*?]", "[vacode:chatgpt:***]")))
                    .add(bot, new PlainText("关键词类型:\n" + StringUtils.getKeywordTypeName(type)))
                    .add(bot, new MessageChainBuilder().append("触发内容:\n").append(keyFormat).build())
                    .add(bot, new MessageChainBuilder().append("回复内容:\n").append(repFormat).build())
                    .add(bot, new PlainText("触发内容文本:"))
                    .add(bot, new PlainText(VanillaUtils.enVanillaCodeKey(key).replaceAll("\\[vacode:", "[☣:").replaceAll(":chatgpt:.*?]", ":chatgpt:***]")))
                    .add(bot, new PlainText("回复内容文本:"))
                    .add(bot, new PlainText(VanillaUtils.enVanillaCodeRep(rep).replaceAll("\\[vacode:", "[☣:").replaceAll(":chatgpt:.*?]", ":chatgpt:***]")));
            boolean tf = false;
            for (long groupId : groups) {
                int level = VanillaUtils.getPermissionLevel(bot, groupId, sender.getId()) * SettingsUtils.getKeyRadix(group.getId());
                long keyId = Va.getKeyword().addKeyword(key, rep, bot.getId(), groupId, type, time, level > 0 ? level : 1);
                if (keyId > 0) {
                    tf = true;
                    forwardMessageBuilder.add(bot, new PlainText("群号: " + (groupId == -1 ? "全局" : groupId) + "\n关键词编号: " + keyId));
                } else if (keyId == -2) {
                    forwardMessageBuilder.add(bot, new PlainText("群(" + groupId + ")\n的回复内容已到上限, 请删除后重试"));
                }
            }
            if (tf) {
                forwardMessageBuilder.add(bot, new PlainText("添加成功"));
            } else {
                forwardMessageBuilder.add(bot, new PlainText("添加失败"));
            }
            Frame.sendMessage(group, forwardMessageBuilder.build());
            return RETURN_BREAK_TRUE;
        }
        return RETURN_CONTINUE;
    }

    /**
     * 查询关键词回复
     */
    @KeywordInsEvent
    public int keySel(String prefix) {
        if (!base.getSelect().contains(prefix)) return RETURN_CONTINUE;
        RegUtils reg = RegExpConfig.keySelRegExp(prefix);
        if (reg.matcher(this.ins).find()) {
            long[] groups;
            String type, key;
            int page = 1;

            groups = getGroups(reg);
            if (groups.length > 1) {
                Frame.sendMessage(group, "表达式有误: 只能同时操作一个群");
            }

            long groupId = groups[0];
            try {
                type = reg.getMatcher().group("type");
            } catch (Exception ignored) {
                type = "";
            }
            try {
                key = reg.getMatcher().group("key");
            } catch (Exception ignored) {
                key = "";
            }
            try {
                page = Integer.parseInt(reg.getMatcher().group("page"));
            } catch (Exception ignored) {
            }

            if (page < 1) {
                Frame.sendMessage(group, "查询的页数有误");
                return RETURN_BREAK_TRUE;
            }

            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group).add(sender, msg);

            PaginationList<KeyData> keywordByPage = Va.getKeyword().getKeywordByPage(key, bot.getId(), groupId, type, page, 20);

            forwardMessageBuilder.add(bot, new PlainText(
                    "关键词类型: " + StringUtils.getKeywordTypeName(type) + "\n" +
                            "数据条数: " + keywordByPage.size() + "/" + keywordByPage.getTotalItemCount()
                            + "\n数据页数: " + keywordByPage.getCurPageNo() + "/" + keywordByPage.getTotalPageCount()
                            + "\n每页条数: " + keywordByPage.getPageItemCount()
            ));
            if (!keywordByPage.isEmpty()) {
                for (KeyData keyData : keywordByPage) {
                    forwardMessageBuilder.add(bot, new PlainText(
                            "关键词ID: " + keyData.getId() + "\n" +
                                    "关键词类型: " + StringUtils.getKeywordTypeName(keyData.getType()) + "\n" +
                                    "关键词权级: " + keyData.getLevel() + "\n" +
                                    "关键词状态: " + (keyData.getStatus() > 0 ? "已启用" : "未启用") + "\n" +
                                    "关键词内容:"
                    ));
                    forwardMessageBuilder.add(bot, MessageChain.deserializeFromMiraiCode(keyData.getWordDecode().replaceAll("\\[vacode:", "[☣:").replaceAll(":chatgpt:.*?]", ":chatgpt:***]"), group));
                    forwardMessageBuilder.add(bot, new PlainText("关键词回复:"));
                    forwardMessageBuilder.add(bot, MessageChain.deserializeFromMiraiCode(keyData.getRepDecode(true).replaceAll("\\[vacode:", "[☣:").replaceAll(":chatgpt:.*?]", ":chatgpt:***]"), group));
                }
            }
            Frame.sendMessage(group, forwardMessageBuilder.build());
            return RETURN_BREAK_TRUE;
        }
        return RETURN_CONTINUE;
    }

    /**
     * 删除关键词回复
     * <p>
     * 通过id删除
     */
    @KeywordInsEvent
    public int keyDel(String prefix) {
        if (!base.getDelete().contains(prefix)) return RETURN_CONTINUE;
        RegUtils reg = RegExpConfig.keyDelRegExp(prefix);
        if (reg.matcher(this.ins).find()) {
            // 判断发送者是否有删除关键词的权限
            if (VanillaUtils.hasNotPermissionAndMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTY_ADMIN))
                return RETURN_CONTINUE;

            long[] groups, keyIds = null;

            groups = getGroups(reg);
            if (groups.length > 1) {
                Frame.sendMessage(group, "表达式有误: 只能同时操作一个群");
            }

            String type = "";
            try {
                type = reg.getMatcher().group("type");
            } catch (Exception ignored) {
            }
            String key = "";
            try {
                key = reg.getMatcher().group("key");
            } catch (Exception ignored) {
            }
            try {
                String keyIdString = reg.getMatcher().group("keyIds");
                keyIds = Arrays.stream(keyIdString.split("\\s")).mapToLong(Long::parseLong).toArray();
            } catch (Exception ignored) {
            }
            if (StringUtils.isNullOrEmpty(key) && keyIds == null) {
                Frame.sendMessage(group, "表达式有误");
                return RETURN_BREAK_TRUE;
            }

            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group)
                    .add(sender, msg);

            long groupId = groups[0];
            if (keyIds != null) {
                for (long keyId : keyIds) {
                    int level = VanillaUtils.getPermissionLevel(bot, groupId, sender.getId()) * SettingsUtils.getKeyRadix(group.getId());
                    KeyData keywordById = Va.getKeyword().getKeywordById(keyId, type);
                    int back = Va.getKeyword().deleteKeywordById(keyId, type, level);
                    if (back > 0) {
                        forwardMessageBuilder.add(bot, new PlainText(
                                "删除成功\n" +
                                        "关键词ID: " + keyId + "\n" +
                                        "关键词类型: " + StringUtils.getKeywordTypeName(keywordById.getType()) + "\n" +
                                        "关键词权级: " + keywordById.getLevel() + "\n" +
                                        "关键词状态: " + (keywordById.getStatus() > 0 ? "已启用" : "未启用") + "\n" +
                                        "关键词内容:"
                        ));
                        forwardMessageBuilder.add(bot, MessageChain.deserializeFromMiraiCode(keywordById.getWordDecode().replaceAll("\\[vacode:", "[☣:").replaceAll(":chatgpt:.*?]", ":chatgpt:***]"), group));
                        forwardMessageBuilder.add(bot, new PlainText("关键词回复:"));
                        forwardMessageBuilder.add(bot, MessageChain.deserializeFromMiraiCode(keywordById.getRepDecode(true).replaceAll("\\[vacode:", "[☣:").replaceAll(":chatgpt:.*?]", ":chatgpt:***]"), group));
                    } else if (back == -2) {
                        forwardMessageBuilder.add(bot, new PlainText(
                                "删除失败: 权限不足\n" +
                                        "关键词ID: " + keyId + "\n" +
                                        "关键词类型: " + StringUtils.getKeywordTypeName(keywordById.getType()) + "\n" +
                                        "关键词权级: " + keywordById.getLevel() + "\n" +
                                        "关键词状态: " + (keywordById.getStatus() > 0 ? "已启用" : "未启用")
                        ));
                    } else {
                        forwardMessageBuilder.add(bot, new PlainText("删除失败\n" +
                                "关键词ID: " + keyId + "\n" +
                                "关键词类型: " + StringUtils.getKeywordTypeName(keywordById.getType())));
                    }
                }
            } else {
                int level = VanillaUtils.getPermissionLevel(bot, groupId, sender.getId()) * SettingsUtils.getKeyRadix(group.getId());
                int[] back = Va.getKeyword().deleteKeyword(key, bot.getId(), groupId, type, level);
                if (back.length > 0 && Arrays.stream(back).min().orElse(0) > 0) {
                    forwardMessageBuilder.add(bot, new PlainText("关键词: " + key + "\n删除成功\n关键词编号: " + Arrays.stream(back).mapToObj(String::valueOf).collect(Collectors.joining(" "))));
                } else if (back.length == 1 && back[0] == -2) {
                    forwardMessageBuilder.add(bot, new PlainText("关键词: " + key + "\n删除失败: 权限不足"));
                } else {
                    forwardMessageBuilder.add(bot, new PlainText("关键词: " + key + "\n删除失败"));
                }
            }
            Frame.sendMessage(group, forwardMessageBuilder.build());
            return RETURN_BREAK_TRUE;
        }
        return RETURN_CONTINUE;
    }

    private long[] getGroups(RegUtils reg) {
        long[] groups;
        try {
            String groupString = reg.getMatcher().group("group");
            if (groupString.startsWith("<") && groupString.endsWith(">")) {
                groupString = groupString.substring(1, groupString.length() - 1);
            }
            if (base.getGlobal().contains(groupString)) {
                groups = new long[]{-1};
            } else {
                groups = VanillaUtils.getGroupFromString(groupString);
                if (groups.length == 1 && groups[0] == 0) {
                    groups[0] = this.group.getId();
                }
            }
        } catch (IllegalStateException | IllegalArgumentException | NullPointerException e) {
            groups = new long[]{this.group.getId()};
        }
        return groups;
    }

    /**
     * 通过回复审核用户添加的关键词
     */
    @KeywordInsEvent
    public int keyExamine(@NotNull String prefix) {
        if (!prefix.equals(this.ins)) return RETURN_CONTINUE;

        boolean operand = false;
        if (base.getAdd().contains(prefix)) operand = true;
        else if (!base.getDelete().contains(prefix)) return RETURN_CONTINUE;

        QuoteReply quoteReply = this.msg.get(QuoteReply.Key);
        if (quoteReply == null) return RETURN_CONTINUE;
        int[] ids = quoteReply.getSource().getIds();
        int[] internalIds = quoteReply.getSource().getInternalIds();
        long fromId = quoteReply.getSource().getFromId();
        MessageChain msgChain = Va.getMessageCache().getMsgChain(
                StringUtils.toString(ids) + "|" + StringUtils.toString(internalIds),
                fromId, group.getId(), MSG_TYPE_GROUP);

        ForwardMessage forwardMessage = msgChain.get(ForwardMessage.Key);
        if (forwardMessage == null) return RETURN_BREAK_TRUE;
        String type = "";
        ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group);
        boolean tf = false;
        for (ForwardMessage.Node node : forwardMessage.getNodeList()) {
            if (node.getSenderId() != bot.getId()) continue;
            String s = node.getMessageChain().contentToString();
            if (s.startsWith("关键词类型:\n")) {
                type = StringUtils.getKeywordType(s.substring("关键词类型:\n".length()).trim());
                forwardMessageBuilder.add(bot, node.getMessageChain());
            }
            if (s.startsWith("群号: ") && s.contains("\n关键词编号: ")) {
                tf = true;
                long groupId;
                try {
                    String groupIdString = s.substring("群号: ".length(), s.indexOf("\n关键词编号: "));
                    groupId = Long.parseLong(groupIdString.equals("全局") ? "-1" : groupIdString);
                } catch (Exception e) {
                    Frame.sendMessage(group, "无法解析该消息");
                    return RETURN_BREAK_TRUE;
                }
                long keyId = Long.parseLong(s.substring(s.indexOf("\n关键词编号: ") + "\n关键词编号: ".length()));
                int level = VanillaUtils.getPermissionLevel(bot, groupId, sender.getId()) * SettingsUtils.getKeyRadix(group.getId());
                if (operand) {
                    if (Va.getKeyword().updateStatus(keyId, 1, type) > 0) {
                        forwardMessageBuilder.add(bot, new PlainText(s + "\n操作成功: 已激活关键词"));
                    } else {
                        forwardMessageBuilder.add(bot, new PlainText(s + "\n操作失败"));
                    }
                } else {
                    int back = Va.getKeyword().deleteKeywordById(keyId, type, level);
                    if (back > 0) {
                        forwardMessageBuilder.add(bot, new PlainText(s + "\n操作成功: 已删除关键词"));
                    } else if (back == -2) {
                        forwardMessageBuilder.add(bot, new PlainText(s + "\n操作失败: 权限不足"));
                    } else {
                        forwardMessageBuilder.add(bot, new PlainText(s + "\n操作失败"));
                    }
                }
            }
        }
        if (tf) {
            Frame.sendMessage(group, forwardMessageBuilder.build());
        } else {
            Frame.sendMessage(group, "无法解析该消息");
        }
        return RETURN_BREAK_TRUE;
    }

    /**
     * 通过id审核用户添加的关键词
     */
    @KeywordInsEvent
    public int keyExamineById(@NotNull String prefix) {
        if (!base.getAdd().contains(prefix)) return RETURN_CONTINUE;
        RegUtils reg = RegExpConfig.keyExamineRegExp(prefix);
        if (reg.matcher(this.ins).find()) {
            // 判断发送者是否有审核关键词的权限
            if (VanillaUtils.hasNotPermissionAndMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTY_ADMIN))
                return RETURN_CONTINUE;

            long[] groups, keyIds;
            String type;

            groups = getGroups(reg);
            if (groups.length > 1) {
                Frame.sendMessage(group, "表达式有误: 只能同时操作一个群");
            }

            type = reg.getMatcher().group("type");
            try {
                String key = reg.getMatcher().group("keyIds");
                keyIds = Arrays.stream(key.split("\\s")).mapToLong(Long::parseLong).toArray();
            } catch (Exception ignored) {
                Frame.sendMessage(group, "表达式有误");
                return RETURN_BREAK_TRUE;
            }

            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group)
                    .add(sender, msg)
                    .add(bot, new PlainText("关键词类型:\n" + type));

            for (long keyId : keyIds) {
                int back = Va.getKeyword().updateStatus(keyId, 1, type);
                if (back > 0) {
                    forwardMessageBuilder.add(bot, new PlainText("关键词编号: " + keyId + "\n操作成功: 已激活关键词"));
                } else {
                    forwardMessageBuilder.add(bot, new PlainText("关键词编号: " + keyId + "\n操作失败"));
                }
            }
            Frame.sendMessage(group, forwardMessageBuilder.build());
            return RETURN_BREAK_TRUE;
        }
        return RETURN_CONTINUE;
    }

    // endregion


    // region 定时任务指令

    @TimerInsEvent
    public int timerAdd(@NotNull String thirdPrefix) {
        if (!base.getAdd().contains(thirdPrefix)) return RETURN_CONTINUE;
        RegUtils reg = RegExpConfig.timerAddRegExp(thirdPrefix);
        if (reg.matcher(this.ins).find()) {
            String exp, rep;
            long[] groups = getGroups(reg);

            exp = reg.getMatcher().group("exp");
            rep = reg.getMatcher().group("rep");
            boolean validExpression = CronExpression.isValidExpression(exp);

            MessageChain repFormat = MessageChain.deserializeFromMiraiCode(rep, group);
            MessageChain keyFormat = MessageChain.deserializeFromMiraiCode(exp, group);

            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group)
                    .add(sender, new PlainText(VanillaUtils.messageToString(msg)))
                    .add(bot, new MessageChainBuilder().append("触发条件:\n").append(keyFormat).build())
                    .add(bot, new MessageChainBuilder().append("任务内容:\n").append(repFormat).build());
            for (long groupId : groups) {
                int level = VanillaUtils.getPermissionLevel(bot, groupId, sender.getId());

                TimerData timer = new TimerData();
                timer.setId(StringUtils.randString());
                timer.setBot(this.bot);
                timer.setBotNum(this.bot.getId());
                timer.setSender(Frame.buildPrivateChatContact(this.bot, this.sender.getId(), groupId, false));
                timer.setSenderNum(this.sender.getId());
                timer.setOnce(!(validExpression && level > 0));
                timer.setMsg(rep);
                timer.setGroupNum(groupId);
                timer.setCron(exp);

                // 构建任务触发器
                TriggerEntity triggerEntity = VanillaUtils.buildTriggerFromExp(new TriggerKey(timer.getId(), timer.getGroupNum() + ".trigger"), exp, level > 0);
                if (CollectionUtil.isNotEmpty(triggerEntity.getRunTime())) {
                    timer.setFirstTime(triggerEntity.getRunTime().stream().mapToLong(Date::getTime).min().orElse(0));
                }
                if (triggerEntity.getRunTime().size() == 1) {
                    forwardMessageBuilder.add(bot, new PlainText("未来两次执行时间: "
                            + "\n1. " + DateUtils.toDateTimeString(triggerEntity.getRunTime().get(0))
                            + "\n2. 无")
                    );
                } else if (triggerEntity.getRunTime().size() == 2) {
                    forwardMessageBuilder.add(bot, new PlainText("未来两次执行时间: "
                            + "\n1. " + DateUtils.toDateTimeString(triggerEntity.getRunTime().get(0))
                            + "\n2. " + DateUtils.toDateTimeString(triggerEntity.getRunTime().get(1)))
                    );
                } else if (!triggerEntity.getRunTime().isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < triggerEntity.getRunTime().size(); i++) {
                        sb.append("\n").append(i + 1).append(". ").append(DateUtils.toDateTimeString(triggerEntity.getRunTime().get(i)));
                    }
                    forwardMessageBuilder.add(bot, new PlainText("未来" + triggerEntity.getRunTime().size() + "次执行时间: " + sb));
                } else {
                    forwardMessageBuilder.add(bot, new PlainText("未来将不会执行该任务"));
                }

                // 构建任务, 装载任务数据
                JobDataMap jobDataMap = new JobDataMap();
                jobDataMap.put("timer", timer);
                JobDetail jobDetail = JobBuilder.newJob(TimerMsgEvent.class)
                        .withIdentity(timer.getId(), timer.getGroupNum() + ".job")
                        .usingJobData(jobDataMap)
                        .build();

                boolean tf = triggerEntity.getTrigger() != null;
                if (tf) {
                    try {
                        Va.getScheduler().scheduleJob(jobDetail, triggerEntity.getTrigger());
                        // 记录在案, 方便删除
                        if (Va.getTimerData().getTimer().containsKey(timer.getGroupNum())) {
                            Va.getTimerData().getTimer().get(timer.getGroupNum()).add(timer);
                        } else {
                            Va.getTimerData().getTimer().put(timer.getGroupNum(), new ArrayList<TimerData>() {{
                                add(timer);
                            }});
                        }
                    } catch (SchedulerException e) {
                        Va.getLogger().error(e);
                        tf = false;
                    }
                }
                forwardMessageBuilder
                        .add(bot, new PlainText("定时任务类型:\n" + (validExpression ? (level > 0 ? "cron" : "cron but once") : "once")))
                        .add(bot, new PlainText("群号: " + (groupId == 0 ? "仅好友" : groupId) + "\n编号: " + timer.getId() + "\n" + (tf ? "添加成功" : "添加失败")));
            }
            Frame.sendMessage(group, forwardMessageBuilder.build());
            return RETURN_BREAK_TRUE;
        }
        return RETURN_CONTINUE;
    }

    @TimerInsEvent
    public int timerDel(@NotNull String thirdPrefix) {
        if (!base.getDelete().contains(thirdPrefix)) return RETURN_CONTINUE;
        RegUtils reg = RegExpConfig.timerDelRegExp(thirdPrefix);
        if (reg.matcher(this.ins).find()) {
            // 判断发送者是否有删除定时任务的权限
            if (VanillaUtils.hasNotPermissionAndMore(bot, group, sender.getId(), PERMISSION_LEVEL_DEPUTY_ADMIN))
                return RETURN_CONTINUE;

            String[] keyIds;

            try {
                String key = reg.getMatcher().group("keyIds");
                keyIds = key.split("\\s");
            } catch (Exception ignored) {
                Frame.sendMessage(group, "表达式有误");
                return RETURN_BREAK_TRUE;
            }

            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group)
                    .add(sender, msg);

            for (String keyId : keyIds) {
                Map<Long, List<TimerData>> map = Va.getTimerData().getTimer();
                TimerData timerData = map.values().stream()
                        .flatMap(List::stream)
                        .filter(o -> o.getId().equals(keyId))
                        .findFirst().orElse(new TimerData());
                map.entrySet().removeIf(entry -> {
                    entry.setValue(entry.getValue().stream()
                            .filter(o -> !o.getId().equals(keyId))
                            .collect(Collectors.toList())
                    );
                    return entry.getValue().isEmpty();
                });
                boolean tf = false;
                try {
                    tf = Va.getScheduler().deleteJob(new JobKey(timerData.getId(), timerData.getGroupNum() + ".job"));
                } catch (SchedulerException e) {
                    Va.getLogger().error(e);
                }
                if (tf) {
                    forwardMessageBuilder
                            .add(bot, new MessageChainBuilder().append("定时任务编号:\n").append(keyId).build())
                            .add(bot, new MessageChainBuilder().append("群号:\n").append(String.valueOf(timerData.getGroupNum())).build())
                            .add(bot, new MessageChainBuilder().append("触发条件:\n").append(timerData.getCron()).build())
                            .add(bot, new MessageChainBuilder().append("任务内容:\n").append(timerData.getMsg()).build())
                            .add(bot, new PlainText("删除成功"));
                } else {
                    forwardMessageBuilder
                            .add(bot, new MessageChainBuilder().append("定时任务编号:\n").append(keyId).build())
                            .add(bot, new PlainText("删除失败"));
                }
            }
            Frame.sendMessage(group, forwardMessageBuilder.build());
            return RETURN_BREAK_TRUE;
        }
        return RETURN_CONTINUE;
    }

    // endregion


    // region 私有方法

    /**
     * 删除顶级指令前缀
     */
    private String delTopPrefix(String text) {
        String prefix = Va.getGlobalConfig().getInstructions().getPrefix();
        if (StringUtils.isNullOrEmpty(prefix)) {
            this.kanriIns = true;
        } else if (text.startsWith(prefix)) {
            this.kanriIns = true;
            return text.substring(prefix.length()).trim();
        }
        return msg.serializeToMiraiCode().trim();
    }

    /**
     * 判断是否包含前缀prefix
     * <p>
     * 并将删除前缀prefix后的结果保存至ins
     *
     * @return 是否失败
     */
    public boolean delPrefix(String prefix) {
        String s = this.ins;
        if (StringUtils.isNullOrEmpty(prefix)) {
            setIns(s);
            return StringUtils.isNullOrEmpty(this.ins);
        } else if (s.startsWith(prefix)) {
            setIns(s.substring(prefix.length()).trim());
            return StringUtils.isNullOrEmpty(this.ins);
        }
        return true;
    }

    private <T> String toRegString(Set<T> set) {
        return toRegString(Arrays.asList(set.toArray()));
    }

    private <T> String toRegString(List<T> list) {
        if (list == null)
            return "";
        int iMax = list.size() - 1;
        if (iMax == -1) return "";

        StringBuilder b = new StringBuilder();
        b.append("(");
        int i = 0;
        for (T t : list) {
            b.append(StringUtils.escapeExprSpecialWord(t.toString()));
            if (i == iMax)
                return b.append(')').toString();
            b.append("|");
            i++;
        }
        return b.toString();
    }

    private void setIns(String s) {
        //[ \f\n\r\t\v]
        this.ins = s.replace("\\f", "\f")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t");
    }

    /**
     * 根据id撤回群消息
     */
    private void recall(int id) {
        MsgCache msgCache = Va.getMessageCache().getMsgCache(id + "|", group.getId(), MSG_TYPE_GROUP);
        if (msgCache != null) {
            try {
                MessageSource.recall(new MessageSourceBuilder()
                        .sender(msgCache.getSender())
                        .target(msgCache.getTarget())
                        .id(msgCache.getIds())
                        .internalId(msgCache.getInternalIds())
                        .build(bot.getId(), MessageSourceKind.GROUP));
            } catch (IllegalStateException e) {
                Va.getLogger().info("无权撤回或已被撤回");
            }
        }
    }

    // endregion
}
