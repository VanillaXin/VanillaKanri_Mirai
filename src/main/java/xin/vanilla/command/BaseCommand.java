package xin.vanilla.command;

import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;
import xin.vanilla.VanillaKanri;

@Deprecated
public class BaseCommand extends JRawCommand {
    public static final BaseCommand INSTANCE = new BaseCommand();
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;

    public BaseCommand() {
        // 使用插件主类对象作为指令拥有者；设置主指令名为 "va"
        super(Va, "va");

        // 可选设置如下属性
        // 设置用法，这将会在 /help 中展示
        setUsage("/va");
        // 设置描述，也会在 /help 中展示
        setDescription("这是插件所有指令的前缀");
        // 设置指令前缀是可选的，即使用 `va` 也能执行指令而不需要 `/va`
        setPrefixOptional(false);
    }

    @Override
    public void onCommand(@NotNull CommandContext context, @NotNull MessageChain args) {

    }
}
