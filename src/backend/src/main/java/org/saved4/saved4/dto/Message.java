package org.saved4.saved4.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.sql.Timestamp;

public class Message implements Serializable {
    int messageId;
    String company;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    Timestamp time;
    String sender;
    String body;

    public Message() {
        // Default constructor for JSON deserialization
    }

    public Message(int messageId, String company, Timestamp time, String sender, String body) {
        this.messageId = messageId;
        this.company = company;
        this.time = time;
        this.body = body;
        this.sender = sender;
    }

    public Message(String company, Timestamp time, String sender, String body) {
        this.company = company;
        this.time = time;
        this.body = body;
        this.sender = sender;
    }

    public Message(String company, String body) {
        this.company = company;
        this.body = body;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
