package xin.vanilla.mapper;

public interface KeywordData {
    void createTable(String table);

    void addKeyword(String word, String rep, long bot, long group, String type, long time, int level);

    void addKeyword(String word, String rep, long bot, long group, String type, long time);

    void addKeyword(String word, String rep, long bot, long group, String type, int level);

    void addKeyword(String word, String rep, long bot, long group, String type);

}
