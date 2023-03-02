package xin.vanilla.util;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.*;
import xin.vanilla.VanillaKanri;

/**
 * 整合部分接口
 */
public class Api {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;

    /**
     * 发送消息
     */
    public static MessageReceipt<Contact> sendMessage(Contact contact, String message) {
        // 反转义事件特殊码
        if (message.startsWith("\\(:vaevent:\\)"))
            message = message.replace("\\(:vaevent:\\)", "(:vaevent:)");
        MessageReceipt<Contact> contactMessageReceipt = contact.sendMessage(message);
        Va.addMsgSendCount();
        Va.getMessageCache().addMsg(contact, contactMessageReceipt.getSource(), MessageUtils.newChain(new PlainText(message)));
        return contactMessageReceipt;
    }

    /**
     * 发送消息
     */
    public static MessageReceipt<Contact> sendMessage(Contact contact, Message message) {
        // 反转义事件特殊码
        if (message instanceof MessageChain) {
            if (message.contentToString().startsWith("\\(:vaevent:\\)")) {
                MessageChain messageChain = (MessageChain) message;
                MessageChainBuilder messages = new MessageChainBuilder();
                for (SingleMessage singleMessage : messageChain) {
                    if (singleMessage instanceof PlainText) {
                        PlainText plainText = (PlainText) singleMessage;
                        messages.add(plainText.contentToString().replace("\\(:vaevent:\\)", "(:vaevent:)"));
                    } else {
                        messages.add(singleMessage);
                    }
                }
                message = messages.build();
            }
        }
        MessageReceipt<Contact> contactMessageReceipt = contact.sendMessage(message);
        Va.addMsgSendCount();
        Va.getMessageCache().addMsg(contact, contactMessageReceipt.getSource(), message);
        return contactMessageReceipt;
    }
}
