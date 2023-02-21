package xin.vanilla.mapper.impl;

import net.mamoe.mirai.contact.*;
import net.mamoe.mirai.message.code.MiraiCode;
import net.mamoe.mirai.message.data.*;
import xin.vanilla.entity.data.MsgCache;
import xin.vanilla.mapper.MessageCache;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.sqlite.SqliteUtil;
import xin.vanilla.util.sqlite.statement.InsertStatement;
import xin.vanilla.util.sqlite.statement.QueryStatement;
import xin.vanilla.util.sqlite.statement.Statement;

import java.sql.SQLException;
import java.util.Arrays;

public class MessageCacheImpl implements MessageCache {
    public static String MSG_TYPE_GROUP = "group_";
    public static String MSG_TYPE_FRIEND = "friend_";
    public static String MSG_TYPE_MEMBER = "member_";
    public static String MSG_TYPE_STRANGER = "stranger_";
    public static String dbname = "\\msg_cache.db";
    public static String dbVersion = "1.0.0";

    private static String[] MSG_TYPES = {MSG_TYPE_GROUP, MSG_TYPE_FRIEND, MSG_TYPE_MEMBER, MSG_TYPE_STRANGER};
    private final SqliteUtil sqliteUtil;

    public MessageCacheImpl(String path) {
        try {
            this.sqliteUtil = SqliteUtil.getInstance(path + dbname);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getTableName() {
        // Date date = new Date(time * 1000);
        // return DateUtils.getYearOfDate(date) + "." + DateUtils.getMonthOfDateWithZero(date);
        return dbVersion;
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
        String table = MSG_TYPE_GROUP + getTableName();
        addMsg(msg, source, time, table, group.getId());
    }

    @Override
    public void addMsg(Friend friend, MessageChain msg) {
        MessageSource source = msg.get(MessageSource.Key);
        if (null == source) return;
        int time = source.getTime();
        String table = MSG_TYPE_FRIEND + getTableName();
        addMsg(msg, source, time, table, friend.getId());
    }

    @Override
    public void addMsg(Member member, MessageChain msg) {
        MessageSource source = msg.get(MessageSource.Key);
        if (null == source) return;
        int time = source.getTime();
        String table = MSG_TYPE_MEMBER + getTableName();
        addMsg(msg, source, time, table, member.getId());
    }

    @Override
    public void addMsg(Stranger stranger, MessageChain msg) {
        MessageSource source = msg.get(MessageSource.Key);
        if (null == source) return;
        int time = source.getTime();
        String table = MSG_TYPE_STRANGER + getTableName();
        addMsg(msg, source, time, table, stranger.getId());
    }

    @Override
    public void addMsg(Contact contact, OnlineMessageSource.Outgoing outgoing, Message msg) {
        String table;
        if (contact instanceof Group)
            table = MSG_TYPE_GROUP + getTableName();
        else if (contact instanceof Friend)
            table = MSG_TYPE_FRIEND + getTableName();
        else if (contact instanceof Member)
            table = MSG_TYPE_MEMBER + getTableName();
        else if (contact instanceof Stranger)
            table = MSG_TYPE_STRANGER + getTableName();
        else return;

        long targetId = outgoing.getTargetId();
        long sender = outgoing.getFromId();
        long botId = outgoing.getBot().getId();
        int[] ids = outgoing.getIds();
        int[] internalIds = outgoing.getInternalIds();
        int time = outgoing.getTime();
        addMsg(MessageUtils.newChain(msg), time, table, targetId, sender, ids, internalIds, botId);
    }

    private void addMsg(MessageChain msg, int time, String table, long target, long sender, int[] ids, int[] internalIds, long botId) {
        createTable(table);
        String nos = StringUtils.toString(ids) + "|" + StringUtils.toString(internalIds);
        String msgString = msg.serializeToMiraiCode();

        if (msgString.equals("")) {
            MarketFace marketFace = msg.get(MarketFace.Key);
            ForwardMessage forwardMessage = msg.get(ForwardMessage.Key);
            Audio audio = msg.get(Audio.Key);

            if (marketFace != null) {
                msgString = marketFace.toString();
            } else if (forwardMessage != null) {
                msgString = forwardMessage.toString();
            } else if (audio != null) {
                msgString = audio.toString();
            }
        }

        InsertStatement insert = InsertStatement.produce(table)
                .put(MsgCache::getNos, nos)
                .put(MsgCache::getBot, botId)
                .put(MsgCache::getSender, sender)
                .put(MsgCache::getTarget, target)
                .put(MsgCache::getTime, time)
                .put(MsgCache::getMsg, msgString);

        sqliteUtil.insert(insert);
    }

    private void addMsg(MessageChain msg, MessageSource source, int time, String table, long target) {
        long sender = source.getFromId();
        int[] ids = source.getIds();
        int[] internalIds = source.getInternalIds();
        long botId = source.getBotId();
        addMsg(msg, time, table, target, sender, ids, internalIds, botId);
    }

    @Override
    public String getMsgString(String no, long sender, long target, long time, String type) {
        return getMsgChain(no, sender, target, time, type).contentToString();
    }

    @Override
    public String getMsgString(String no, long sender, long target, String type) {
        return getMsgString(no, sender, target, 0, type);
    }

    @Override
    public String getMsgString(String no, long sender, long target, long time) {
        return getMsgString(no, sender, target, time, null);
    }

    @Override
    public String getMsgString(String no, long sender, long target) {
        return getMsgString(no, sender, target, 0);
    }

    @Override
    public String getMsgString(String no, long target, String type) {
        return getMsgString(no, 0, target, 0, type);
    }

    @Override
    public String getMsgString(String no, long target) {
        return getMsgString(no, 0, target);
    }

    @Override
    public MessageChain getMsgChain(String no, long sender, long target, long time, String type) {
        return MiraiCode.deserializeMiraiCode(getMsgMiraiCode(no, sender, target, time, type));
    }

    @Override
    public MessageChain getMsgChain(String no, long sender, long target, String type) {
        return getMsgChain(no, sender, target, 0, type);
    }

    @Override
    public MessageChain getMsgChain(String no, long sender, long target, long time) {
        return getMsgChain(no, sender, target, time, null);
    }

    @Override
    public MessageChain getMsgChain(String no, long sender, long target) {
        return getMsgChain(no, sender, target, 0);
    }

    @Override
    public MessageChain getMsgChain(String no, long target, String type) {
        return getMsgChain(no, 0, target, 0, type);
    }

    @Override
    public MessageChain getMsgChain(String no, long target) {
        return getMsgChain(no, 0, target);
    }

    @Override
    public String getMsgMiraiCode(String no, long sender, long target, long time, String type) {
        MsgCache msgCache = getMsgCache(no, sender, target, time, type);
        if (msgCache == null) return null;
        return msgCache.getMsg();
    }

    @Override
    public String getMsgMiraiCode(String no, long sender, long target, String type) {
        return getMsgMiraiCode(no, sender, target, 0, type);
    }

    @Override
    public String getMsgMiraiCode(String no, long sender, long target, long time) {
        return getMsgMiraiCode(no, sender, target, time, null);
    }

    @Override
    public String getMsgMiraiCode(String no, long sender, long target) {
        return getMsgMiraiCode(no, sender, target, 0);
    }

    @Override
    public String getMsgMiraiCode(String no, long target, String type) {
        return getMsgMiraiCode(no, 0, target, 0, type);
    }

    @Override
    public String getMsgMiraiCode(String no, long target) {
        return getMsgMiraiCode(no, 0, target);
    }

    @Override
    public int[] getInternalIds(int[] ids, long target, String type) {
        String nos = getMsgCache(StringUtils.toString(ids), target, type).getNos();
        String internalIds = nos.substring(nos.indexOf("|") + 1);
        if (!internalIds.contains(",")) return new int[]{Integer.parseInt(internalIds)};
        return Arrays.stream(internalIds.split(",")).mapToInt(Integer::parseInt).toArray();
    }

    @Override
    public int[] getInternalIds(int id, long target, String type) {
        return getInternalIds(new int[]{id}, target, type);
    }

    @Override
    public MsgCache getMsgCache(String no, long sender, long target, long time, String type) {
        MsgCache msgCache = null;
        if (!StringUtils.isNullOrEmpty(type))
            MSG_TYPES = new String[]{type};

        for (String msgType : MSG_TYPES) {
            Statement query = QueryStatement.produce()
                    .from(msgType + getTableName())
                    .where(MsgCache::getTarget).eq(target);

            if (sender > 0) query.and(MsgCache::getTarget).eq(target);
            if (time > 0) query.and(MsgCache::getTime).eq(time);
            if (!no.contains("|"))
                query.and(MsgCache::getNos).like("%" + no + "%|%");
            else if (no.startsWith("|"))
                query.and(MsgCache::getNos).likeEndsWith(no);
            else
                query.and(MsgCache::getNos).likeStartsWith(no);
            msgCache = sqliteUtil.getEntity(query, MsgCache.class);
            if (msgCache != null && msgCache.getId() > 0) break;
        }
        return msgCache;
    }

    @Override
    public MsgCache getMsgCache(String no, long sender, long target, String type) {
        return getMsgCache(no, sender, target, 0, type);
    }

    @Override
    public MsgCache getMsgCache(String no, long sender, long target, long time) {
        return getMsgCache(no, sender, target, time, null);
    }

    @Override
    public MsgCache getMsgCache(String no, long sender, long target) {
        return getMsgCache(no, sender, target, 0);
    }

    @Override
    public MsgCache getMsgCache(String no, long target, String type) {
        return getMsgCache(no, 0, target, 0, type);
    }

    @Override
    public MsgCache getMsgCache(String no, long target) {
        return getMsgCache(no, 0, target);
    }

}
