package xin.vanilla.mapper.impl;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.data.MessageChain;
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

    }

    @Override
    public void addMsg(Friend friend, MessageChain msg) {

    }
}
