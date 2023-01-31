package xin.vanilla.mapper.impl;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import xin.vanilla.mapper.MessageCache;
import xin.vanilla.util.sqlite.SqliteUtil;

import java.sql.SQLException;

public class MessageCacheImpl implements MessageCache {
    public static String dbname = "\\msg_cache.db";
    private final SqliteUtil sqliteUtil;

    public MessageCacheImpl(String path) {
        try {
            this.sqliteUtil = SqliteUtil.getInstance(path + dbname);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createTable(String date) {

    }

    @Override
    public void addMsg(Group group, MessageChain msg) {
        MessageSource source = msg.get(MessageSource.Key);
        if (null == source) return;
        int time = source.getTime();
        long botId = source.getBotId();
        long fromId = source.getFromId();
        int[] ids = source.getIds();
        int[] internalIds = source.getInternalIds();


    }

    @Override
    public void addMsg(Friend friend, MessageChain msg) {

    }
}
