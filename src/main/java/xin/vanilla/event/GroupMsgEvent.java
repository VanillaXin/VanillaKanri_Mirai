package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import nl.vv32.rcon.Rcon;

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
        if (msg.contentToString().startsWith("/va get string")) {
            group.sendMessage("testString is: " + Va.globalConfig.mc_rcon_ip.get());
        }
        if (msg.contentToString().startsWith("/va set string ")) {
            String s = msg.contentToString().substring("/va set string ".length());
            Va.globalConfig.mc_rcon_ip.set(s);
            group.sendMessage("testString now is: " + Va.globalConfig.mc_rcon_ip.get());
        }

        if (msg.contentToString().startsWith("/va get int")) {
            group.sendMessage("testInt is: " + Va.globalConfig.mc_rcon_port.get());
        }
        if (msg.contentToString().startsWith("/va set int ")) {
            int s = Integer.parseInt(msg.contentToString().substring("/va set int ".length()));
            Va.globalConfig.mc_rcon_port.set(s);
            group.sendMessage("testInt now is: " + Va.globalConfig.mc_rcon_port.get());
        }

        if (msg.contentToString().startsWith("/va get owner")) {
            group.sendMessage("botOwner is: " + Va.globalConfig.PERMISSIONS.get().getSuperOwner());
        }
        if (msg.contentToString().startsWith("/va set owner ")) {
            String s = msg.contentToString().substring("/va set owner ".length());
            Va.globalConfig.PERMISSIONS.get().setSuperOwner(Long.valueOf(s));
            group.sendMessage("botOwner now is: " + s);
        }

        if (msg.contentToString().startsWith("/va get superAdmin")) {
            group.sendMessage("superAdmin is: " + Va.globalConfig.PERMISSIONS.get().getSuperAdmin());
        }
        if (msg.contentToString().startsWith("/va set superAdmin ")) {
            String s = msg.contentToString().substring("/va set superAdmin ".length());
            Va.globalConfig.PERMISSIONS.get().setSuperAdmin(new HashSet<Long>() {{
                addAll(Arrays.stream(s.split(" ")).map(Long::parseLong).collect(Collectors.toList()));
            }});
            group.sendMessage("superAdmin now is: " + s);
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

        try (Rcon rcon = Rcon.open(Va.globalConfig.mc_rcon_ip.get(), Va.globalConfig.mc_rcon_port.get())) {
            if (rcon.authenticate(Va.globalConfig.mc_rcon_psw.get())) {
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
