package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;
import xin.vanilla.rcon.Rcon;
import xin.vanilla.util.Api;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.VanillaUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xin.vanilla.common.RegExp.RCON_RESULT_LIST;
import static xin.vanilla.mapper.impl.MessageCacheImpl.MSG_TYPE_GROUP;
import static xin.vanilla.util.VanillaUtils.PERMISSION_LEVEL_SUPERADMIN;

public class GroupMsgEvent extends BaseMsgEvent {
    private final GroupMessageEvent event;
    private final MessageChain msg;
    private final Group group;
    private final Member sender;
    private final Bot bot;
    private final long time;
    private final boolean isBlock;


    public GroupMsgEvent(GroupMessageEvent event) {
        this.isBlock = new InstructionMsgEvent(event).run();
        this.event = event;
        this.msg = this.event.getMessage();
        this.group = this.event.getGroup();
        this.sender = this.event.getSender();
        this.bot = this.event.getBot();
        this.time = this.event.getTime();
        Va.messageCache.addMsg(this.group, this.msg);
    }

    public void run() {
        if (isBlock) return;
        logger.info("群聊: " + group.getId() + ":" + sender.getId() + " -> " + msg.serializeToMiraiCode());
        if (rcon()) return;
        if (hentai()) return;

        test();
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
            if (!VanillaUtils.hasPermissionOrMore(bot, group, sender.getId(), PERMISSION_LEVEL_SUPERADMIN))
                return false;
            command = msg.contentToString().substring(prefix.length());
        } else if (msg.contentToString().equals("/list") || msg.contentToString().equals("/ls")) command = "list";
        else return false;

        try (Rcon rcon = Rcon.open(Va.globalConfig.getMc_rcon_ip(), Va.globalConfig.getMc_rcon_port())) {
            if (rcon.authenticate(Va.globalConfig.getMc_rcon_psw())) {
                String back = rcon.sendCommand(command);
                if (back.matches(RCON_RESULT_LIST.build())) {
                    // There are 0 of a max of 20 players online:
                    Matcher matcher = RCON_RESULT_LIST.matcher(back);
                    String player = matcher.group("player");
                    if (StringUtils.isNullOrEmptyEx(player)) {
                        back = "香草世界空无一人。";
                    } else {
                        String num = matcher.group("num");
                        String max = matcher.group("max");
                        back = "香草世界有" + num + "/" + max + "个玩家在线：\n" + player + "。";
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
        if (msg.contentToString().matches(".*?(来图|不够([射蛇色涩瑟铯\uD83D\uDC0D])).*?")) {
            String path = Va.globalConfig.getHentai_path().get();
            if (!StringUtils.isNullOrEmpty(path)) {
                List<Path> paths;
                if (!Va.dataCache.containsKey(path)) {
                    getHentaiList(path);
                }
                paths = (List<Path>) Va.dataCache.get(path);
                long index = VanillaUtils.getDataCacheAsLong(path + "!index");
                VanillaUtils.setDateCache(path + "!index", ++index);
                if (paths.size() <= index) {
                    getHentaiList(path);
                } else {
                    group.sendMessage(new MessageChainBuilder()
                            .append(ExternalResource.uploadAsImage(paths.get((int) index).toFile(), group))
                            .build());
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
            Va.dataCache.put(path, files);
        } catch (IOException e) {
            e.printStackTrace();
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
            no = source.getIds()[0] - no;
            String msgCache = Va.messageCache.getMsgMiraiCode(String.valueOf(no), group.getId(), MSG_TYPE_GROUP);
            Api.sendMessage(group, msgCache);
        }

        // if (msg.contentToString().equals("/va get string")) {
        //     Api.sendMessage(group, "testString is: " + Va.globalConfig.getMc_rcon_ip());
        // }
        // if (msg.contentToString().startsWith("/va set string ")) {
        //     String s = msg.contentToString().substring("/va set string ".length());
        //     Va.globalConfig.setMc_rcon_ip(s);
        //     Api.sendMessage(group, "testString now is: " + Va.globalConfig.getMc_rcon_ip());
        // }

        // if (msg.contentToString().equals("/va get int")) {
        //     Api.sendMessage(group, "testInt is: " + Va.globalConfig.getMc_rcon_port());
        // }
        // if (msg.contentToString().startsWith("/va set int ")) {
        //     int s = Integer.parseInt(msg.contentToString().substring("/va set int ".length()));
        //     Va.globalConfig.setMc_rcon_port(s);
        //     Api.sendMessage(group, "testInt now is: " + Va.globalConfig.getMc_rcon_port());
        // }

        if (msg.contentToString().equals("/va get owner")) {
            Api.sendMessage(group, "botOwner is: " + Va.globalConfig.getPermissions().get(bot.getId()).getBotOwner());
        }
        // if (msg.contentToString().startsWith("/va set owner ")) {
        //     String s = msg.contentToString().substring("/va set owner ".length());
        //     Va.globalConfig.getPermissions().get(bot.getId()).setBotOwner(Long.parseLong(s));
        //     Api.sendMessage(group, "botOwner now is: " + Va.globalConfig.getPermissions().get(bot.getId()).getBotOwner());
        // }

        if (msg.contentToString().equals("/va get superAdmin")) {
            Api.sendMessage(group, "superAdmin is: " + Va.globalConfig.getPermissions().get(bot.getId()).getSuperAdmin());
        }
        // if (msg.contentToString().startsWith("/va set superAdmin ")) {
        //     String s = msg.contentToString().substring("/va set superAdmin ".length());
        //     Va.globalConfig.getPermissions().get(bot.getId()).setSuperAdmin(new HashSet<Long>() {{
        //         addAll(Arrays.stream(s.split(" ")).map(Long::parseLong).collect(Collectors.toList()));
        //     }});
        //     Api.sendMessage(group, "superAdmin now is: " + Va.globalConfig.getPermissions().get(bot.getId()).getSuperAdmin());
        // }

        // Va.config.refreshSource();

        if (msg.contentToString().equals("/va get secondaryPrefix")) {
            Api.sendMessage(group, Va.globalConfig.getInstructions().getSecondaryPrefix().toString());
        }
    }
}
