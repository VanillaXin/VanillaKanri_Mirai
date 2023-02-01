package xin.vanilla.mapper;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.message.data.MessageChain;

public interface MessageCache {

    /**
     * 根据日期创建表, 若有则不重新创建
     */
    void createTable(String table);

    void addMsg(Group group, MessageChain msg);

    void addMsg(Friend friend, MessageChain msg);

    void addMsg(Member member, MessageChain msg);

    void addMsg(Stranger stranger, MessageChain msg);
}
