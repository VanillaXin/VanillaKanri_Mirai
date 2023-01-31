package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;
import xin.vanilla.rcon.Rcon;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

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
    }

    public void run() {
        if (isBlock) return;
        logger.info("群聊: " + group.getId() + ":" + sender.getId() + " -> " + msg.contentToString());
        if (rcon()) return;

        test();
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
            MessageSource source = msg.get(MessageSource.Key);
            logger.info(Arrays.toString(source.getIds()));
            logger.info(Arrays.toString(source.getInternalIds()));

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

        if (msg.contentToString().startsWith("/va get string")) {
            group.sendMessage("testString is: " + Va.globalConfig.getMc_rcon_ip());
        }
        if (msg.contentToString().startsWith("/va set string ")) {
            String s = msg.contentToString().substring("/va set string ".length());
            Va.globalConfig.setMc_rcon_ip(s);
            group.sendMessage("testString now is: " + Va.globalConfig.getMc_rcon_ip());
        }

        if (msg.contentToString().startsWith("/va get int")) {
            group.sendMessage("testInt is: " + Va.globalConfig.getMc_rcon_port());
        }
        if (msg.contentToString().startsWith("/va set int ")) {
            int s = Integer.parseInt(msg.contentToString().substring("/va set int ".length()));
            Va.globalConfig.setMc_rcon_port(s);
            group.sendMessage("testInt now is: " + Va.globalConfig.getMc_rcon_port());
        }

        if (msg.contentToString().startsWith("/va get owner")) {
            group.sendMessage("botOwner is: " + Va.globalConfig.getPermissions().get(bot.getId()).getBotOwner());
        }
        if (msg.contentToString().startsWith("/va set owner ")) {
            String s = msg.contentToString().substring("/va set owner ".length());
            Va.globalConfig.getPermissions().get(bot.getId()).setBotOwner(Long.parseLong(s));
            group.sendMessage("botOwner now is: " + Va.globalConfig.getPermissions().get(bot.getId()).getBotOwner());
        }

        if (msg.contentToString().startsWith("/va get superAdmin")) {
            group.sendMessage("superAdmin is: " + Va.globalConfig.getPermissions().get(bot.getId()).getSuperAdmin());
        }
        if (msg.contentToString().startsWith("/va set superAdmin ")) {
            String s = msg.contentToString().substring("/va set superAdmin ".length());
            Va.globalConfig.getPermissions().get(bot.getId()).setSuperAdmin(new HashSet<Long>() {{
                addAll(Arrays.stream(s.split(" ")).map(Long::parseLong).collect(Collectors.toList()));
            }});
            group.sendMessage("superAdmin now is: " + Va.globalConfig.getPermissions().get(bot.getId()).getSuperAdmin());
        }

        // Va.config.refreshSource();
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
            if (!(sender.getId() == 196468986L || sender.getId() == 3085477411L)) return false;
            command = msg.contentToString().substring(prefix.length());
        } else if (msg.contentToString().equals("/list") || msg.contentToString().equals("/ls")) command = "list";
        else return false;

        try (Rcon rcon = Rcon.open(Va.globalConfig.getMc_rcon_ip(), Va.globalConfig.getMc_rcon_port())) {
            if (rcon.authenticate(Va.globalConfig.getMc_rcon_psw())) {
                group.sendMessage(rcon.sendCommand(command));
            } else {
                group.sendMessage("Failed to authenticate");
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
