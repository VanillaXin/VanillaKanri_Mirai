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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xin.vanilla.common.RegExpConfig.RCON_RESULT_LIST;

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
        if (event.getOriginalEvent() != null)
            Va.getMessageCache().addMsg(this.group, this.msg);
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

        // 测试
        // audioTest();
        // test();

        // 核心功能: 关键词回复
        if (capability.getKeyRep()) keyRep();
    }

    /**
     * 解析关键词回复
     */
    private boolean keyRep() {
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
        if (msg.contentToString().startsWith("抽")) {
            // 角色名称
            String nick = msg.contentToString().substring(1);
            String nickKey = nick;
            if (Va.getGlobalConfig().getOther().getWifePrefix().contains(nick)) {
                String key = DateUtil.format(new Date(), "yyyy.MM.dd") + "." + group.getId() + "." + sender.getId();
                long wife = 0;
                try {
                    // 今天已抽过的角色名称及角色
                    String[] wifeVal = Va.getPluginData().getWife().get(key).split(":");
                    wife = Long.parseLong(wifeVal[1]);
                    // 判断有无抽过其他角色
                    if (!nick.equals(wifeVal[0])) {
                        Api.sendMessage(group, new MessageChainBuilder()
                                .append(new At(sender.getId()))
                                .append(" 今天你已经有 ").append(wifeVal[0]).append(" 啦!").build());
                        return true;
                    }
                } catch (Exception ignored) {
                }

                final String[] nicks = {"老婆", "老公", "男友", "女友"};
                for (String s : nicks) {
                    if (s.equals(nick)) {
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
        return false;
    }

    private void chatGPTVoice() {
        final String prefix = "chatGPTVoice";
        if (msg.contentToString().startsWith(prefix)) {
            String command = msg.contentToString().substring(prefix.length());

            String back = Api.chatGPT(sender.getNick(), command);
            Api.sendMessage(group, new MessageChainBuilder()
                    .append(new At(sender.getId()))
                    .append(back).build());

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
    }

    private void chatGPT() {
        final String prefix = "chatGPT";
        if (msg.contentToString().startsWith(prefix)) {
            String command = msg.contentToString().substring(prefix.length());
            String back = Api.chatGPT(sender.getNick(), command);
            if (StringUtils.isNullOrEmptyEx(back)) {
                Api.sendMessage(group, "可能是请求太快也可能是模型使用超时总之挂了，后续在改");
            } else {
                Api.sendMessage(group, back);
            }
        }
    }

    private void onlineRandomPic() {
        if (msg.contentToString().matches(".*?cos.*?")) {
            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group).add(sender, msg);
            try {
                for (int i = 0; i <= 10; i++) {
                    ExternalResource ex;
                    try (HttpResponse execute = HttpRequest.get("https://picture.yinux.workers.dev")
                            .setHttpProxy("127.0.0.1", 10808).execute()) {
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
                uri = Api.aiPicture(prompt, unprompt);
                // Api.sendMessage(group,uri);
            } catch (Exception e) {
                Api.sendMessage(group, "请求出错");
            }

            InputStream inputStream;
            try (HttpResponse authorization = HttpRequest.get(aiDrawUrl + "/file=" + uri)
                    .header("authorization", key).timeout(1000000).execute()) {
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
                    Api.sendMessage(group, "查询到数据: " + StringUtils.convertToString(strings, "\n"));
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

    }
}
