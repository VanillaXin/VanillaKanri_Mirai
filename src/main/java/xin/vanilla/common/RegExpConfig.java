package xin.vanilla.common;

import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.DecodeKeyParam;
import xin.vanilla.entity.config.Other;
import xin.vanilla.entity.config.instruction.BaseInstructions;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.entity.config.instruction.TimerTaskInstructions;
import xin.vanilla.util.*;
import xin.vanilla.util.mcstatus.McQuery;

import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static xin.vanilla.util.RegUtils.REG_SEPARATOR;
import static xin.vanilla.util.mcstatus.McQuery.*;

public class RegExpConfig {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;

    private static final KanriInstructions kanri = Va.getGlobalConfig().getInstructions().getKanri();
    private static final KeywordInstructions keyword = Va.getGlobalConfig().getInstructions().getKeyword();
    private static final TimerTaskInstructions timer = Va.getGlobalConfig().getInstructions().getTimer();
    private static final BaseInstructions base = Va.getGlobalConfig().getInstructions().getBase();

    public static final String DATE_TIME_CODE = "(?<date>"
            + "(?<year>\\d{4})"
            + "[\\\\\\-\\. /_:年](?<month>[01]?\\d)"
            + "[\\\\\\-\\. /_:月](?<day>[0-3]?\\d)日?"
            + ")"
            + "(?<time>"
            + "(?:[\\\\\\-\\. /_:T]?(?<hour>[012]?\\d)[时点]?)"
            + "(?:[\\\\\\-\\. /_:]?(?<minute>[0-5]?\\d)分?)?"
            + "(?:[\\\\\\-\\. /_:]?(?<second>[0-5]?\\d)秒?)?"
            + "(?:[\\\\\\-\\. /_:]?(?<millisecond>\\d{1,3}))?"
            + ")?";

    public static final String QQ_CODE = "(?:(?:" +
            StringUtils.escapeExprSpecialWord(new At(2333333333L).toString()).replace("2333333333", "\\d{5,10}")
            + "|" + StringUtils.escapeExprSpecialWord(AtAll.INSTANCE.toString())
            + "|\\d{6,10})" + REG_SEPARATOR + "?)+";

    public static final String GROUP_CODE = "<(?:(?:\\d{5,10}"
            + "|" + RegUtils.processGroup(base.getThat())
            + "|" + RegUtils.processGroup(base.getGlobal()) + ")" + REG_SEPARATOR + "?)*?>";

    /**
     * MC RCON 指令返回内容: /list
     */
    public static final RegUtils RCON_RESULT_LIST =
            RegUtils.start().append("There").separator().append("are").separator()
                    .groupIgByName("num", "\\d+").separator()
                    .append("of").separator().append("a").separator().append("max").separator().append("of").separator()
                    .groupIgByName("max", "\\d+").separator()
                    .append("players").separator().append("online:").separator()
                    .groupIgByName("player", "[^:]*?")
                    .end();

    public static final RegUtils DATE_TIME = RegUtils.start().appendIg(RegExpConfig.DATE_TIME_CODE).end();


    // region 群管指令

    public static RegUtils defaultRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 设置群管理员指令
     */
    public static RegUtils adminRegExp(String prefix) {
        //  ad add <QQ>
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("operation", base.getAdd(), base.getDelete()).separator()
                .groupIgByName("qq", QQ_CODE).end();
    }

    /**
     * 设置副管指令
     */
    public static RegUtils deputyAdminRegExp(String prefix) {
        //  dad add <QQ>
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("operation", base.getAdd(), base.getDelete(), base.getSelect()).separator("?")
                .groupIgByName("qq", QQ_CODE).appendIg("?").end();
    }

    /**
     * 设置主管指令
     */
    public static RegUtils botAdminRegExp(String prefix) {
        //  bad add <QQ>
        return RegUtils.start().groupNon(prefix).separator()
                .groupByName("operation", base.getAdd(), base.getDelete(), base.getSelect()).separator("?")
                .groupIgByName("qq", QQ_CODE).appendIg("?").end();
    }

    /**
     * 设置超管指令
     */
    public static RegUtils superAdminRegExp(String prefix) {
        //  sad add <QQ>
        return botAdminRegExp(prefix);
    }

    /**
     * 设置主人指令
     */
    public static RegUtils botOwnerRegExp(String prefix) {
        //  owner add <QQ>
        return botAdminRegExp(prefix);
    }

    /**
     * 设置群名片指令
     */
    public static RegUtils cardRegExp(String prefix) {
        //  card <QQ> [CONTENT]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("qq", QQ_CODE).separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 设置群精华消息指令
     */
    public static RegUtils essenceRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator("?")
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 解除禁言指令
     */
    public static RegUtils loudRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("qq", new HashSet<String>() {{
                    add(QQ_CODE);
                    for (String s : base.getAtAll())
                        add(StringUtils.escapeExprSpecialWord(s));
                }}).end();
    }

    /**
     * 禁言指令
     */
    public static RegUtils muteRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("qq", new HashSet<String>() {{
                    add(QQ_CODE);
                    for (String s : base.getAtAll())
                        add(StringUtils.escapeExprSpecialWord(s));
                }}).separator("?")
                .groupIgByName("operation", "\\d{1,5}(?:\\.\\d{1,2})?").appendIg("?").end();
    }

    /**
     * 设置群头衔指令
     */
    public static RegUtils tagRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("qq", QQ_CODE).appendIg("?").separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    /**
     * 踢出群聊指令
     */
    public static RegUtils kickRegExp(String prefix) {
        return RegUtils.start().appendIg(kanri.getKick().replaceAll("\\s", "\\\\s")
                        .replace("[VA_CODE.QQS]", new RegUtils()
                                .groupIgByName("qq", QQ_CODE).toString())).separator("?")
                .groupIgByName("operation", "(?:0|1|真|假|是|否|true|false|y|n|Y|N)").appendIg("?")
                .groupIgByName("group", GROUP_CODE).appendIg("?")
                .end();
    }

    /**
     * 戳一戳指令
     */
    public static RegUtils tapRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("qq", QQ_CODE).separator("?")
                .groupIgByName("operation", "\\d").appendIg("?").end();
    }

    /**
     * 撤回群消息指令
     */
    public static RegUtils withdrawRegExp(String prefix) {
        return RegUtils.start().groupNon(prefix).separator("?")
                // .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("operation", ".*?").end();
    }

    // endregion 群管指令


    // region 关键词指令

    /**
     * 添加关键词回复指令
     */
    public static RegUtils keyAddRegExp(String prefix) {
        // /va key add [<group>] 精准|包含|拼音|正则 [key] rep [content]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("type", keyword.getExactly(), keyword.getContain(), keyword.getPinyin(), keyword.getRegex()).separator()
                .groupIgByName("key", ".*?").separator()
                .groupNon(keyword.getSuffix()).separator()
                .groupIgByName("rep", ".*?").end();
    }

    /**
     * 查询关键词回复指令
     */
    public static RegUtils keySelRegExp(String prefix) {
        // /va key sel [<group>] [精准|包含|拼音|正则] [key]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("type", keyword.getExactly(), keyword.getContain(), keyword.getPinyin(), keyword.getRegex()).appendIg("?").separator("?")
                .groupIgByName("key", ".*?")
                .groupIgByName("page", "\\d+").appendIg("?")
                .end();
    }

    /**
     * 删除关键词回复指令
     */
    public static RegUtils keyDelRegExp(String prefix) {
        // /va key del [<group>] 精准|包含|拼音|正则 [keyId keyId]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("type", keyword.getExactly(), keyword.getContain(), keyword.getPinyin(), keyword.getRegex()).separator()
                .groupIgByName("key", "\\[.+\\]").appendIg("?")
                .groupIgByName("keyIds", "(?:\\d+\\s?)+").end();
    }

    /**
     * 审核关键词回复指令
     */
    public static RegUtils keyExamineRegExp(String prefix) {
        // /va key add|del [<group>] 精准|包含|拼音|正则 [keyId keyId]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("type", keyword.getExactly(), keyword.getContain(), keyword.getPinyin(), keyword.getRegex()).separator()
                .groupIgByName("keyIds", "(?:\\d+\\s?)+").end();
    }

    // endregion 关键词指令


    // region 定时任务指令

    /**
     * 添加定时任务指令
     */
    public static RegUtils timerAddRegExp(String prefix) {
        // /va timer add [<group>] [exp] rep [content]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .appendIg("[\"\\'\\<\\[\\{\\(]")
                .groupIgByName("exp"
                        , "(?:\\d{1,6}(?:\\.\\d{1,4})?(?:ms|s|m|h|d|MS|S|M|H|D|Ms|mS)?"
                        , "(?:[\\d\\*\\-,\\?LW#/]+" + REG_SEPARATOR + "){4,6}(?:[\\d\\*\\-,\\?LW#/]+))"
                        , DATE_TIME_CODE)
                .appendIg("[\"\\'\\>\\]\\}\\)]").separator()
                .groupNon(timer.getSuffix()).separator()
                .groupIgByName("rep", ".*?").end();
    }

    /**
     * 查询定时任务指令
     */
    public static RegUtils timerSelRegExp(String prefix) {
        // /va timer sel [<group>]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupIgByName("page", "\\d+").appendIg("?")
                .end();
    }

    /**
     * 删除定时任务指令
     */
    public static RegUtils timerDelRegExp(String prefix) {
        // /va timer del [timerId timerId]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("keyIds", "(?:\\w+\\s?)+").end();
    }

    /**
     * 审核定时任务指令
     */
    public static RegUtils timerExamineRegExp(String prefix) {
        // /va key add|del [timerId timerId]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("keyIds", "(?:\\w+\\s?)+").end();
    }

    // endregion 定时任务指令


    /**
     * Va特殊码
     */
    public static class VaCode {
        /**
         * 禁言特殊码
         */
        public static RegUtils MUTE = new RegUtils().append("[vacode:mute:")
                .groupIgByName("time1", "\\d{1,5}(?:\\.\\d{1,2})?")
                .groupIgByName("time2", "-\\d{1,5}(?:\\.\\d{1,2})?").appendIg("?")
                .append("]");

        /**
         * 撤回特殊码
         */
        public static RegUtils RECALL = new RegUtils().append("[vacode:recall]");

        /**
         * 踢出特殊码
         */
        public static RegUtils KICK = new RegUtils().append("[vacode:kick")
                .groupIgByName("bool", ":(?:0|1|真|假|是|否|true|false|y|n|Y|N)")
                .groupIgByName("reason", ":[^:\\[\\]]*?")
                .append("]");

        /**
         * MC查询特殊码
         */
        public static RegUtils MC = new RegUtils().append("[vacode:")
                .appendIg("[Mm][Cc][Qq]uery").append(":")
                .groupIgByName("ip", "[^:]*?").append(":")
                .groupIgByName("port", "\\d{2,5}?").append(":")
                .groupIgByName("name", "[^:]*?")
                .append("]");

        /**
         * GPT请求特殊码
         */
        public static RegUtils GPT = new RegUtils().append("[vacode:chatgpt:")
                .groupIgByName("key", ".*?")
                .append("]");

        /**
         * 回复特殊码
         */
        public static RegUtils REPLY = new RegUtils().append("[vacode:reply]");

        /**
         * 戳一戳特殊码
         */
        public static RegUtils TAP = new RegUtils().append("[vacode:tap")
                .groupIgByName("qq", ":\\d{5,10}").appendIg("?")
                .groupIgByName("num", ":\\d{1}").appendIg("?")
                .append("]");

        /**
         * 回复特殊码
         */
        public static RegUtils REP = new RegUtils().append("[vacode:rep")
                .groupIgByName("prefix", ":(?:\\:|\\]|[^:\\]])+").appendIg("?")
                .groupIgByName("content", ":(?:\\:|\\]|[^:\\]])+")
                .groupIgByName("suffix", ":(?:\\:|\\]|[^:\\]])+").appendIg("?")
                .append("]");

        /**
         * 头像特殊码
         */
        public static RegUtils HEAD = new RegUtils().append("[vacode:head:")
                .groupIgByName("qq", "\\d{5,10}")
                .groupIgByName("size", ":(?:40|41|100|140|640)").appendIg("?")
                .append("]");

        /**
         * 随机群员QQ
         */
        public static RegUtils RANDOMQQ = new RegUtils().append("[vacode:randomqq")
                .groupIgByName("flag", ":[^\\]]*?").appendIg("?")
                .append("]");

        /**
         * QQ昵称
         */
        public static RegUtils NICKNAME = new RegUtils().append("[vacode:nickname:")
                .groupIgByName("qq", "\\d{5,10}")
                .append("]");

        /**
         * 关键词 回复内容编码
         */
        public static Map<String, String> EN_REP = new HashMap<String, String>() {{
            put("\\[mirai:at:@(?<qq>\\d{6,10})]", "[vacode:${qq}]");
            put("\\[mirai:atall]", "[vacode:@@]");
        }};

        /**
         * 关键词 回复内容解码
         * <p>
         * 非重要特殊码_前置解码
         */
        public static Map<String, String> DE_REP = new HashMap<String, String>() {{
            put("\\[vacode:void]", "");
            put("\\[vacode:@(?<qq>\\d{6,10})]", "[mirai:at:${qq}]");
            put("\\[vacode:@@]", AtAll.INSTANCE.serializeToMiraiCode());
        }};

        /**
         * 关键词 回复内容解码
         * <p>
         * 非重要特殊码_后置解码
         */
        public static Map<String, String> DE_REP_ = new HashMap<String, String>() {{
            put("\\(vacode;void\\)", "");
            put("\\(vacode;@(?<qq>\\d{6,10})\\)", "[mirai:at:${qq}]");
            put("\\(vacode;@@\\)", AtAll.INSTANCE.serializeToMiraiCode());
        }};

        /**
         * 关键词 触发内容编码
         */
        public static Map<String, String> EN_KEY = new HashMap<String, String>() {{
            put("\\[mirai:image:\\{(?<code>[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12})}\\.\\w{3,5}]", "[vacode:image:{${code}}]");
        }};

        /**
         * 关键词 触发内容解码
         */
        public static Map<String, String> DE_KEY = new HashMap<String, String>() {{
            put("\\[vacode:image:\\{(?<code>[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12})}]", "[mirai:image:{${code}}.mirai]");
        }};

        /**
         * 禁言
         */
        @NotNull
        public static String exeMute(String msg, NormalMember member) {
            Matcher matcher = MUTE.matcher(msg);
            try {
                if (matcher.find()) {
                    float time1 = Float.parseFloat(matcher.group("time1"));
                    float time2;
                    try {
                        time2 = Math.abs(Float.parseFloat(matcher.group("time2")));
                        if (time2 < time1) time2 = time1 + 0.01F;
                    } catch (Exception ignored) {
                        time2 = time1 + 0.01F;
                    }
                    double time = new Random().nextDouble(time1, time2);
                    member.mute((int) (time * 60));
                }
            } catch (Exception e) {
                Va.getLogger().error(e);
            }
            return msg.replaceAll(MUTE.build(), "");
        }

        /**
         * 撤回
         */
        @NotNull
        public static String exeRecall(String msg, MessageChain messageChain) {
            Matcher matcher = RECALL.matcher(msg);
            try {
                if (matcher.find()) {
                    try {
                        MessageSource recall = messageChain.get(MessageSource.Key);
                        if (recall != null) MessageSource.recall(recall);
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception e) {
                Va.getLogger().error(e);
            }
            return msg.replaceAll(RECALL.build(), "");
        }

        /**
         * 踢出
         *
         * @param word 触发规则
         */
        @NotNull
        public static String exeKick(String word, String msg, NormalMember member) {
            Matcher matcher = KICK.matcher(msg);
            try {
                if (matcher.find()) {
                    boolean bool = StringUtils.stringToBoolean(matcher.group("bool"));
                    String reason;
                    try {
                        reason = (matcher.group("reason"));
                    } catch (Exception ignored) {
                        reason = "触发了关键词: " + word;
                    }
                    member.kick(reason, bool);
                }
            } catch (Exception e) {
                Va.getLogger().error(e);
            }
            return msg.replaceAll(KICK.build(), "");
        }

        /**
         * Minecraft服务器信息查询
         */
        @NotNull
        public static String exeMcQuery(String msg) {
            RegUtils regUtils = MC;
            try {
                while (regUtils.matcher(msg).find()) {
                    Matcher matcher = regUtils.getMatcher();
                    String ip = matcher.group("ip");
                    int port = 25565;
                    try {
                        port = Integer.parseInt(matcher.group("port"));
                    } catch (NumberFormatException ignored) {
                    }
                    String name = matcher.group("name");
                    McQuery mcQuery = new McQuery(name, ip + ":" + port);
                    mcQuery.query();
                    StringBuilder info = new StringBuilder();
                    Other.Mc mcConf = Va.getGlobalConfig().getOther().getMc();
                    if (StringUtils.isNotNullOrEmpty(mcQuery.error())) {
                        switch (mcQuery.error()) {
                            case ERROR_MSG_CONNECT_FAILED:
                                if (CollectionUtils.isNullOrEmpty(mcConf.getConnectFailed())) {
                                    info.append(mcQuery.serverName())
                                            .append(":").append(ERROR_MSG_CONNECT_FAILED);
                                } else {
                                    String err = CollectionUtils.getRandomElement(mcConf.getConnectFailed(), Va.getRandom());
                                    if (err.contains("%s")) {
                                        info.append(String.format(err, mcQuery.serverName()));
                                    } else {
                                        info.append(mcQuery.serverName()).append(":").append(err);
                                    }
                                }
                                break;
                            case ERROR_MSG_LOADING:
                                info.append(mcQuery.serverName())
                                        .append(":").append(ERROR_MSG_LOADING);
                                break;
                            case ERROR_MSG_UNKNOWN_HOST:
                                if (CollectionUtils.isNullOrEmpty(mcConf.getUnknownHost())) {
                                    info.append(mcQuery.serverName())
                                            .append(":").append(ERROR_MSG_UNKNOWN_HOST);
                                } else {
                                    String err = CollectionUtils.getRandomElement(mcConf.getUnknownHost(), Va.getRandom());
                                    if (err.contains("%s")) {
                                        info.append(String.format(err, mcQuery.serverName()));
                                    } else {
                                        info.append(mcQuery.serverName()).append(":").append(err);
                                    }
                                }
                                break;
                            case ERROR_MSG_UNKNOWN_RESPONSE:
                                if (CollectionUtils.isNullOrEmpty(mcConf.getUnknownResponse())) {
                                    info.append(mcQuery.serverName())
                                            .append(":").append(ERROR_MSG_UNKNOWN_RESPONSE);
                                } else {
                                    String err = CollectionUtils.getRandomElement(mcConf.getUnknownResponse(), Va.getRandom());
                                    if (err.contains("%s")) {
                                        info.append(String.format(err, mcQuery.serverName()));
                                    } else {
                                        info.append(mcQuery.serverName()).append(":").append(err);
                                    }
                                }
                                break;
                        }
                    } else if (mcQuery.onlinePlayers() == 0) {
                        String err = CollectionUtils.getRandomElement(mcConf.getNone(), Va.getRandom());
                        if (err.contains("%s")) {
                            info.append(String.format(err, mcQuery.serverName()));
                        } else {
                            info.append(mcQuery.serverName()).append(":").append(err);
                        }
                    } else {
                        String success = mcConf.getSuccess();
                        success = success.replace("[name]", mcQuery.serverName());
                        success = success.replace("[motd]", mcQuery.description());
                        success = success.replace("[host]", mcQuery.serverIp());
                        success = success.replace("[port]", String.valueOf(mcQuery.serverPort()));
                        success = success.replace("[version]", mcQuery.serverVersion());
                        success = success.replace("[players]", mcQuery.playerListString());
                        success = success.replace("[online]", String.valueOf(mcQuery.onlinePlayers()));
                        success = success.replace("[max]", String.valueOf(mcQuery.maxPlayers()));
                        info.append(success);
                    }
                    msg = msg.replaceAll(StringUtils.escapeExprSpecialWord(matcher.group(0)), info.toString());
                }
            } catch (Exception e) {
                Va.getLogger().error(e);
            }
            return msg.replaceAll(MC.build(), "");
        }

        /**
         * GPT
         */
        @NotNull
        public static String exeGpt(String word, String msg, UserOrBot user) {
            Matcher matcher = GPT.matcher(msg);
            String chatGPT = "";
            try {
                if (matcher.find()) {
                    String key = matcher.group("key");
                    chatGPT = Api.chatGPT(user.getNick(), key, word);
                }
            } catch (Exception e) {
                Va.getLogger().error(e);
            }
            return msg.replaceAll(GPT.build(), chatGPT);
        }

        /**
         * 引用回复
         */
        @NotNull
        public static MessageChain exeReply(String msg, MessageChain messageChain, Contact contact) {
            Matcher matcher = REPLY.matcher(msg);
            String result = msg.replaceAll(REPLY.build(), "");
            MessageChain messages = MessageChain.deserializeFromMiraiCode(result, contact);
            try {
                if (matcher.find()) {
                    MessageChainBuilder builder = new MessageChainBuilder();
                    builder.append(messages).append(new QuoteReply(messageChain));
                    messages = builder.build();
                }
            } catch (Exception e) {
                Va.getLogger().error(e);
            }
            return messages;
        }

        /**
         * 戳一戳
         */
        public static @NotNull String exeTap(String msg, Contact sender) {
            Matcher matcher = TAP.matcher(msg);
            try {
                if (matcher.find()) {
                    long qq;
                    try {
                        qq = Long.parseLong((matcher.group("qq")));
                    } catch (Exception ignored) {
                        qq = sender.getId();
                    }

                    int num;
                    try {
                        num = Integer.parseInt((matcher.group("num")));
                    } catch (Exception ignored) {
                        num = 1;
                    }

                    for (int i = 0; i < num; i++) {
                        long finalQq = qq;
                        Va.delayed(i * 5 * 1000L, () -> {
                            if (sender instanceof Friend) {
                                ((Friend) sender).nudge().sendTo(sender);
                            } else if (sender instanceof Member) {
                                Group group = ((Member) sender).getGroup();
                                NormalMember member = group.get(finalQq);
                                if (member != null) {
                                    member.nudge().sendTo(group);
                                } else {
                                    ((Member) sender).nudge().sendTo(group);
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                Va.getLogger().error(e);
            }
            return msg.replaceAll(TAP.build(), "");
        }

        /**
         * 复读
         */
        public static @NotNull String exeRep(DecodeKeyParam param, String msg) {
            Matcher matcher = REP.matcher(msg);
            try {
                if (param.getMsg() != null && matcher.find()) {
                    String prefix = StringUtils.substring(StringUtils.nullToEmpty(matcher.group("prefix")).replaceAll("\\\\:", ":").replaceAll("\\\\]", "]"), 1);
                    String content = StringUtils.substring(StringUtils.nullToEmpty(matcher.group("content")).replaceAll("\\\\:", ":").replaceAll("\\\\]", "]"), 1);
                    String suffix = StringUtils.substring(StringUtils.nullToEmpty(matcher.group("suffix")).replaceAll("\\\\:", ":").replaceAll("\\\\]", "]"), 1);
                    String _msg = VanillaUtils.messageToString(param.getMsg());
                    /*
                    /va key add include \va say rep [vacode:rep:[[vacode:qnumber]\]\:]

                    key:   /va say
                    word:  [vacode:rep:[[vacode:qnumber]\]\:]
                    msg:   /va say test

                    ok [196468986]: test
                     */
                    msg = msg.replaceAll(StringUtils.escapeExprSpecialWord(matcher.group(0)), content);
                    msg = _msg.replace(param.getRepWord().getWord(), msg);
                    msg = prefix + msg + suffix;
                }
            } catch (Exception e) {
                Va.getLogger().error(e);
            }
            return msg.replaceAll(REP.build(), "");
        }

        public static @NotNull String exeRandomQQ(DecodeKeyParam param, String msg) {
            RegUtils regUtils = RANDOMQQ;
            try {
                while (regUtils.matcher(msg).find()) {
                    Matcher matcher = regUtils.getMatcher();
                    Group group = param.getGroup();
                    String qq = "";
                    if (group != null) {
                        ContactList<NormalMember> members = group.getMembers();
                        List<Long> qqs = members.stream().map(NormalMember::getId).collect(Collectors.toList());
                        qqs.add(param.getBot().getId());
                        qq = String.valueOf(qqs.get((int) (Va.getRandom().nextDouble() * qqs.size())));
                    }
                    msg = msg.replaceAll(StringUtils.escapeExprSpecialWord(matcher.group(0)), qq);
                }
            } catch (Exception e) {
                Va.getLogger().error(e);
            }
            return msg.replaceAll(regUtils.build(), "");
        }

        public static @NotNull String exeNickName(DecodeKeyParam param, String msg) {
            RegUtils regUtils = NICKNAME;
            try {
                while (regUtils.matcher(msg).find()) {
                    Matcher matcher = regUtils.getMatcher();
                    long qq = Long.parseLong((matcher.group("qq")));

                    String nick = "";
                    if (param.getGroup() != null) {
                        NormalMember member = param.getGroup().get(qq);
                        if (member != null) {
                            nick = member.getNick();
                        }
                    }
                    if (nick.isEmpty()) {
                        Friend friend = param.getBot().getFriend(qq);
                        if (friend != null) {
                            nick = friend.getNick();
                        }
                    }
                    if (nick.isEmpty()) {
                        Stranger stranger = param.getBot().getStranger(qq);
                        if (stranger != null) {
                            nick = stranger.getNick();
                        }
                    }
                    msg = msg.replaceAll(StringUtils.escapeExprSpecialWord(matcher.group(0)), nick);
                }
            } catch (Exception e) {
                Va.getLogger().error(e);
            }
            return msg.replaceAll(regUtils.build(), "");
        }

        public static @NotNull String exeHead(DecodeKeyParam param, String msg) {
            RegUtils regUtils = HEAD;
            try {
                while (regUtils.matcher(msg).find()) {
                    Matcher matcher = regUtils.getMatcher();
                    long qq = Long.parseLong((matcher.group("qq")));

                    int size;
                    try {
                        size = Integer.parseInt(StringUtils.substring(matcher.group("size"), 1));
                    } catch (Exception ignored) {
                        size = AvatarSpec.ORIGINAL.getSize();
                    }
                    Image image = Frame.buildImageByUrl(StringUtils.getAvatarUrl(qq, size), param.getGroup() == null ? param.getSender() : param.getGroup());
                    String serialize = image.serializeToMiraiCode();
                    msg = msg.replaceAll(StringUtils.escapeExprSpecialWord(matcher.group(0)), serialize);
                }
            } catch (Exception e) {
                Va.getLogger().error(e);
            }
            return msg.replaceAll(HEAD.build(), "");
        }
    }
}
