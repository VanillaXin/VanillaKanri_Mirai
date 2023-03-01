package xin.vanilla.mapper;

public interface KeywordData {
    void createTable(String table);

    long addKeyword(String word, String rep, long bot, long group, String type, long time, int level);

    long addKeyword(String word, String rep, long bot, long group, String type, long time);

    long addKeyword(String word, String rep, long bot, long group, String type, int level);

    long addKeyword(String word, String rep, long bot, long group, String type);

}
