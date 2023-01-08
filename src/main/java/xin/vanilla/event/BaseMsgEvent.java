package xin.vanilla.event;


import net.mamoe.mirai.utils.MiraiLogger;
import xin.vanilla.VanillaKanri;

public class BaseMsgEvent {
    protected static final VanillaKanri Va = VanillaKanri.INSTANCE;
    protected static final MiraiLogger logger = Va.getLogger();

    BaseMsgEvent() {
        Va.config.init();
    }
}
