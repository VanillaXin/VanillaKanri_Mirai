package xin.vanilla.event;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.sun.javafx.fxml.builder.ProxyBuilder;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import sun.net.www.http.HttpClient;
import xin.vanilla.enumeration.PermissionLevel;
import xin.vanilla.rcon.Rcon;
import xin.vanilla.util.Api;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.VanillaUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
    private final GroupMessageEvent event;
    private final MessageChain msg;
    private final Group group;
    private final Member sender;
    private final Bot bot;
    private final long time;


    public GroupMsgEvent(GroupMessageEvent event) {
        this.event = event;
        this.msg = this.event.getMessage();
        this.group = this.event.getGroup();
        this.sender = this.event.getSender();
        this.bot = this.event.getBot();
        this.time = this.event.getTime();
        Va.getMessageCache().addMsg(this.group, this.msg);
    }

    public void run() {
        // logger.info("群聊: " + group.getId() + ":" + sender.getId() + " -> " + msg.serializeToMiraiCode());
        st();
        st2();
        chatGpt2();
//        if (rcon()) return;
//        if (hentai()) return;
//        test();
    }

    private void chatGpt2(){
        final String prefix = "chatGPT";
        if (msg.contentToString().startsWith(prefix)) {
            String command = msg.contentToString().substring(prefix.length());
            JSONObject jsonObject = JSONUtil.createObj();
            Map<String, Object> map = new HashMap<String, Object>();
            List list = new ArrayList<Map<String,Object>>();
            list.add(map);
            map.put("role","user");
            map.put("content",command);
            jsonObject.put("model","gpt-3.5-turbo")
                    .put("messages", list);

            try {
                String res = HttpRequest.post("https://api.openai.com/v1/chat/completions").setHttpProxy("localhost", 10808)
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
//            System.out.println(jsonObject3.get("content"));
//            System.out.println(jsonObject2.get("text"));

                String bake = (String) jsonObject3.get("content");
                bake = ReUtil.delFirst("^\n+", bake);
                Api.sendMessage(group, bake);
//            System.out.println(bake);
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
                jsonObject.put("model", "text-davinci-003").put("prompt", command).put("max_tokens", 4000).put("temperature", 0).put("top_p", 1).put("frequency_penalty", 0).put("presence_penalty", 0.6).put("stop", list);


                String result = HttpRequest.post("https://api.openai.com/v1/completions").setHttpProxy("127.0.0.1", 10808).header("Content-Type", "application/json").header("Accept-Encoding", " gzip,deflate").header("Content-Length", "1024").header("Transfer-Encoding", " chunked").header("Authorization", "Bearer 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").body(JSONUtil.toJsonStr(jsonObject)).timeout(40000).execute().body();

                JSONObject jsonObject1 = JSONUtil.parseObj(result);
                JSONArray jsonArray = JSONUtil.parseArray(jsonObject1.get("choices"));
                JSONObject jsonObject2 = JSONUtil.parseObj(jsonArray.get(0));
                System.out.println(jsonObject2);
                System.out.println(jsonObject2.get("text"));

                String bake = (String) jsonObject2.get("text");
                bake = ReUtil.delFirst("^\n+", bake);
//                bake = StrUtil.replace(bake,"\n","");
//                StrUtil.trim(bake);
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

            group.sendMessage(forwardMessageBuilder.build());


//                MessageChain chain = new MessageChainBuilder()
//                        .append(img)
//                        .build();

//                event.getSubject().sendMessage(chain);

//            ExternalResource ex = ExternalResource.Companion.create(HttpUtil.downloadBytes("https://picture.yinux.workers.dev"));

        }


    }

    public void st2() {

        if (msg.contentToString().matches(".*?cos.*?")) {
            ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group).add(sender, msg);
            try {
                for (int i = 0; i <= 10; i++) {
                    InputStream inputStream = HttpRequest.get("https://picture.yinux.workers.dev").setHttpProxy("127.0.0.1", 10808).execute().bodyStream();
//                    URL url = new URL("https://api.jrsgslb.cn/cos/url.php?return=img");
//                    InputStream inputStream = url.openConnection().getInputStream();
                    ExternalResource ex = ExternalResource.Companion.create(inputStream);
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

    /**
     * 执行MC RCON指令
     *
     * @return 是否不继续执行
     */
    private boolean rcon() {
        final String prefix = "/va mc.rcon ";

        String command;
        if (msg.contentToString().startsWith(prefix)) {
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PermissionLevel.PERMISSION_LEVEL_SUPER_ADMIN))
                return false;
            command = msg.contentToString().substring(prefix.length());
        } else if (msg.contentToString().equals("/list") || msg.contentToString().equals("/ls")) command = "list";
        else return false;

        try (Rcon rcon = Rcon.open(Va.getGlobalConfig().getMc_rcon_ip(), Va.getGlobalConfig().getMc_rcon_port())) {
            if (rcon.authenticate(Va.getGlobalConfig().getMc_rcon_psw())) {
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
        if (msg.contentToString().matches("(来.?|.*?不够)[射蛇色涩瑟铯\uD83D\uDC0D].*?")) {
            String path = Va.getGlobalConfig().getHentai_path().get();
            if (!StringUtils.isNullOrEmpty(path)) {
                List<Path> paths;
                if (!Va.getDataCache().containsKey(path)) {
                    getHentaiList(path);
                }
                paths = (List<Path>) Va.getDataCache().get(path);
                long index = VanillaUtils.getDataCacheAsLong(path + "!index");
                int i1 = RandomUtil.randomInt(1, 5);
                index += i1;
                VanillaUtils.setDateCache(path + "!index", index);
                if (paths.size() <= index) {
                    getHentaiList(path);
                } else {
                    ForwardMessageBuilder forwardMessageBuilder = new ForwardMessageBuilder(group).add(sender, msg);
                    for (int i = 0; i < i1; i++) {
                        forwardMessageBuilder.add(sender, new MessageChainBuilder().append(ExternalResource.uploadAsImage(paths.get((int) index - i).toFile(), group)).build());
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

    private void test() {
        // 构造消息
        MessageChain chain = new MessageChainBuilder().append(new PlainText("string")).append("string") // 会被构造成 PlainText 再添加, 相当于上一行
                .append(AtAll.INSTANCE).append(Image.fromId("{f8f1ab55-bf8e-4236-b55e-955848d7069f}.png")).build();

        // 取某类型消息
        Image image = (Image) msg.stream().filter(Image.class::isInstance).findFirst().orElse(null);

        // 撤回指定消息
        QuoteReply quote = msg.get(QuoteReply.Key);
        if (quote != null && msg.contentToString().equals("recall")) MessageSource.recall(quote.getSource());

        // 利用缓存的ids与internalIds撤回消息
        if (group.getId() == 851159783L) {
            // MessageSource source = msg.get(MessageSource.Key);
            // logger.info(Arrays.toString(source.getIds()));
            // logger.info(Arrays.toString(source.getInternalIds()));

            if (msg.contentToString().startsWith("/va recall by ")) {
                String s = msg.contentToString().substring("/va recall by ".length());

                int[] ids = Arrays.stream(s.substring(0, s.indexOf("|")).split(",")).mapToInt(Integer::parseInt).toArray();
                int[] internalIds = Arrays.stream(s.substring(s.indexOf("|") + 1).split(",")).mapToInt(Integer::parseInt).toArray();

                MessageSource.recall(new MessageSourceBuilder().sender(3085477411L).target(group.getId()).id(ids).internalId(internalIds).build(bot.getId(), MessageSourceKind.GROUP));
            }
        }

        // 序列化转码消息
        if (msg.contentToString().startsWith("/va to string ")) Api.sendMessage(group, msg.serializeToMiraiCode());

        if (msg.contentToString().startsWith("/va get msgcache ")) {
            int no = Integer.parseInt(msg.contentToString().substring("/va get msgcache ".length()));
            MessageSource source = msg.get(MessageSource.Key);
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
