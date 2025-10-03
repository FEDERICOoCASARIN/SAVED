package org.saved4.saved4.dto.chat;

import org.saved4.saved4.dto.Message;

public class CompanyChat extends Chat {
    public CompanyChat(String company, int unread, Message lastMsg) {
        super("Admin", company, unread, lastMsg); // participant = "Admin" for company view
    }
}
