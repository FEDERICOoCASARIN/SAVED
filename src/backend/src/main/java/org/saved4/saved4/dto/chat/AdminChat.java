package org.saved4.saved4.dto.chat;

import org.saved4.saved4.dto.Message;
import java.io.Serializable;

public class AdminChat extends Chat {

    public AdminChat(String company, int unread, Message lastMsg) {
        super(company, company, unread, lastMsg); // participant = company for admin view
    }
}
