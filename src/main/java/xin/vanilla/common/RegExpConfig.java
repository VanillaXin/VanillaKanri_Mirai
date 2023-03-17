package xin.vanilla.common;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.config.instruction.BaseInstructions;
import xin.vanilla.entity.config.instruction.KanriInstructions;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.entity.config.instruction.TimedTaskInstructions;
import xin.vanilla.util.RegUtils;
import xin.vanilla.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;

import static xin.vanilla.util.RegUtils.REG_SEPARATOR;

public class RegExpConfig {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;

    private static final KanriInstructions kanri = Va.getGlobalConfig().getInstructions().getKanri();
    private static final KeywordInstructions keyword = Va.getGlobalConfig().getInstructions().getKeyword();
    private static final TimedTaskInstructions timed = Va.getGlobalConfig().getInstructions().getTimed();
    private static final BaseInstructions base = Va.getGlobalConfig().getInstructions().getBase();


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
                    .groupIgByName("num", "\\d{1,2}").separator()
                    .append("of").separator().append("a").separator().append("max").separator().append("of").separator()
                    .groupIgByName("max", "\\d{1,2}").separator()
                    .append("players").separator().append("online:").separator()
                    .groupIgByName("player", "[\\S ]*?").separator();


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
        // /va key sel [<group>] 精准|包含|拼音|正则 [key]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("type", keyword.getExactly(), keyword.getContain(), keyword.getPinyin(), keyword.getRegex()).separator("?")
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
                .groupIgByName("keyIds", "\\d+\\s?").end();
    }

    /**
     * 审核关键词回复指令
     */
    public static RegUtils keyExamineRegExp(String prefix) {
        // /va key add|del [<group>] 精准|包含|拼音|正则 [keyId keyId]
        return RegUtils.start().groupNon(prefix).separator()
                .groupIgByName("group", GROUP_CODE).appendIg("?").separator("?")
                .groupByName("type", keyword.getExactly(), keyword.getContain(), keyword.getPinyin(), keyword.getRegex()).separator()
                .groupIgByName("keyIds", "\\d+\\s?").end();
    }

    // endregion


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
         * 回复特殊码
         */
        public static RegUtils REPLY = new RegUtils().append("[vacode:reply]");

        /**
         * 关键词 回复内容编码
         */
        public static Map<String, String> EN_REP = new HashMap<String, String>() {{
            put("\\[mirai:at:@(?<qq>\\d{6,10})]", "[vacode:${qq}]");
            put("\\[mirai:atall]", "[vacode:@@]");
        }};

        /**
         * 关键词 回复内容解码
         */
        public static Map<String, String> DE_REP = new HashMap<String, String>() {{
            put("\\[vacode:void]", "");
            put("\\[vacode:@(?<qq>\\d{6,10})]", "[mirai:at:${qq}]");
            put("\\[vacode:@@]", AtAll.INSTANCE.serializeToMiraiCode());
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
                e.printStackTrace();
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
                e.printStackTrace();
            }
            return msg.replaceAll(RECALL.build(), "");
        }

        /**
         * 踢出
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
                e.printStackTrace();
            }
            return msg.replaceAll(KICK.build(), "");
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
                e.printStackTrace();
            }
            return messages;
        }
    }
}
