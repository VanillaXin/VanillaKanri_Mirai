package xin.vanilla.entity.instruction;

import java.util.HashSet;
import java.util.Set;

/**
 * 插件指令集合
 */
public class Instructions {
    // -: 可以有0~1个, *: 有且只有1个, +:可以有1~∞个

    // 顶级前缀-
    public String prefix = "/va";

    // 二级前缀列表
    public Set<String> secondaryPrefix = new HashSet<>();

    // 基础指令
    public BaseInstructions base = new BaseInstructions();

    // 关键词指令
    public KeywordInstructions keyword = new KeywordInstructions();

    // 群管指令
    public KanriInstructions kanri = new KanriInstructions();
}
