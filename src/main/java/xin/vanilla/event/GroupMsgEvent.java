package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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

        if (msg.contentToString().startsWith("/va get string")) {
            group.sendMessage("testString is: " + Va.config.source.testString.get());
        }
        if (msg.contentToString().startsWith("/va set string ")) {
            String s = msg.contentToString().substring("/va set string ".length());
            Va.config.source.testString.set(s);
            group.sendMessage("testString now is: " + Va.config.source.testString.get());
        }

        if (msg.contentToString().startsWith("/va get int")) {
            group.sendMessage("testInt is: " + Va.config.source.testInt.get());
        }
        if (msg.contentToString().startsWith("/va set int ")) {
            int s = Integer.parseInt(msg.contentToString().substring("/va set int ".length()));
            Va.config.source.testInt.set(s);
            group.sendMessage("testInt now is: " + Va.config.source.testInt.get());
        }

        if (msg.contentToString().startsWith("/va get owner")) {
            group.sendMessage("botOwner is: " + Va.config.PERMISSIONS.superOwner);
        }
        if (msg.contentToString().startsWith("/va set owner ")) {
            String s = msg.contentToString().substring("/va set owner ".length());
            Va.config.source.permission.get().put("botOwner", new HashSet<String>() {{
                add(s);
            }});
            group.sendMessage("botOwner now is: " + s);
        }

        if (msg.contentToString().startsWith("/va get superAdmin")) {
            group.sendMessage("superAdmin is: " + Va.config.PERMISSIONS.superAdmin);
        }
        if (msg.contentToString().startsWith("/va set superAdmin ")) {
            String s = msg.contentToString().substring("/va set superAdmin ".length());
            Va.config.source.permission.get().put("superAdmin", new HashSet<String>() {{
                addAll(Arrays.stream(s.split(" ")).collect(Collectors.toList()));
            }});
            group.sendMessage("superAdmin now is: " + s);
        }

    }
}
