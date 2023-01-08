package xin.vanilla.entity.instruction;

import java.util.HashSet;
import java.util.Set;

/**
 * 群管操作指令
 */
public class KanriInstructions {
    // 群管指令前缀-
    public String prefix = "";

    // 群管理+
    public Set<String> admin = new HashSet<String>() {{
        add("ad");
    }};

    // 头衔+
    public Set<String> tag = new HashSet<String>() {{
        add("tag");
    }};

    // 群名片+
    public Set<String> card = new HashSet<String>() {{
        add("card");
    }};

    // 戳一戳+
    public Set<String> tap = new HashSet<String>() {{
        add("tap");
        add("slap");
    }};

    // 禁言+
    public Set<String> mute = new HashSet<String>() {{
        add("mute");
        add("ban");
    }};

    // 解除禁言+
    public Set<String> loud = new HashSet<String>() {{
        add("loud");
    }};

    // 撤回+
    public Set<String> withdraw = new HashSet<String>() {{
        add("recall");
        add("withdraw");
        add("rec");
    }};

    // 踢出(危险操作, 使用特殊语法)*
    public String kick = "kick [VA_CODE.QQS] out";

    // 精华消息+
    public Set<String> essence = new HashSet<String>() {{
        add("essence");
        add("fine");
    }};
}
