package xin.vanilla.entity.instruction;

/**
 * 群管操作指令
 */
public class KanriInstructions {
    // 群管指令前缀-
    public String prefix = "";

    // 群管理+
    public String[] admin = new String[]{"ad"};

    // 头衔+
    public String[] tag = new String[]{"tag"};

    // 群名片+
    public String[] card = new String[]{"card"};

    // 戳一戳+
    public String[] tap = new String[]{"tap", "slap"};

    // 禁言+
    public String[] mute = new String[]{"mute", "ban"};

    // 解除禁言+
    public String[] loud = new String[]{"loud"};

    // 撤回+
    public String[] withdraw = new String[]{"withdraw", "recall", "rec"};

    // 踢出(危险操作, 使用特殊语法)*
    public String kick = "kick [VA_CODE.QQS] out";

    // 精华消息+
    public String[] essence = new String[]{"essence", "fine"};
}
