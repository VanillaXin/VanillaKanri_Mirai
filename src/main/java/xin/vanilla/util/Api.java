package xin.vanilla.util;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.message.MessageReceipt;
import net.mamoe.mirai.message.data.Message;
import net.mamoe.mirai.message.data.MessageUtils;
import xin.vanilla.VanillaKanri;

/**
 * 整合部分接口
 */
public class Api {
    private static final VanillaKanri Va = VanillaKanri.INSTANCE;

    public static MessageReceipt<Contact> sendMessage(Contact contact, String message) {
        MessageReceipt<Contact> contactMessageReceipt = contact.sendMessage(message);
        Va.messageCache.addMsg(contact, contactMessageReceipt.getSource(), MessageUtils.newChain(message));
        return contactMessageReceipt;
    }

    public static MessageReceipt<Contact> sendMessage(Contact contact, Message message) {
        MessageReceipt<Contact> contactMessageReceipt = contact.sendMessage(message);
        Va.messageCache.addMsg(contact, contactMessageReceipt.getSource(), message);
        return contactMessageReceipt;
    }
}
