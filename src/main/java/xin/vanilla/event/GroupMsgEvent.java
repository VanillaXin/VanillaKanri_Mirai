package xin.vanilla.event;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;

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
        if (msg.contentToString().startsWith("/va botOwner")) {
            group.sendMessage("香草酱的主人是: " + Va.config.PERMISSIONS.botOwner.get() + " 喵!");
        }
        if (msg.contentToString().startsWith("/va set botOwner ")) {
            long botOwner = Long.parseLong(msg.contentToString().substring("/va set botOwner ".length()));
            Va.config.PERMISSIONS.botOwner.set(botOwner);
            Va.reloadPluginConfig(Va.config.PERMISSIONS);
            group.sendMessage("香草酱的主人现在是: " + Va.config.PERMISSIONS.botOwner.get() + " 喵!");
        }

    }
}
