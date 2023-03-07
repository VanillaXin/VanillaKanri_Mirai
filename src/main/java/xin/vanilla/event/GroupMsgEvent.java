package xin.vanilla.event;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import xin.vanilla.entity.data.KeyData;
import xin.vanilla.entity.event.events.GroupMessageEvents;
import xin.vanilla.enumeration.PermissionLevel;
import xin.vanilla.rcon.Rcon;
import xin.vanilla.util.Api;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.VanillaUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        if (event.getOriginalEvent() != null)
            Va.getMessageCache().addMsg(this.group, this.msg);
    }

    public void run() {
        if (rcon()) return;
        if (hentai()) return;
        if (keyRep()) return;
        // logger.info("群聊: " + group.getId() + ":" + sender.getId() + " -> " + msg.serializeToMiraiCode());
        // chatGpt2();
        st();
        st2();
        audioTest();
        chartGPTVoice();
        // test();
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
                Api.sendMessage(group, "Failed to authenticate");
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 涩图
     *
     * @return 是否不继续执行
     */
    private boolean hentai() {
        if (msg.contentToString().matches(".*?(来.?|.*?不够)[射蛇色涩瑟铯\uD83D\uDC0D].*?")) {
            String path = Va.getGlobalConfig().getOther().getHentaiPath();
            if (!StringUtils.isNullOrEmpty(path)) {
                List<Path> paths;
                if (!Va.getDataCache().containsKey(path)) {
                    getHentaiList(path);
                }
                paths = (List<Path>) Va.getDataCache().get(path);
                long index = VanillaUtils.getDataCacheAsLong(path + "!index");
                int i1 = RandomUtil.randomInt(1, Math.max(22 - msg.contentToString().length(), 1));
                index += i1;
                VanillaUtils.setDateCache(path + "!index", index);
                if (paths.size() <= index) {
                    getHentaiList(path);
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

    private void getHentaiList(String path) {
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
     * 解析关键词回复
     */
    private boolean keyRep() {
        // 群内关键词查询
        KeyData keyword = Va.getKeywordData().getKeyword(VanillaUtils.messageToString(msg), bot.getId(), group.getId());
        if (keyword.getId() > 0) {
            MessageChain rep = MessageChain.deserializeFromMiraiCode(keyword.getMsg(), group);
            Api.sendMessage(group, rep);
            return true;
        }

        // 全局关键词查询
        keyword = Va.getKeywordData().getKeyword(VanillaUtils.messageToString(msg), bot.getId(), -1);
        if (keyword.getId() > 0) {
            MessageChain rep = MessageChain.deserializeFromMiraiCode(keyword.getMsg(), group);
            Api.sendMessage(group, rep);
            return true;
        }

        return false;
    }


    private void chartGPTVoice() {


        final String prefix = "chatGPTVoice";
        if (msg.contentToString().startsWith(prefix)) {

            String command = msg.contentToString().substring(prefix.length());

            String bake = Api.chatGPT(command);
            Api.sendMessage(group, bake);
            String res = Api.fanyi_jp(bake.replace("\n", "".replace(" ", ""))).replace("\n", "").replace(" ", "");
//            Api.sendMessage(group,res);
            String path = "E:\\model\\temp\\";
            String id = IdUtil.randomUUID();
            path = path + id + ".wav";
            Api.sendMessage(group, res);
            try {
                Process process = Runtime.getRuntime().exec("C:\\ProgramData\\Anaconda3\\envs\\vits\\python.exe C:\\Users\\Administrator\\Desktop\\MoeGoe-master\\MoeGoe.py " + res + " " + path);

                Api.sendMessage(group, "消息执行");
                process.waitFor();
//                Path path = Paths.get("E:\\model\\dd.wav");
                File file = new File(path);
                ExternalResource externalResource = ExternalResource.create(file);
                OfflineAudio offlineAudio = group.uploadAudio(externalResource);
                Api.sendMessage(group, offlineAudio);
                externalResource.close();
            } catch (Exception e) {
                Api.sendMessage(group, "可能是请求太快也可能是模型使用超时总之挂了，后续在改");
                throw new RuntimeException(e);

            }
        }

    }

    private void audioTest() {


        final String prefix = "audio";
        if (msg.contentToString().startsWith(prefix)) {

            String command = msg.contentToString().substring(prefix.length());

            String appid = "20210126000681992";
            String salt = "112";
            String key = "X3WYhQFwg6O8cPWv7dTe";
            String q = command;
            String sign = SecureUtil.md5(appid + q + salt + key);
            System.out.println(sign);
            Map<String, Object> map = new HashMap<>();
            map.put("from", "auto");
            map.put("to", "jp");
            map.put("appid", appid);
            map.put("q", q);
            map.put("salt", salt);
            map.put("sign", sign);

            String body = HttpRequest.post("https://fanyi-api.baidu.com/api/trans/vip/translate")
                    .form(map)
                    .execute().body();
            JSONObject jsonObject1 = JSONUtil.parseObj(body);
            JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("trans_result"));
            JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
            String res = (String) jsonObject2.get("dst");
            String path = "E:\\model\\temp\\";
            String id = IdUtil.randomUUID();
            path = path + id + ".wav";
            Api.sendMessage(group, res);
            try {
                Process process = Runtime.getRuntime().exec("C:\\ProgramData\\Anaconda3\\envs\\vits\\python.exe C:\\Users\\Administrator\\Desktop\\MoeGoe-master\\MoeGoe.py " + res + " " + path);

                Api.sendMessage(group, "消息执行");
                process.waitFor();
                Thread.sleep(1000 * 10);
//                Path path = Paths.get("E:\\model\\dd.wav");
                File file = new File(path);
                ExternalResource externalResource = ExternalResource.create(file);
                OfflineAudio offlineAudio = group.uploadAudio(externalResource);
                Api.sendMessage(group, offlineAudio);
            } catch (Exception e) {
                Api.sendMessage(group, "可能是请求太快也可能是模型使用超时总之挂了，后续在改");
                throw new RuntimeException(e);

            }
        }

    }

    private void chatGpt2() {
        final String prefix = "chatGPT";
        if (msg.contentToString().startsWith(prefix)) {
            String command = msg.contentToString().substring(prefix.length());
            JSONObject jsonObject = JSONUtil.createObj();
            Map<String, Object> map = new HashMap<>();
            List<Map<String, Object>> list = new ArrayList<>();
            list.add(map);
            map.put("role", "user");
            map.put("content", command);
            jsonObject.set("model", "gpt-3.5-turbo").set("messages", list);

            try {
                String res = HttpRequest.post("https://api.openai.com/v1/chat/completions")
                        .setHttpProxy("localhost", 10808)
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer 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")
                        .body(JSONUtil.toJsonStr(jsonObject))
                        .timeout(40000)
                        .execute()
                        .body();

                JSONObject jsonObject1 = JSONUtil.parseObj(res);
                JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("choices"));
                JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
                JSONObject jsonObject3 = JSONUtil.parseObj(jsonObject2.get("message"));
                // System.out.println(jsonObject3.get("content"));
                // System.out.println(jsonObject2.get("text"));

                String bake = (String) jsonObject3.get("content");
                bake = ReUtil.delFirst("^\n+", bake);
                Api.sendMessage(group, bake);
                // System.out.println(bake);
            } catch (IORuntimeException e) {
                Api.sendMessage(group, "可能是请求太快也可能是模型使用超时总之挂了，后续在改");
                throw new RuntimeException(e);
            }
        }
    }

    private void chatGpt() {
        final String prefix = "chatGPT";
        if (msg.contentToString().startsWith(prefix)) {

            try {
                String command = msg.contentToString().substring(prefix.length());
                JSONObject jsonObject = JSONUtil.createObj();
                String[] list = {"Human:", "AI:"};
                jsonObject.set("model", "text-davinci-003")
                        .set("prompt", command)
                        .set("max_tokens", 4000)
                        .set("temperature", 0)
                        .set("top_p", 1)
                        .set("frequency_penalty", 0)
                        .set("presence_penalty", 0.6)
                        .set("stop", list);

                String result = HttpRequest.post("https://api.openai.com/v1/completions")
                        .setHttpProxy("127.0.0.1", 10808)
                        .header("Content-Type", "application/json")
                        .header("Accept-Encoding", " gzip,deflate")
                        .header("Content-Length", "1024")
                        .header("Transfer-Encoding", " chunked")
                        .header("Authorization", "Bearer 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")
                        .body(JSONUtil.toJsonStr(jsonObject))
                        .timeout(40000)
                        .execute().body();

                JSONObject jsonObject1 = JSONUtil.parseObj(result);
                JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("choices"));
                JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
                System.out.println(jsonObject2);
                System.out.println(jsonObject2.get("text"));

                String bake = (String) jsonObject2.get("text");
                bake = ReUtil.delFirst("^\n+", bake);
                // bake = StrUtil.replace(bake,"\n","");
                // StrUtil.trim(bake);
                Api.sendMessage(group, bake);
            } catch (Exception e) {
                Api.sendMessage(group, "可能是请求太快也可能是模型使用超时总之挂了，后续在改");
                throw new RuntimeException(e);
            }
        }
    }

    public void st() {
        if (msg.contentToString().matches("(来.?|.*?不够)[射蛇色涩瑟铯\uD83D\uDC0D].*?")) {
            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group).add(sender, msg);
            try {
                for (int i = 0; i <= 10; i++) {
                    URL url = new URL("https://api.jrsgslb.cn/cos/url.php?return=img");
                    InputStream inputStream = url.openConnection().getInputStream();
                    ExternalResource ex = ExternalResource.Companion.create(inputStream);
                    Image img = ExternalResource.uploadAsImage(ex, event.getSubject());

                    forwardMessageBuilder.add(sender, new MessageChainBuilder().append(img).build());
                    ex.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // group.sendMessage(forwardMessageBuilder.build());
            //
            // MessageChain chain = new MessageChainBuilder()
            //         .append(img)
            //         .build();

            // event.getSubject().sendMessage(chain);
            // ExternalResource ex = ExternalResource.Companion.create(HttpUtil.downloadBytes("https://picture.yinux.workers.dev"));
        }
    }

    public void st2() {
        if (msg.contentToString().matches(".*?cos.*?")) {
            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group).add(sender, msg);
            try {
                for (int i = 0; i <= 10; i++) {
                    ExternalResource ex;
                    try (InputStream inputStream = HttpRequest.get("https://picture.yinux.workers.dev")
                            .setHttpProxy("127.0.0.1", 10808).execute().bodyStream()) {
                        // URL url = new URL("https://api.jrsgslb.cn/cos/url.php?return=img");
                        // InputStream inputStream = url.openConnection().getInputStream();
                        ex = ExternalResource.Companion.create(inputStream);
                    }
                    Image img = ExternalResource.uploadAsImage(ex, event.getSubject());

                    forwardMessageBuilder.add(sender, new MessageChainBuilder().append(img).build());
                    ex.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            group.sendMessage(forwardMessageBuilder.build());
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
