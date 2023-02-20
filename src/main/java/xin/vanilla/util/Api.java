package xin.vanilla.util;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.PlainText;
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
        MessageReceipt<Contact> contactMessageReceipt = contact.sendMessage(message);
        Va.getMessageCache().addMsg(contact, contactMessageReceipt.getSource(), MessageUtils.newChain(new PlainText(message)));
        return contactMessageReceipt;
    }

    /**
     * 发送消息
     */
    public static MessageReceipt<Contact> sendMessage(Contact contact, Message message) {
        MessageReceipt<Contact> contactMessageReceipt = contact.sendMessage(message);
        Va.getMessageCache().addMsg(contact, contactMessageReceipt.getSource(), message);
        return contactMessageReceipt;
    }
}
