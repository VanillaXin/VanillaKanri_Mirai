package xin.vanilla.mapper.impl;

import cn.hutool.core.date.DateUtil;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.entity.data.KeyData;
import xin.vanilla.mapper.Base;
import xin.vanilla.mapper.KeywordData;
import xin.vanilla.util.sqlite.SqliteUtil;
import xin.vanilla.util.sqlite.statement.InsertStatement;

import java.sql.SQLException;

public class KeywordDataImpl extends Base implements KeywordData {
    public static String dbname = "\\keyword.db";

    public static String KEYWORD_TYPE_EXACTLY = "exactly";
    public static String KEYWORD_TYPE_CONTAIN = "contain";
    public static String KEYWORD_TYPE_PINYIN = "pinyin";
    public static String KEYWORD_TYPE_REGEXP = "regexp";

    public static String[] KEYWORD_TYPES = {KEYWORD_TYPE_EXACTLY, KEYWORD_TYPE_CONTAIN, KEYWORD_TYPE_PINYIN, KEYWORD_TYPE_REGEXP};

    private final SqliteUtil sqliteUtil;

    public KeywordDataImpl(String path) {
        try {
            this.sqliteUtil = SqliteUtil.getInstance(path + dbname);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createTable(String table) {
        if (!sqliteUtil.containsTable(table)) {
            sqliteUtil.executeSql(
                    "CREATE TABLE '" + table + "' (" +
                            " id     INTEGER     PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            " word   TEXT                                  NOT NULL," +
                            " msg    TEXT                                  NOT NULL," +
                            " bot    INTEGER(10)                           NOT NULL," +
                            " group  INTEGER(10)                           NOT NULL," +
                            " time   INTEGER(10)                           NOT NULL," +
                            " level  INTEGER(2)                            NOT NULL" +
                            ")");
            sqliteUtil.executeSql("CREATE UNIQUE INDEX 'word_msg_group_unique'" + " ON '" + table + "' ('word', 'group', 'msg')");
        }
    }

    @Override
    public void addKeyword(String word, String rep, long bot, long group, String type, long time, int level) {
        KeywordInstructions keyword = VanillaKanri.INSTANCE.getGlobalConfig().getInstructions().getKeyword();
        String table;
        if (keyword.getExactly().contains(type)) {
            table = getTableName(KEYWORD_TYPE_EXACTLY);
        } else if (keyword.getContain().contains(type)) {
            table = getTableName(KEYWORD_TYPE_CONTAIN);
        } else if (keyword.getPinyin().contains(type)) {
            table = getTableName(KEYWORD_TYPE_PINYIN);
        } else if (keyword.getRegex().contains(type)) {
            table = getTableName(KEYWORD_TYPE_REGEXP);
        } else return;
        createTable(table);
        // TODO 定义特殊码, 转义特殊码
        InsertStatement insert = InsertStatement.produce(table)
                .put(KeyData::getWord, word)
                .put(KeyData::getMsg, rep)
                .put(KeyData::getBot, bot)
                .put(KeyData::getGroup, group)
                .put(KeyData::getTime, time)
                .put(KeyData::getLevel, level);
        sqliteUtil.insert(insert);
    }

    @Override
    public void addKeyword(String word, String rep, long bot, long group, String type, long time) {
        addKeyword(word, rep, bot, group, type, time, 1);
    }

    @Override
    public void addKeyword(String word, String rep, long bot, long group, String type, int level) {
        addKeyword(word, rep, bot, group, type, DateUtil.currentSeconds(), level);
    }

    @Override
    public void addKeyword(String word, String rep, long bot, long group, String type) {
        addKeyword(word, rep, bot, group, type, DateUtil.currentSeconds(), 1);
    }
}
