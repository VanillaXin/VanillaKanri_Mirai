package xin.vanilla.mapper;

import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.OnlineMessageSource;

public interface MessageCache {

    /**
     * 根据日期创建表, 若有则不重新创建
     */
    void createTable(String table);

    void addMsg(Group group, MessageChain msg);

    void addMsg(Friend friend, MessageChain msg);

    void addMsg(Member member, MessageChain msg);

    void addMsg(Stranger stranger, MessageChain msg);

    void addMsg(Contact contact, OnlineMessageSource.Outgoing outgoing, Message message);

    String getMsgString(String no, long sender, long target, long time, String type);

    String getMsgString(String no, long sender, long target, String type);

    String getMsgString(String no, long sender, long target, long time);

    String getMsgString(String no, long sender, long target);

    String getMsgString(String no, long target, String type);

    String getMsgString(String no, long target);

    MessageChain getMsgChain(String no, long sender, long target, long time, String type);

    MessageChain getMsgChain(String no, long sender, long target, String type);

    MessageChain getMsgChain(String no, long sender, long target, long time);

    MessageChain getMsgChain(String no, long sender, long target);

    MessageChain getMsgChain(String no, long target, String type);

    MessageChain getMsgChain(String no, long target);

    String getMsgMiraiCode(String no, long sender, long target, long time, String type);

    String getMsgMiraiCode(String no, long sender, long target, String type);

    String getMsgMiraiCode(String no, long sender, long target, long time);

    String getMsgMiraiCode(String no, long sender, long target);

    String getMsgMiraiCode(String no, long target, String type);

    String getMsgMiraiCode(String no, long target);
}
