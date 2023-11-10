package xin.vanilla.event;

import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.message.data.MessageChain;
import xin.vanilla.common.RegExpConfig;
import xin.vanilla.common.annotation.Capability;
import xin.vanilla.entity.KeyRepEntity;
import xin.vanilla.entity.data.KeyData;
import xin.vanilla.util.Api;
import xin.vanilla.util.VanillaUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FriendMsgEvent extends BaseMsgEvent {
    private final FriendMessageEvent event;
    private final Friend friend;


    public FriendMsgEvent(FriendMessageEvent event) {
        super(event.getMessage(), event.getBot(), event.getTime());
        this.event = event;
        this.friend = this.event.getSender();
        Va.getMessageCache().addMsg(this.friend, this.msg);
    }

    public void run() {
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

    /**
     * 解析关键词回复
     */
    @Capability
    private void keyRep() {
        // 关键词查询
        KeyData keyword = Va.getKeywordData().getKeyword(VanillaUtils.messageToString(msg), bot.getId(), -2);
        if (keyword.getId() > 0) {
            MessageChain rep = RegExpConfig.VaCode.exeReply(keyword.getRepDecode(null, bot, friend, msg), msg, friend);
            KeyRepEntity keyRepEntity = new KeyRepEntity(friend);
            keyRepEntity.setMsg(VanillaUtils.messageToString(msg));
            keyRepEntity.setSenderId(friend.getId());
            keyRepEntity.setSenderName(friend.getNick());
            Api.sendMessage(keyRepEntity, rep);
        }
    }
}
