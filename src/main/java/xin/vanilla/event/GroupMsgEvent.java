package xin.vanilla.event;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import net.mamoe.mirai.contact.ContactList;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.NormalMember;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.entity.KeyRepEntity;
import xin.vanilla.entity.config.Base;
import xin.vanilla.entity.config.Other;
import xin.vanilla.entity.data.KeyData;
import xin.vanilla.entity.data.MsgCache;
import xin.vanilla.entity.event.events.GroupMessageEvents;
import xin.vanilla.enumeration.PermissionLevel;
import xin.vanilla.rcon.Rcon;
import xin.vanilla.util.Api;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.VanillaUtils;
import xin.vanilla.util.sqlite.SqliteUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xin.vanilla.common.RegExpConfig.RCON_RESULT_LIST;
import static xin.vanilla.mapper.impl.MessageCacheImpl.MSG_TYPE_GROUP;

public class GroupMsgEvent extends BaseMsgEvent {
    private final GroupMessageEvents event;
    private final Group group;
    private final Member sender;


    public GroupMsgEvent(GroupMessageEvents event) {
        super(event.getMessage(), event.getBot(), event.getTime());
        this.event = event;
        this.group = this.event.getGroup();
        this.sender = this.event.getSender();
        // 当原始事件不为null时才记录消息
        if (event.getOriginalEvent() != null) Va.getMessageCache().addMsg(this.group, this.msg);
    }

    public void run() {
        // logger.info("群聊: " + group.getId() + ":" + sender.getId() + " -> " + msg.serializeToMiraiCode());

        Base.Capability capability = Va.getGlobalConfig().getBase().getCapability();

        if (capability.getRcon()) if (rcon()) return;

        if (capability.getLocalRandomPic()) if (localRandomPic()) return;
        if (capability.getGetWife()) if (getWife()) return;
        if (capability.getQueryTest()) queryTest();

        if (capability.getChatGPT()) chatGPT();
        if (capability.getChatGPTVoice()) chatGPTVoice();
        if (capability.getOnlineRandomPic()) onlineRandomPic();
        if (capability.getOnlineAiPic()) onlineAiPic();
        searchMsg();

        // 测试
        // audioTest();
        // test();

        // 核心功能: 关键词回复
        if (capability.getKeyRep()) keyRep();
    }


    private boolean searchMsg() {
        // TODO 丢到InstructionMsgEvent中解析
        if (msg.contentToString().startsWith("/va get msgcache ")) {
            MessageChainBuilder messages = new MessageChainBuilder();
            for (SingleMessage singleMessage : msg) {
                if (singleMessage instanceof PlainText) {
                    PlainText plainText = (PlainText) singleMessage;
                    messages.add(plainText.contentToString().substring("/va get msgcache ".length()));
                } else {
                    messages.add(singleMessage);
                }
            }
            String no = VanillaUtils.serializeToJsonCode(messages.build());
            MessageSource source = msg.get(MessageSource.Key);
            assert source != null;
            List<MsgCache> msgCache = Va.getMessageCache().getMsgChainByKeyWord(no, sender.getId(), group.getId(), 0, MSG_TYPE_GROUP);
            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group).add(sender, msg);
            for (MsgCache item : msgCache) {
                Member normalMember = sender.getGroup().get(item.getSender());
                MessageChain singleMessages = VanillaUtils.deserializeJsonCode(item.getMsg());
                try {
                    if (normalMember != null)
                        forwardMessageBuilder.add(normalMember, singleMessages, (int) item.getTime());
                } catch (Exception e) {
                    break;
                }
            }
            Api.sendMessage(group, forwardMessageBuilder.build());
        }
        return false;
    }


    /**
     * 解析关键词回复
     */
    private boolean keyRep() {
        /*
         * TODO 关键词回复解析:
         *  1.需要提供的内容, 机器人ID, 发送人ID, [来源群ID], 发送时间, 消息主体, 消息ID, 被触发的关键词, 被触发的回复,
         *  2.优先解析静态特殊码: AT、时间、image图片(区别于pic图片)、随机数 等
         *  3.然后解析概率选择、条件判断 等特殊码
         *  4.接着解析群管类特殊码(防止群管类特殊码注入(不是))
         *  5.最后解析post、get、pic图片、引用回复、复读 等特殊码
         *  6.转义替换敏感数据(GPT key等)、图片消息、语音消息、文本消息 应在Api.sendMessage里面处理
         *  待实现特殊码清单(关键词):
         *  概率选择、条件判断、get、post、RCON、合并转发、GPT上下文、REP(复读消息)、黑名单、白名单、夸奖、警告、图片消息(将发送内容转为图片)、文本消息(将发送内容转为纯文本)、语音消息(将发送内容转为语音) 等
         *  待实现特殊码清单(事件):
         *  机器人入群事件、某人入群事件、机器人被踢事件、某人被踢事件、机器人被禁言事件、某人被禁言事件 等
         *  基于以上事件待实现功能:
         *  邀请人数、被禁言次数 等
         */

        // 关键词查询
        KeyData keyword = Va.getKeywordData().getKeyword(VanillaUtils.messageToString(msg), bot.getId(), -group.getId());
        if (keyword.getId() > 0) {
            MessageChain rep = RegExpConfig.VaCode.exeReply(keyword.getRepDecode(group, bot, sender, msg), msg, group);
            KeyRepEntity keyRepEntity = new KeyRepEntity(group);
            keyRepEntity.setMsg(VanillaUtils.messageToString(msg));
            keyRepEntity.setSenderId(sender.getId());
            keyRepEntity.setSenderName(sender.getNick());
            Api.sendMessage(keyRepEntity, rep);
            return true;
        }
        return false;
    }

    /**
     * 执行MC RCON指令
     *
     * @return 是否不继续执行
     */
    private boolean rcon() {
        final String prefix = "/va mc.rcon ";

        String command;
        if (msg.contentToString().startsWith(prefix)) {
            if (VanillaUtils.hasNotPermissionAndMore(bot, group, sender.getId(), PermissionLevel.PERMISSION_LEVEL_SUPER_ADMIN))
                return false;
            command = msg.contentToString().substring(prefix.length());
        } else if (msg.contentToString().equals("/list") || msg.contentToString().equals("/ls")) command = "list";
        else return false;

        try (Rcon rcon = Rcon.open(Va.getGlobalConfig().getOther().getMcRconIp(), Va.getGlobalConfig().getOther().getMcRconPort())) {
            if (rcon.authenticate(Va.getGlobalConfig().getOther().getMcRconPsw())) {
                String back = rcon.sendCommand(command);
                if (back.matches(RCON_RESULT_LIST.build())) {
                    // There are 0 of a max of 20 players online:
                    Matcher matcher = RCON_RESULT_LIST.matcher(back);
                    String player;
                    if (matcher.find()) {
                        try {
                            player = matcher.group("player");
                        } catch (IllegalStateException e) {
                            player = "";
                        }
                        if (StringUtils.isNullOrEmptyEx(player)) {
                            back = "香草世界空无一人。";
                        } else {
                            String num = matcher.group("num");
                            String max = matcher.group("max");
                            back = "香草世界有" + num + "/" + max + "个玩家在线：\n" + player.trim() + "。";
                        }
                    }
                }
                Api.sendMessage(group, back);
            } else {
                Api.sendMessage(group, "香草世界不属于你。");
            }
            return true;
        } catch (IOException e) {
            Api.sendMessage(group, "香草世界一片混沌。");
        }
        return false;
    }

    /**
     * 本地随机涩图
     *
     * @return 是否不继续执行
     */
    private boolean localRandomPic() {
        if (msg.contentToString().matches(".*?(来.?|.*?不够)[射蛇色涩瑟铯\uD83D\uDC0D].*?")) {
            String path = Va.getGlobalConfig().getOther().getHentaiPath();
            if (!StringUtils.isNullOrEmpty(path)) {
                List<Path> paths;
                if (!Va.getDataCache().containsKey(path)) {
                    getLocalPicList(path);
                }
                paths = (List<Path>) Va.getDataCache().get(path);
                long index = VanillaUtils.getDataCacheAsLong(path + "!index");
                int i1 = RandomUtil.randomInt(1, Math.max(22 - msg.contentToString().length(), 1));
                index += i1;
                VanillaUtils.setDateCache(path + "!index", index);
                if (paths.size() <= index) {
                    getLocalPicList(path);
                } else {
                    ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group)
                            .add(sender, msg);
                    for (int i = 0; i < i1; i++) {
                        forwardMessageBuilder.add(sender, new MessageChainBuilder()
                                .append(ExternalResource.uploadAsImage(paths.get((int) index - i).toFile(), group))
                                .build());
                    }
                    group.sendMessage(forwardMessageBuilder.build()).recallIn(100 * 1000);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 遍历路径及子路径下所有文件
     */
    private void getLocalPicList(String path) {
        List<Path> files;
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            files = paths.filter(Files::isRegularFile).collect(Collectors.toList());
            Collections.shuffle(files);
            Va.getDataCache().put(path, files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 抽老婆
     */
    private boolean getWife() {
        String wifePrefix = Va.getGlobalConfig().getOther().getWifePrefix();
        if (msg.contentToString().startsWith(wifePrefix)) {
            // 角色名称
            String nick = msg.contentToString().substring(wifePrefix.length());
            String nickKey = nick;
            Set<String> wifeSuffix = Va.getGlobalConfig().getOther().getWifeSuffix();
            for (String suffix : wifeSuffix) {
                if (nickKey.matches(suffix)) {
                    String key = DateUtil.format(new Date(), "yyyy.MM.dd") + "." + group.getId() + "." + sender.getId();
                    long wife = 0;
                    try {
                        // 今天已抽过的角色名称及角色
                        String[] wifeVal = Va.getPluginData().getWife().get(key).split(":");
                        wife = Long.parseLong(wifeVal[1]);
                        // 判断有无抽过其他角色
                        if (!nickKey.equals(wifeVal[0])) {
                            Api.sendMessage(group, new MessageChainBuilder()
                                    .append(new At(sender.getId()))
                                    .append(" 今天你已经有 ").append(wifeVal[0]).append(" 啦!").build());
                            return true;
                        }
                    } catch (Exception ignored) {
                    }

                    final String[] nicks = {"老婆", "老公", "男友", "女友"};
                    for (String s : nicks) {
                        if (s.equals(nickKey)) {
                            nick = "亲爱的";
                            break;
                        }
                    }

                    if (wife == 0) {
                        ContactList<NormalMember> members = group.getMembers();
                        List<Long> qqs = members.stream().map(NormalMember::getId).collect(Collectors.toList());
                        qqs.add(bot.getId());
                        wife = qqs.get((int) (Math.random() * qqs.size()));
                        Va.getPluginData().getWife().put(key, nickKey + ":" + wife);
                    }
                    NormalMember normalMember = group.get(wife);
                    assert normalMember != null;
                    Api.sendMessage(group, new MessageChainBuilder()
                            .append(new At(sender.getId()))
                            .append(" 今天你的群友").append(nick).append("是\n")
                            .append(Api.uploadImageByUrl(normalMember.getAvatarUrl(), group))
                            .append("\n『").append(normalMember.getNick()).append("』")
                            .append("(").append(String.valueOf(wife)).append(") 喵!")
                            .build());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean chatGPTVoice() {
        final String prefix = "chatGPTVoice";
        if (msg.contentToString().startsWith(prefix)) {
            String command = msg.contentToString().substring(prefix.length());

            String back = Api.chatGPT(command);
            Api.sendMessage(group, new MessageChainBuilder().append(new At(sender.getId())).append(back).build());

            String res = Api.translateToJP(back.replace("\r", "")).replace("\n", "").replace(" ", "");
            String path = Va.getGlobalConfig().getOther().getVoiceSavePath() + "\\";
            String id = IdUtil.randomUUID();
            path = path + id + ".wav";
            try {
                Process process = Runtime.getRuntime().exec(Va.getGlobalConfig().getOther().getPythonPath() + " " + Va.getGlobalConfig().getOther().getMoeGoePath() + " " + res + " " + path);
                process.waitFor();
                File file = new File(path);
                ExternalResource externalResource = ExternalResource.create(file);
                OfflineAudio offlineAudio = group.uploadAudio(externalResource);
                Api.sendMessage(group, offlineAudio);
                externalResource.close();
            } catch (Exception e) {
                Api.sendMessage(group, "可能是请求太快也可能是模型使用超时总之挂了，后续在改");
                // throw new RuntimeException(e);
            }
        }
        return false;
    }

    private boolean chatGPT() {
        if (VanillaUtils.messageToString(msg).contains(new At(bot.getId()).toString())) {
            String command = VanillaUtils.messageToPlainText(msg);
            // Api.sendMessage(group,command);
            // final String prefix = "chatGPT";
            String back = Api.chatGPT(command);
            if (StringUtils.isNullOrEmptyEx(back)) {
                Api.sendMessage(group, "可能是请求太快也可能是模型使用超时总之挂了，后续在改");
            } else {
                Api.sendMessage(group, back);
            }
        }
        return true;
    }

    private void onlineRandomPic() {
        if (msg.contentToString().matches(".*?cos.*?")) {
            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group).add(sender, msg);
            try {
                for (int i = 0; i <= 10; i++) {
                    ExternalResource ex;
                    try (HttpResponse execute = HttpRequest.get("https://picture.yinux.workers.dev").setHttpProxy("127.0.0.1", 10809).execute()) {
                        try (InputStream inputStream = execute.bodyStream()) {
                            // URL url = new URL("https://api.jrsgslb.cn/cos/url.php?return=img");
                            // InputStream inputStream = url.openConnection().getInputStream();
                            ex = ExternalResource.Companion.create(inputStream);
                        }
                        Image img = ExternalResource.uploadAsImage(ex, group);

                        forwardMessageBuilder.add(sender, new MessageChainBuilder().append(img).build());
                        ex.close();
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            group.sendMessage(forwardMessageBuilder.build());
        }
    }

    /**
     * ai绘画
     * Prompt 后接tag
     * UnPrompt 后接反向tag
     */
    private void onlineAiPic() {
        final String msgString = msg.contentToString();
        if (msgString.startsWith("/va ai draw Prompt:")) {
            String key = Va.getGlobalConfig().getOther().getAiDrawKey();
            String aiDrawUrl = Va.getGlobalConfig().getOther().getAiDrawUrl();
            // Api.sendMessage(group,msg.contentToString().substring("/va ai draw Prompt:".length(),msg.contentToString().indexOf("/UnPrompt:")));

            String prompt;
            String unprompt = "";

            String[] split = msgString.substring("/va ai draw Prompt:".length()).split("/UnPrompt:");
            prompt = split[0];
            if (split.length == 2) unprompt = split[1];

            String uri = null;
            try {
                uri = Api.aiPictureV2(prompt, unprompt);
                // Api.sendMessage(group,uri);
            } catch (Exception e) {
                Api.sendMessage(group, "请求出错");
            }

            InputStream inputStream;
            try (HttpResponse authorization = HttpRequest.get(aiDrawUrl + "/file=" + uri).header("authorization", key).timeout(1000000).execute()) {
                inputStream = authorization.bodyStream();
                try (ExternalResource externalResource = ExternalResource.Companion.create(inputStream)) {
                    Image image = ExternalResource.uploadAsImage(externalResource, group);
                    Api.sendMessage(group, new MessageChainBuilder().append(new At(sender.getId())).append(image).build());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 查询测试
     */
    private void queryTest() {
        Other.QueryTest queryTest = Va.getGlobalConfig().getOther().getQueryTest();
        String somethingPath = queryTest.getPath();
        String content = msg.contentToString();
        if (queryTest.getGroups().contains(group.getId()) && content.startsWith(queryTest.getPrefix())) {
            if (StringUtils.isNullOrEmptyEx(somethingPath)) return;
            try {
                String value = content.substring(queryTest.getPrefix().length());
                SqliteUtil sqliteUtil = SqliteUtil.getInstance(somethingPath);
                String[][] strings = sqliteUtil.getStrings2(queryTest.getSql().replaceAll("\\$\\{value}", value));
                if (strings != null && strings.length > 0)
                    Api.sendMessage(group, "查询到数据: " + StringUtils.convertToString(strings, ", ", "\n"));
                else Api.sendMessage(group, "啥也没有");
            } catch (SQLException ignored) {
            }
        }
    }


    private void fileTest() {
        final String prefix = "filetest";

        if (msg.contentToString().startsWith(prefix)) {
            String s = Va.getGlobalConfig().getChatGptKey().get();
            Api.sendMessage(group, s);
            String chatGPTKey = Va.getGlobalConfig().getOther().getChatGPTKey();

            Api.sendMessage(group, new MessageChainBuilder().append(new At(sender.getId())).append(chatGPTKey).build());
        }
    }

    private void audioTest() {
        final String prefix = "audio";
        if (msg.contentToString().startsWith(prefix)) {
            String command = msg.contentToString().substring(prefix.length());
            String res = Api.translateToJP(command);
            String path = Va.getGlobalConfig().getOther().getVoiceSavePath() + "\\";
            String id = IdUtil.randomUUID();
            path = path + id + ".wav";
            Api.sendMessage(group, new MessageChainBuilder()
                    .append(new At(sender.getId()))
                    .append(res)
                    .build());
            try {
                Process process = Runtime.getRuntime().exec(Va.getGlobalConfig().getOther().getPythonPath() + " " + Va.getGlobalConfig().getOther().getMoeGoePath() + " " + res + " " + path);

                process.waitFor();
                File file = new File(path);
                ExternalResource externalResource = ExternalResource.create(file);
                OfflineAudio offlineAudio = group.uploadAudio(externalResource);
                Api.sendMessage(group, offlineAudio);
            } catch (Exception e) {
                Api.sendMessage(group, "可能是请求太快也可能是模型使用超时总之挂了，后续在改");
                // throw new RuntimeException(e);
            }
        }

    }

    private void test() {
        // 构造消息
        MessageChain chain = new MessageChainBuilder()
                .append(new PlainText("string"))
                .append("string") // 会被构造成 PlainText 再添加, 相当于上一行
                .append(AtAll.INSTANCE)
                .append(Image.fromId("{f8f1ab55-bf8e-4236-b55e-955848d7069f}.png"))
                .build();

        // 取某类型消息
        Image image = (Image) msg.stream().filter(Image.class::isInstance).findFirst().orElse(null);

        // 撤回指定消息
        QuoteReply quote = msg.get(QuoteReply.Key);
        if (quote != null && msg.contentToString().equals("recall"))
            MessageSource.recall(quote.getSource());

        // 利用缓存的ids与internalIds撤回消息
        if (group.getId() == 851159783L) {
            // MessageSource source = msg.get(MessageSource.Key);
            // logger.info(Arrays.toString(source.getIds()));
            // logger.info(Arrays.toString(source.getInternalIds()));

            if (msg.contentToString().startsWith("/va recall by ")) {
                String s = msg.contentToString().substring("/va recall by ".length());

                int[] ids = Arrays.stream(s.substring(0, s.indexOf("|")).split(","))
                        .mapToInt(Integer::parseInt).toArray();
                int[] internalIds = Arrays.stream(s.substring(s.indexOf("|") + 1).split(","))
                        .mapToInt(Integer::parseInt).toArray();

                MessageSource.recall(new MessageSourceBuilder()
                        .sender(3085477411L)
                        .target(group.getId())
                        .id(ids)
                        .internalId(internalIds)
                        .build(bot.getId(), MessageSourceKind.GROUP));
            }
        }

        // 序列化转码消息
        if (msg.contentToString().startsWith("/va to string "))
            Api.sendMessage(group, msg.serializeToMiraiCode());

        if (msg.contentToString().startsWith("/va get msgcache ")) {
            int no = Integer.parseInt(msg.contentToString().substring("/va get msgcache ".length()));
            MessageSource source = msg.get(MessageSource.Key);
            assert source != null;
            no = source.getIds()[0] - no;
            String msgCache = Va.getMessageCache().getMsgJsonCode(String.valueOf(no), group.getId(), MSG_TYPE_GROUP);
            Api.sendMessage(group, msgCache);
        }

        // if (msg.contentToString().equals("/va get string")) {
        //     Api.sendMessage(group, "testString is: " + Va.getGlobalConfig().getMc_rcon_ip());
        // }
        // if (msg.contentToString().startsWith("/va set string ")) {
        //     String s = msg.contentToString().substring("/va set string ".length());
        //     Va.getGlobalConfig().setMc_rcon_ip(s);
        //     Api.sendMessage(group, "testString now is: " + Va.getGlobalConfig().getMc_rcon_ip());
        // }

        // if (msg.contentToString().equals("/va get int")) {
        //     Api.sendMessage(group, "testInt is: " + Va.getGlobalConfig().getMc_rcon_port());
        // }
        // if (msg.contentToString().startsWith("/va set int ")) {
        //     int s = Integer.parseInt(msg.contentToString().substring("/va set int ".length()));
        //     Va.getGlobalConfig().setMc_rcon_port(s);
        //     Api.sendMessage(group, "testInt now is: " + Va.getGlobalConfig().getMc_rcon_port());
        // }

        if (msg.contentToString().equals("/va get owner")) {
            Api.sendMessage(group, "botOwner is: " + Va.getGlobalConfig().getPermissions().get(bot.getId()).getBotOwner());
        }
        // if (msg.contentToString().startsWith("/va set owner ")) {
        //     String s = msg.contentToString().substring("/va set owner ".length());
        //     Va.getGlobalConfig().getPermissions().get(bot.getId()).setBotOwner(Long.parseLong(s));
        //     Api.sendMessage(group, "botOwner now is: " + Va.getGlobalConfig().getPermissions().get(bot.getId()).getBotOwner());
        // }

        if (msg.contentToString().equals("/va get superAdmin")) {
            Api.sendMessage(group, "superAdmin is: " + Va.getGlobalConfig().getPermissions().get(bot.getId()).getSuperAdmin());
        }
        // if (msg.contentToString().startsWith("/va set superAdmin ")) {
        //     String s = msg.contentToString().substring("/va set superAdmin ".length());
        //     Va.getGlobalConfig().getPermissions().get(bot.getId()).setSuperAdmin(new HashSet<Long>() {{
        //         addAll(Arrays.stream(s.split(" ")).map(Long::parseLong).collect(Collectors.toList()));
        //     }});
        //     Api.sendMessage(group, "superAdmin now is: " + Va.getGlobalConfig().getPermissions().get(bot.getId()).getSuperAdmin());
        // }

        // Va.config.refreshSource();

        if (msg.contentToString().equals("/va get secondaryPrefix")) {
            Api.sendMessage(group, Va.getGlobalConfig().getInstructions().getSecondaryPrefix().toString());
        }
    }
}
