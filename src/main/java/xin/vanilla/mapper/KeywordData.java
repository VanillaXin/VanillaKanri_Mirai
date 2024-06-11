package xin.vanilla.mapper;

import xin.vanilla.entity.data.KeyData;
import xin.vanilla.util.sqlite.PaginationList;

import java.util.List;

public interface KeywordData {
    // void createTable(String table);

    long addKeyword(String word, String rep, long bot, long group, String type, long time, int level);

    long addKeyword(String word, String rep, long bot, long group, String type, long time);

    long addKeyword(String word, String rep, long bot, long group, String type, int level);

    long addKeyword(String word, String rep, long bot, long group, String type);

    KeyData getKeywordById(long id, String type);

    KeyData getKeyword(String word, long bot, long group, String type);

    KeyData getKeyword(String word, long bot, long group);

    KeyData getKeyword(String word, long bot, String type);

    KeyData getKeyword(String word, long bot);

    List<KeyData> getKeywordList(String word, long bot, long group, String type);

    List<KeyData> getKeywordList(String word, long bot, long group);

    List<KeyData> getKeywordList(String word, long bot, String type);

    List<KeyData> getKeywordList(String word, long bot);

    PaginationList<KeyData> getKeywordByPage(String word, long bot, long group, String type, int page, int size);

    PaginationList<KeyData> getKeywordByPage(String word, long bot, String type, int page, int size);

    PaginationList<KeyData> getKeywordByPage(long bot, String type, int page, int size);

    PaginationList<KeyData> getKeywordByPage(long bot, long group, String type, int page, int size);

    int deleteKeywordById(long id, String type, int level);

    int deleteKeyword(String word, long bot, long group, String type, int level);

    int deleteKeyword(String word, long bot, long group, int level);

    int deleteKeyword(String word, long bot, String type, int level);

    int deleteKeyword(String word, long bot, int level);

    int updateStatus(long id, int status, String type);
}
