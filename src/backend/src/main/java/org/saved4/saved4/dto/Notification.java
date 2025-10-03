package org.saved4.saved4.dto;

import java.io.Serializable;
import java.sql.Timestamp;

import org.saved4.saved4.enums.NotificationType;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

/**
 * Represents a notification object used in the system.
 * Contains data about the notification including related order/message,
 * receiver info, timestamp, and read status.
 */
public class Notification implements Serializable {
    @JsonProperty("id")
    private int id;
    private NotificationType type;
    private String title;
    private String data;
    private String receiver;
    private Integer orderId;
    private Integer messageId;
    private boolean read;
    @JsonSerialize(using = ToStringSerializer.class)
    private Timestamp timestamp;
     
    /** Default no-args constructor. */
    public Notification() {}


    /**
     * Parameterized constructor for Notification.
     *
     * @param id the notification ID
     * @param data the message or data associated with the notification
     * @param receiver the receiver's username
     * @param orderId the associated order ID
     * @param messageId the associated message ID
     * @param read whether the notification has been read
     * @param timestamp the timestamp of when the notification was created
     */
    public Notification(int id, String title, NotificationType type, String data, String receiver, Integer orderId, Integer messageId, boolean read, Timestamp timestamp) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.data = data;
        this.receiver = receiver;
        this.orderId = orderId;
        this.messageId = messageId;
        this.read = read;
        this.timestamp = timestamp;
    }


    /** Returns the notification ID. */
    public int getId() { return id; }
    

    /** Sets the notification ID. */
    public void setId(int id) { 
        this.id = id; }

    /** Returns the notification data/message. */
    public String getData() { 
        return data; }
        
    /** Sets the notification data/message. */
    public void setData(String data) { 
        this.data = data; }

    /** Returns the receiver's username. */
    public String getReceiver() { 
        return receiver; }

    /** Sets the receiver's username. */
    public void setReceiver(String receiver) { 
        this.receiver = receiver; }

    /** Returns the associated order ID. */
    public Integer getOrderId() { 
        return orderId; }

    /** Sets the associated order ID. */
    public void setOrderId(Integer orderId) { 
        this.orderId = orderId; }

    /** Returns the associated message ID. */
    public Integer getMessageId() { 
        return messageId; }

    /** Sets the associated message ID. */
    public void setMessageId(Integer messageId) { 
        this.messageId = messageId; }

    /** Returns whether the notification has been read. */
    public boolean isRead() {
         return read; }

    /** Sets the read status of the notification. */
    public void setRead(boolean read) { 
        this.read = read; }
    
    /** Returns the timestamp of the notification. */
    public Timestamp getTimestamp() { 
        return timestamp; }
        
    /** Sets the timestamp of the notification. */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp; }
        
    /** Returns the notification type (placeholder logic). */
    public NotificationType getType() {
        return type;
    }    

        /** Returns the notification type (placeholder logic). */
    public void setType(NotificationType type) {
        this.type = type;
    }    

    /** Returns the title of the notification. */
    public String getTitle() {
        return title;
    }    

    /** Sets the title of the notification. */
	public void setTitle(String title) {
		this.title = title;
	}
}