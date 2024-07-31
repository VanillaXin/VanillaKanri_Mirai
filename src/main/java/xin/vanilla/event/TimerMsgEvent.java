package xin.vanilla.event;

import lombok.Setter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.UserOrBot;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import xin.vanilla.VanillaKanri;
import xin.vanilla.common.annotation.Capability;
import xin.vanilla.entity.DecodeKeyParam;
import xin.vanilla.entity.KeyRepEntity;
import xin.vanilla.entity.data.KeyData;
import xin.vanilla.entity.data.TimerData;
import xin.vanilla.util.Frame;
import xin.vanilla.util.VanillaUtils;

public class TimerMsgEvent extends BaseMsgEvent implements Job {
    private final VanillaKanri Va = VanillaKanri.INSTANCE;

    @Setter
    private TimerData timer;

    private Group group;
    private Contact sender;
    private Contact target;
    private Bot bot;
    private String repString;
    private String lastResult;

    public TimerMsgEvent() {
        super(null, null, System.currentTimeMillis());
    }

    @Override
    public void execute(JobExecutionContext context) {
        // TimerData timer = (TimerData) context.getJobDetail().getJobDataMap().get("timer");
        assert timer.getBot() != null;
        this.bot = timer.getBot();
        this.group = this.bot.getGroup(timer.getGroupNum());
        this.sender = timer.getSender();
        this.target = this.group != null ? this.group : this.sender;
        this.repString = timer.getMsg();
        this.lastResult = timer.getLastResult();
        this.msg = new MessageChainBuilder().append(timer.getCron()).build();
        this.run();
    }

    /**
     * 解析消息特殊码并发送
     */
    @Capability
    private boolean timer() {
        KeyData repWord = new KeyData();
        repWord.setRep(this.repString);
        repWord.setBot(this.bot.getId());
        if (this.group != null) {
            repWord.setGroup(this.group.getId());
        }
        repWord.setTime(this.time);
        String result = VanillaUtils.deVanillaCodeIns(new DecodeKeyParam(bot, sender, group, (int) (this.time / 1000), msg, repWord));
        // 判断是否包含 仅变化才发送 特殊码
        if (result.contains("[vacode:timer:changed]")) {
            result = result.replace("[vacode:timer:changed]", "");
            if (!result.equals(this.lastResult)) {
                timer.setLastResult(result);
            } else {
                result = "";
            }
        }
        MessageChain messages = MessageChain.deserializeFromMiraiCode(result, target);
        KeyRepEntity keyRepEntity = new KeyRepEntity(target);
        keyRepEntity.setMsg(VanillaUtils.messageToString(msg));
        keyRepEntity.setSenderId(sender.getId());
        if (sender instanceof Group) {
            keyRepEntity.setSenderName(((Group) sender).getName());
        } else if (sender instanceof UserOrBot) {
            keyRepEntity.setSenderName(((UserOrBot) sender).getNick());
        }
        return null != Frame.sendMessage(keyRepEntity, messages);
    }
}
