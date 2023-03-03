package xin.vanilla.mapper.impl;

import cn.hutool.core.date.DateUtil;
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xin.vanilla.VanillaKanri;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.entity.data.KeyData;
import xin.vanilla.mapper.KeywordData;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.sqlite.PaginationList;
import xin.vanilla.util.sqlite.SqliteUtil;
import xin.vanilla.util.sqlite.statement.InsertStatement;
import xin.vanilla.util.sqlite.statement.QueryStatement;
import xin.vanilla.util.sqlite.statement.Statement;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

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
                    "CREATE TABLE `" + table + "` (" +
                            " `id`     INTEGER     PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            " `word`   TEXT                                  NOT NULL," +
                            " `msg`    TEXT                                  NOT NULL," +
                            " `bot`    INTEGER(10)                           NOT NULL," +
                            " `group`  INTEGER(10)                           NOT NULL," +
                            " `time`   INTEGER(10)                           NOT NULL," +
                            " `level`  INTEGER(2)                            NOT NULL" +
                            ")");
            sqliteUtil.executeSql("CREATE UNIQUE INDEX 'word_msg_group_unique'" + " ON `" + table + "` ('word', 'group', 'msg')");
        }
    }

    @Override
    public long addKeyword(String word, String rep, long bot, long group, String type, long time, int level) {
        String table = getTable(type);
        createTable(table);
        // TODO 定义特殊码, 转义特殊码
        InsertStatement insert = InsertStatement.produce(table)
                .put(KeyData::getWord, word)
                .put(KeyData::getMsg, rep)
                .put(KeyData::getBot, bot)
                .put(KeyData::getGroup, group)
                .put(KeyData::getTime, time)
                .put(KeyData::getLevel, level);
        if (sqliteUtil.insert(insert) > 0) {
            return sqliteUtil.getLastInsertRowId(table);
        }
        return 0;
    }

    @Override
    public long addKeyword(String word, String rep, long bot, long group, String type, long time) {
        return addKeyword(word, rep, bot, group, type, time, 1);
    }

    @Override
    public long addKeyword(String word, String rep, long bot, long group, String type, int level) {
        return addKeyword(word, rep, bot, group, type, DateUtil.currentSeconds(), level);
    }

    @Override
    public long addKeyword(String word, String rep, long bot, long group, String type) {
        return addKeyword(word, rep, bot, group, type, DateUtil.currentSeconds(), 1);
    }

    @Override
    public KeyData getKeywordById(long id, String type) {
        String table = getTable(type);
        Statement query = QueryStatement.produce().from(table).where(KeyData::getId).eq(id);
        KeyData entity = sqliteUtil.getEntity(query, KeyData.class);
        if (entity == null) return new KeyData();
        return entity;
    }

    @Override
    public KeyData getKeyword(String word, long bot, long group, String type) {
        return selectRandom(getKeywordList(word, bot, group, type));
    }

    @Override
    public KeyData getKeyword(String word, long bot, long group) {
        return getKeyword(word, bot, group, null);
    }

    @Override
    public KeyData getKeyword(String word, long bot, String type) {
        return getKeyword(word, bot, 0, type);
    }

    @Override
    public KeyData getKeyword(String word, long bot) {
        return getKeyword(word, bot, 0, null);
    }

    @Override
    public List<KeyData> getKeywordList(String word, long bot, long group, String type) {
        String[] types;
        if (StringUtils.isNullOrEmpty(type)) types = KEYWORD_TYPES;
        else types = new String[]{type};

        List<KeyData> keys = new ArrayList<>();
        for (String typeString : types) {
            String table = getTable(typeString);
            createTable(table);
            Statement query = QueryStatement.produce().from(table)
                    .where(KeyData::getBot).eq(bot);
            if (group != 0)
                query.and(KeyData::getGroup).eq(group);

            assert table != null;
            // 完全匹配
            if (table.startsWith(KEYWORD_TYPE_EXACTLY)) {
                query.and(KeyData::getWord).eq(word);
            }
            // 包含匹配
            else if (table.startsWith(KEYWORD_TYPE_CONTAIN)) {
                query.andLikeContains(KeyData::getWord, word);
            }
            // 拼音包含匹配
            else if (table.startsWith(KEYWORD_TYPE_PINYIN)) {
                query.andLikeContains(KeyData::getWord, PinyinHelper.toPinyin(word, PinyinStyleEnum.NORMAL).trim());
            }
            // 正则匹配
            else if (table.startsWith(KEYWORD_TYPE_REGEXP)) {
                // TODO 正则匹配
                // query.andRegexp(KeyData::getWord, word);
                continue;
            }
            List<KeyData> list = sqliteUtil.getList(query, KeyData.class);
            if (list != null) {
                list.forEach(k -> k.setType(table));
                keys.addAll(list);
            }
        }
        return keys;
    }

    @Override
    public List<KeyData> getKeywordList(String word, long bot, long group) {
        return getKeywordList(word, bot, group, null);
    }

    @Override
    public List<KeyData> getKeywordList(String word, long bot, String type) {
        return getKeywordList(word, bot, 0, type);
    }

    @Override
    public List<KeyData> getKeywordList(String word, long bot) {
        return getKeywordList(word, bot, 0, null);
    }

    @Override
    public PaginationList<KeyData> getKeywordByPage(String word, long bot, long group, String type, int page, int size) {
        String[] types;
        if (StringUtils.isNullOrEmpty(type)) types = KEYWORD_TYPES;
        else types = new String[]{type};

        for (String typeString : types) {
            String table = getTable(typeString);
            createTable(table);
            Statement query = QueryStatement.produce().from(table)
                    .where(KeyData::getBot).eq(bot);
            if (group != 0)
                query.and(KeyData::getGroup).eq(group);

            assert table != null;
            // 完全匹配
            if (table.startsWith(KEYWORD_TYPE_EXACTLY)) {
                query.and(KeyData::getWord).eq(word);
            }
            // 包含匹配
            else if (table.startsWith(KEYWORD_TYPE_CONTAIN)) {
                query.andLikeContains(KeyData::getWord, word);
            }
            // 拼音包含匹配
            else if (table.startsWith(KEYWORD_TYPE_PINYIN)) {
                query.andLikeContains(KeyData::getWord, PinyinHelper.toPinyin(word, PinyinStyleEnum.NORMAL).trim());
            }
            // 正则匹配
            else if (table.startsWith(KEYWORD_TYPE_REGEXP)) {
                // TODO 正则匹配
                // query.andRegexp(KeyData::getWord, word);
                continue;
            }
            PaginationList<KeyData> paginationList = sqliteUtil.getPaginationList(query, page, size, KeyData.class);
            paginationList.forEach(k -> k.setType(table));
            if (paginationList.getTotalItemCount() > 0) return paginationList;
        }
        return new PaginationList<>(page, size, 0);
    }

    @Override
    public PaginationList<KeyData> getKeywordByPage(String word, long bot, long group, int page, int size) {
        return getKeywordByPage(word, bot, group, null, page, size);
    }

    @Override
    public PaginationList<KeyData> getKeywordByPage(String word, long bot, String type, int page, int size) {
        return getKeywordByPage(word, bot, 0, type, page, size);
    }

    @Override
    public PaginationList<KeyData> getKeywordByPage(String word, long bot, int page, int size) {
        return getKeywordByPage(word, bot, 0, null, page, size);
    }

    @Override
    public int deleteKeywordById(long id, String type) {
        return 0;
    }

    @Override
    public int deleteKeyword(String word, long bot, long group, String type) {
        return 0;
    }

    @Override
    public int deleteKeyword(String word, long bot, long group) {
        return 0;
    }

    @Override
    public int deleteKeyword(String word, long bot, String type) {
        return 0;
    }

    @Override
    public int deleteKeyword(String word, long bot) {
        return 0;
    }

    /**
     * 获取表名
     */
    @Nullable
    private String getTable(String type) {
        KeywordInstructions keyword = VanillaKanri.INSTANCE.getGlobalConfig().getInstructions().getKeyword();
        if (keyword.getExactly().contains(type)) {
            return getTableName(KEYWORD_TYPE_EXACTLY);
        } else if (keyword.getContain().contains(type)) {
            return getTableName(KEYWORD_TYPE_CONTAIN);
        } else if (keyword.getPinyin().contains(type)) {
            return getTableName(KEYWORD_TYPE_PINYIN);
        } else if (keyword.getRegex().contains(type)) {
            return getTableName(KEYWORD_TYPE_REGEXP);
        } else throw new RuntimeException("表不存在");
    }

    /**
     * 根据权级Level随机选择一个关键词
     */
    @NotNull
    public static KeyData selectRandom(@NotNull List<KeyData> keys) {
        // 按照 level 属性分组
        Map<Integer, List<KeyData>> groupedKeys = keys.stream()
                .collect(Collectors.groupingBy(KeyData::getLevel));

        // 对每个分组内的对象进行随机抽取，并合并成一个列表
        List<KeyData> selectedKeys = groupedKeys.values().stream()
                .map(k -> k.get(new Random().nextInt(k.size())))
                .collect(Collectors.toList());

        // 对合并后的列表进行随机抽取，并返回选中的对象
        int totalWeight = selectedKeys.stream().mapToInt(KeyData::getLevel).sum();
        if (totalWeight > 0) {
            int randomNumber = new Random().nextInt(totalWeight) + 1;
            int cumulativeWeight = 0;
            for (KeyData key : selectedKeys) {
                cumulativeWeight += key.getLevel();
                if (randomNumber <= cumulativeWeight) {
                    return key;
                }
            }
        }
        return new KeyData();
    }
}
