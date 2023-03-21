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
import xin.vanilla.util.SettingsUtils;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.VanillaUtils;
import xin.vanilla.util.lambda.LambdaUtils;
import xin.vanilla.util.sqlite.PaginationList;
import xin.vanilla.util.sqlite.SqliteUtil;
import xin.vanilla.util.sqlite.statement.*;

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

    private final VanillaKanri Va = VanillaKanri.INSTANCE;

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
                    "CREATE TABLE IF NOT EXISTS `" + table + "` (" +
                            " `" + LambdaUtils.getFiledName(KeyData::getId) + "`     INTEGER     PRIMARY KEY AUTOINCREMENT NOT NULL," +
                            " `" + LambdaUtils.getFiledName(KeyData::getWord) + "`   TEXT                                  NOT NULL," +
                            " `" + LambdaUtils.getFiledName(KeyData::getRep) + "`    TEXT                                  NOT NULL," +
                            " `" + LambdaUtils.getFiledName(KeyData::getBot) + "`    INTEGER(10)                           NOT NULL," +
                            " `" + LambdaUtils.getFiledName(KeyData::getGroup) + "`  INTEGER(10)                           NOT NULL," +
                            " `" + LambdaUtils.getFiledName(KeyData::getTime) + "`   INTEGER(10)                           NOT NULL," +
                            " `" + LambdaUtils.getFiledName(KeyData::getLevel) + "`  INTEGER(4)                            NOT NULL DEFAULT 1," +
                            " `" + LambdaUtils.getFiledName(KeyData::getStatus) + "` INTEGER(1)                            NOT NULL DEFAULT 0" +
                            ")");
            sqliteUtil.executeSql("CREATE UNIQUE INDEX IF NOT EXISTS `"
                    + LambdaUtils.getFiledName(KeyData::getWord)
                    + "_" + LambdaUtils.getFiledName(KeyData::getRep)
                    + "_" + LambdaUtils.getFiledName(KeyData::getGroup)
                    + "_unique`" + " ON `" + table + "` (" +
                    "`" + LambdaUtils.getFiledName(KeyData::getWord) + "`, " +
                    "`" + LambdaUtils.getFiledName(KeyData::getRep) + "`, " +
                    "`" + LambdaUtils.getFiledName(KeyData::getGroup) + "`)");
        }
    }

    @Override
    public long addKeyword(String word, String rep, long bot, long group, String type, long time, int level) {
        String table = getTable(type);
        createTable(table);
        // TODO 定义特殊码, 转义特殊码, 判断普通群员的消息中是否有群管操作特殊码
        String wordCode = VanillaUtils.enVanillaCodeKey(word);

        // 查询该level创建的关键词数量是否超出限制
        Statement query = QueryStatement.produce(KeyData::getId).from(table)
                .where(KeyData::getWord).eq(wordCode)
                .and(KeyData::getBot).eq(bot);

        if (group < -1000) {
            query.and(KeyData::getGroup).in(-1, Math.abs(group));
        } else {
            query.and(KeyData::getGroup).eq(group);
        }

        query.and(KeyData::getLevel).eq(level > 0 ? level : 1)
                .orderBy(KeyData::getId).asc();
        List<KeyData> list = sqliteUtil.getList(query, KeyData.class);
        // 根据策略判断是否自动删除最旧的关键词
        if (list.size() >= SettingsUtils.getKeyRadix(group)) {
            if (SettingsUtils.getKeyRadixAutoDel(group)) {
                if (deleteKeywordById(list.get(0).getId(), type, level > 0 ? level : 1) < 0) {
                    return -2;
                }
            } else {
                return -2;
            }
        }

        InsertStatement insert = InsertStatement.produce(table)
                .put(KeyData::getWord, wordCode)
                .put(KeyData::getRep, rep)
                .put(KeyData::getBot, bot)
                .put(KeyData::getGroup, group)
                .put(KeyData::getTime, time)
                .put(KeyData::getLevel, level > 0 ? level : 1);
        // 判断是否非普通群员添加的关键词
        if (level > 1 || SettingsUtils.getKeyAutoExamine(group)) {
            insert.put(KeyData::getStatus, 1);
        }
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

        word = VanillaUtils.enVanillaCodeKey(word);

        List<KeyData> keys = new ArrayList<>();
        for (String typeString : types) {
            String table = getTable(typeString);
            createTable(table);
            Statement query = QueryStatement.produce().from(table)
                    .where(KeyData::getBot).eq(bot)
                    .and(KeyData::getStatus).gt(0);

            if (group < -1000) {
                query.and(KeyData::getGroup).in(-1, Math.abs(group));
            } else if (group == -2) {
                query.and(KeyData::getGroup).in(-1, group);
            } else if (group != 0) {
                query.and(KeyData::getGroup).eq(group);
            }

            assert table != null;
            andWord(word, table, query);
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
        String table = getTable(type);
        createTable(table);
        Statement query = QueryStatement.produce().from(table)
                .where(KeyData::getBot).eq(bot)
                .and(KeyData::getStatus).gt(0);
        if (group < -1000) {
            query.and(KeyData::getGroup).in(-1, Math.abs(group));
        } else if (group != 0) {
            query.and(KeyData::getGroup).eq(group);
        }

        assert table != null;
        if (!StringUtils.isNullOrEmpty(word)) {
            word = VanillaUtils.enVanillaCodeKey(word);
            andWord(word, table, query);
        }
        PaginationList<KeyData> paginationList = sqliteUtil.getPaginationList(query, page, size, KeyData.class);
        paginationList.forEach(k -> k.setType(table));
        if (paginationList.getTotalItemCount() > 0) return paginationList;

        return new PaginationList<>(page, size, 0);
    }

    private void andWord(String word, @NotNull String table, Statement query) {
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
            query.andRegexp(KeyData::getWord, word);
        }
    }

    @Override
    public PaginationList<KeyData> getKeywordByPage(String word, long bot, String type, int page, int size) {
        return getKeywordByPage(word, bot, 0, type, page, size);
    }

    @Override
    public PaginationList<KeyData> getKeywordByPage(long bot, String type, int page, int size) {
        return getKeywordByPage(null, bot, 0, type, page, size);
    }

    @Override
    public PaginationList<KeyData> getKeywordByPage(long bot, long group, String type, int page, int size) {
        return getKeywordByPage(null, bot, group, type, page, size);
    }

    @Override
    public int deleteKeywordById(long id, String type, int level) {
        String table = getTable(type);
        // 查询该id的关键词的level
        Statement query = QueryStatement.produce().from(table)
                .where(KeyData::getId).eq(id)
                .orderBy(KeyData::getId).asc();
        KeyData keyData = sqliteUtil.getEntity(query, KeyData.class);
        if (keyData.getLevel() > level) return -2;

        Statement deleteStatement = DeleteStatement.produce(table)
                .where(KeyData::getId).eq(id);
        return sqliteUtil.delete(deleteStatement);
    }

    @Override
    public int deleteKeyword(String word, long bot, long group, String type, int level) {
        return 0;
    }

    @Override
    public int deleteKeyword(String word, long bot, long group, int level) {
        return 0;
    }

    @Override
    public int deleteKeyword(String word, long bot, String type, int level) {
        return 0;
    }

    @Override
    public int deleteKeyword(String word, long bot, int level) {
        return 0;
    }

    @Override
    public int updateStatus(long id, int status, String type) {
        String table = getTable(type);
        Statement updateStatement = UpdateStatement.produce(table)
                .set(KeyData::getStatus, status)
                .where(KeyData::getId).eq(id);
        return sqliteUtil.update(updateStatement);
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
