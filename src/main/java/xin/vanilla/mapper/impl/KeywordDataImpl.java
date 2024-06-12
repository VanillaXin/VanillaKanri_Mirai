package xin.vanilla.mapper.impl;

import cn.hutool.core.date.DateUtil;
import com.github.houbb.pinyin.constant.enums.PinyinStyleEnum;
import com.github.houbb.pinyin.util.PinyinHelper;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;
import xin.vanilla.config.KeyDataFile;
import xin.vanilla.entity.config.instruction.KeywordInstructions;
import xin.vanilla.entity.data.KeyData;
import xin.vanilla.mapper.KeywordData;
import xin.vanilla.util.SettingsUtils;
import xin.vanilla.util.StringUtils;
import xin.vanilla.util.VanillaUtils;
import xin.vanilla.util.sqlite.PaginationList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class KeywordDataImpl extends Base implements KeywordData {

    public static String KEYWORD_TYPE_EXACTLY = "exactly";
    public static String KEYWORD_TYPE_CONTAIN = "contain";
    public static String KEYWORD_TYPE_PINYIN = "pinyin";
    public static String KEYWORD_TYPE_REGEXP = "regexp";

    private final List<KeyData> keyword;

    public KeywordDataImpl(KeyDataFile keyword) {
        this.keyword = keyword.getKey();
    }

    @Override
    public long addKeyword(String word, String rep, long bot, long group, String type, long time, int level) {
        String table = getTable(type);
        // TODO 定义特殊码, 转义特殊码, 判断普通群员的消息中是否有群管操作特殊码
        String wordCode = VanillaUtils.enVanillaCodeKey(word);

        // 查询该level创建的关键词数量是否超出限制
        Stream<KeyData> keyDataStream = keyword.stream()
                .filter(key -> key.getType().equals(table))
                .filter(key -> key.getWord().equals(wordCode))
                .filter(key -> key.getBot() == bot)
                .filter(key -> key.getLevel() <= (level > 0 ? level : 1));

        if (group < -1000) {
            keyDataStream = keyDataStream.filter(key -> key.getGroup() == -1 || key.getGroup() == Math.abs(group));
        } else {
            keyDataStream = keyDataStream.filter(key -> key.getGroup() == group);
        }
        List<KeyData> list = keyDataStream.sorted(Comparator.comparing(KeyData::getId)).collect(Collectors.toList());

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
        KeyData insert = new KeyData();
        insert.setWord(wordCode);
        insert.setRep(rep);
        insert.setBot(bot);
        insert.setGroup(group);
        insert.setTime(time);
        insert.setLevel(level > 0 ? level : 1);
        insert.setType(table);
        // 判断是否非普通群员添加的关键词
        if (level > 1 || SettingsUtils.getKeyAutoExamine(group)) {
            insert.setStatus(1);
        }
        insert.setId(keyword.stream().max(Comparator.comparing(KeyData::getId)).orElse(new KeyData()).getId() + 1);
        keyword.add(insert);
        return insert.getId();
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
        return keyword.stream()
                .filter(key -> key.getType().equals(table))
                .filter(key -> key.getId() == id)
                .findFirst().orElse(new KeyData());
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
        word = VanillaUtils.enVanillaCodeKey(word);
        String table = "";

        Stream<KeyData> keyDataStream = keyword.stream()
                .filter(key -> key.getBot() == bot)
                .filter(key -> key.getStatus() > 0);
        if (StringUtils.isNotNullOrEmpty(type)) {
            table = getTable(type);
            keyDataStream = keyDataStream.filter(key -> key.getType().equals(getTable(type)));
        }

        if (group < -1000) {
            keyDataStream = keyDataStream.filter(key -> key.getGroup() == -1 || key.getGroup() == Math.abs(group));
        } else if (group == -2) {
            keyDataStream = keyDataStream.filter(key -> key.getGroup() == -1 || key.getGroup() == group);
        } else if (group != 0) {
            keyDataStream = keyDataStream.filter(key -> key.getGroup() == group);
        }

        keyDataStream = andWord(word, table, keyDataStream);
        return keyDataStream.collect(Collectors.toList());
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
        String table = "";

        Stream<KeyData> keyDataStream = keyword.stream()
                .filter(key -> key.getBot() == bot)
                .filter(key -> key.getStatus() > 0);
        if (StringUtils.isNotNullOrEmpty(type)) {
            table = getTable(type);
            keyDataStream = keyDataStream.filter(key -> key.getType().equals(getTable(type)));
        }
        if (group < -1000) {
            keyDataStream = keyDataStream.filter(key -> key.getGroup() == -1 || key.getGroup() == Math.abs(group));
        } else if (group != 0) {
            keyDataStream = keyDataStream.filter(key -> key.getGroup() == group);
        }

        if (StringUtils.isNotNullOrEmpty(word)) {
            word = VanillaUtils.enVanillaCodeKey(word);
            keyDataStream = andWord(word, table, keyDataStream);
        }
        List<KeyData> list = keyDataStream.collect(Collectors.toList());
        PaginationList<KeyData> result = new PaginationList<>(page, size, list.size());
        result.addAll(list);
        return result;
    }

    private Stream<KeyData> andWord(String word, @NotNull String table, Stream<KeyData> query) {
        if (StringUtils.isNullOrEmpty(word)) {
            query = query.filter(key -> {
                if ((key.getPattern() == null && (KEYWORD_TYPE_REGEXP.equals(key.getType())))) {
                    key.setPattern(Pattern.compile(word));
                }
                return (KEYWORD_TYPE_EXACTLY.equals(key.getType()) && word.equals(key.getWord()))
                        || (KEYWORD_TYPE_CONTAIN.equals(key.getType()) && word.contains(key.getWord()))
                        || (KEYWORD_TYPE_PINYIN.equals(key.getType()) && PinyinHelper.toPinyin(word, PinyinStyleEnum.NORMAL).trim().contains(key.getWord()))
                        || (KEYWORD_TYPE_REGEXP.equals(key.getType()) && key.getPattern().matcher(word).matches());
            });
        }
        // 完全匹配
        else if (table.startsWith(KEYWORD_TYPE_EXACTLY)) {
            query = query.filter(key -> KEYWORD_TYPE_EXACTLY.equals(key.getType()))
                    .filter(key -> word.equals(key.getWord()));
        }
        // 包含匹配
        else if (table.startsWith(KEYWORD_TYPE_CONTAIN)) {
            query = query.filter(key -> KEYWORD_TYPE_CONTAIN.equals(key.getType()))
                    .filter(key -> word.contains(key.getWord()));
        }
        // 拼音包含匹配
        else if (table.startsWith(KEYWORD_TYPE_PINYIN)) {
            query = query.filter(key -> KEYWORD_TYPE_PINYIN.equals(key.getType()))
                    .filter(key -> PinyinHelper.toPinyin(word, PinyinStyleEnum.NORMAL).trim().contains(key.getWord()));
        }
        // 正则匹配
        else if (table.startsWith(KEYWORD_TYPE_REGEXP)) {
            query = query.filter(key -> KEYWORD_TYPE_REGEXP.equals(key.getType()))
                    .filter(key -> {
                        if (key.getPattern() == null) {
                            key.setPattern(Pattern.compile(word));
                        }
                        return key.getPattern().matcher(word).matches();
                    });
        }
        // 默认使用完全匹配
        else {
            query = query.filter(key -> KEYWORD_TYPE_EXACTLY.equals(key.getType()))
                    .filter(key -> key.getWord().equals(word));
        }
        return query;
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
        KeyData keyData = keyword.stream()
                .filter(key -> key.getType().equals(table))
                .filter(key -> key.getId() == id)
                .findFirst().orElse(new KeyData());
        if (keyData.getLevel() > level) return -2;

        return keyword.removeIf(key -> key.getId() == id) ? 1 : -1;
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
        KeyData keyData = keyword.stream()
                .filter(key -> key.getType().equals(table))
                .filter(key -> key.getId() == id)
                .findFirst().orElse(null);
        if (keyData != null) {
            keyData.setStatus(status);
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * 获取表名
     */
    @NotNull
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
