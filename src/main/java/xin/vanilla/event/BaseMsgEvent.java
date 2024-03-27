package xin.vanilla.event;


import lombok.Getter;
import lombok.Setter;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.SingleMessage;
import net.mamoe.mirai.utils.MiraiLogger;
import xin.vanilla.VanillaKanri;
import xin.vanilla.common.annotation.Capability;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BaseMsgEvent {
    protected static final VanillaKanri Va = VanillaKanri.INSTANCE;
    protected static final MiraiLogger logger = Va.getLogger();
    @Getter
    @Setter
    protected MessageChain msg;
    protected final Bot bot;
    protected final long time;

    BaseMsgEvent(MessageChain msg, Bot bot, long time) {
        this.msg = msg;
        this.bot = bot;
        this.time = time;
    }

    /**
     * 转义事件特殊码
     */
    protected void encodeVaEvent() {
        // 转义事件特殊码
        if (this.msg.contentToString().contains("(:vaevent:)")) {
            MessageChainBuilder messages = new MessageChainBuilder();
            for (SingleMessage singleMessage : msg) {
                if (singleMessage instanceof PlainText) {
                    PlainText plainText = (PlainText) singleMessage;
                    messages.add(plainText.contentToString().replace("(:vaevent:)", "\\(:vaevent:\\)"));
                } else {
                    messages.add(singleMessage);
                }
            }
            this.msg = messages.build();
        }
    }

    protected void run() {
        Map<String, Integer> capability = Va.getGlobalConfig().getBase().getCapability();
        Method[] methods = this.getClass().getDeclaredMethods();
        List<Method> methodList = Arrays.stream(methods)
                .filter(method -> method.isAnnotationPresent(Capability.class)
                        && method.getReturnType().equals(boolean.class))
                .collect(Collectors.toList());
        List<Map.Entry<String, Integer>> entryList = capability.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());
        for (Map.Entry<String, Integer> entry : entryList) {
            Method method = methodList.stream()
                    .filter(o -> (entry.getKey().equals(this.getClass().getSimpleName() + "." + o.getName())))
                    .findFirst()
                    .orElse(null);
            if (method != null) {
                try {
                    boolean result = (boolean) method.invoke(this);
                    if (result) return;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
