package org.saved4.saved4.dto.chat;

import org.saved4.saved4.dto.Message;
import java.io.Serializable;

public class Chat implements Serializable {
    private String participant; // For admin: company name, for company: "Admin"
    private String company;     // Always the company name
    private int unread;
    private Message lastMsg;    // Last message in the chat

    public Chat() {
        // Default constructor for JSON serialization
    }

    public Chat(String participant, String company, int unread, Message lastMsg) {
        this.participant = participant;
        this.company = company;
        this.unread = unread;
        this.lastMsg = lastMsg;
    }

    public Chat(String company, int unread) {
        this.company = company;
        this.participant = company;
        this.unread = unread;
    }

    public String getParticipant() {
        return participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public Message getLastMsg() {
        return lastMsg;
    }

    public void setLastMsg(Message lastMsg) {
        this.lastMsg = lastMsg;
    }
}
