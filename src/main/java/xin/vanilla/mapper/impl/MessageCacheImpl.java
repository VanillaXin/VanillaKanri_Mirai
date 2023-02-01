package xin.vanilla.mapper.impl;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageSource;
import xin.vanilla.mapper.MessageCache;
import xin.vanilla.util.DateUtils;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.sqlite.SqliteUtil;
import xin.vanilla.util.sqlite.statement.InsertStatement;

import java.sql.SQLException;
import java.util.Date;

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

    private String getTableName(long time) {
        Date date = new Date(time * 1000);
        return DateUtils.getYearOfDate(date) + "." + DateUtils.getMonthOfDateWithZero(date);
    }

    @Override
    public void createTable(String table) {
        if (!sqliteUtil.containsTable(table)) {
            sqliteUtil.executeSql(
                    "CREATE TABLE '" + table + "' (" +
                            " id     INTEGER     PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            " nos    TEXT        UNIQUE                    NOT NULL," +
                            " bot    INTEGER(10)                           NOT NULL," +
                            " sender INTEGER(10)                           NOT NULL," +
                            " target INTEGER(10)                           NOT NULL," +
                            " time   INTEGER(10)                           NOT NULL," +
                            " msg    TEXT                                  NOT NULL" +
                            ")");
        }
    }

    @Override
    public void addMsg(Group group, MessageChain msg) {
        MessageSource source = msg.get(MessageSource.Key);
        if (null == source) return;
        int time = source.getTime();
        String table = "group_" + getTableName(time);
        addMsg(msg, source, time, table, group.getId());
    }

    @Override
    public void addMsg(Friend friend, MessageChain msg) {
        MessageSource source = msg.get(MessageSource.Key);
        if (null == source) return;
        int time = source.getTime();
        String table = "friend_" + getTableName(time);
        addMsg(msg, source, time, table, friend.getId());
    }

    @Override
    public void addMsg(Member member, MessageChain msg) {
        MessageSource source = msg.get(MessageSource.Key);
        if (null == source) return;
        int time = source.getTime();
        String table = "member_" + getTableName(time);
        addMsg(msg, source, time, table, member.getId());
    }

    @Override
    public void addMsg(Stranger stranger, MessageChain msg) {
        MessageSource source = msg.get(MessageSource.Key);
        if (null == source) return;
        int time = source.getTime();
        String table = "stranger_" + getTableName(time);
        addMsg(msg, source, time, table, stranger.getId());
    }

    private void addMsg(MessageChain msg, MessageSource source, int time, String table, long target) {
        long sender = source.getFromId();
        int[] ids = source.getIds();
        int[] internalIds = source.getInternalIds();
        long botId = source.getBotId();

        createTable(table);
        String nos = StringUtils.toString(ids) + "|" + StringUtils.toString(internalIds);
        InsertStatement insert = InsertStatement.produce(getTableName(time))
                .put("nos", nos)
                .put("bot", botId)
                .put("sender", sender)
                .put("target", target)
                .put("time", time)
                .put("msg", MiraiCode.serializeToMiraiCode(msg.stream().iterator()));

        sqliteUtil.insert(insert);
    }

}
