package org.saved4.saved4.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ChangeLog {
    private int changeId;
    private int orderId;
    private Timestamp modifiedAt;
    private String changeStatus;
    private String modifiedBy;
    private String orderOwner;
    private List<String> typeChanged;
    private Map<String, Object> dataInitial;
    private Map<String, Object> dataChanged;
    private Map<String, String> isRead;

    public ChangeLog() {}

    public ChangeLog(int changeId, int orderId, Timestamp modifiedAt, String changeStatus, String modifiedBy, String orderOwner) {
        this.changeId = changeId;
        this.orderId = orderId;
        this.modifiedAt = modifiedAt;
        this.changeStatus = changeStatus;
        this.modifiedBy = modifiedBy;
        this.orderOwner = orderOwner;
        this.typeChanged = new ArrayList<>();
        this.dataInitial = new HashMap<>();
        this.dataChanged = new HashMap<>();
        this.isRead = new HashMap<>();
    }

    // Getters and setters
    public int getChangeId() { return changeId; }
    public void setChangeId(int changeId) { this.changeId = changeId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public Timestamp getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(Timestamp dataModified) { this.modifiedAt = dataModified; }

    public List<ChangeStatus> getChangeStatus() {
        if (this.changeStatus == null || this.changeStatus.isEmpty()) {
            return List.of(); // Return an empty list if no status
        }
        // Split the string and convert each part back to ChangeStatus enum
        return Arrays.stream(this.changeStatus.split(","))
                .map(String::trim)
                .map(ChangeStatus::fromDbValue) // Uses the helper method in ChangeStatus enum
                .collect(Collectors.toList());
    }
    public void setChangeStatus(List<ChangeStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            this.changeStatus = null;
        } else {
            // Convert list of enums to comma-separated string of their dbValues
            this.changeStatus = statuses.stream()
                    .map(ChangeStatus::getDbValue)
                    .collect(Collectors.joining(","));
        }
    }

    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }

    public String getOrderOwner() { return orderOwner; }
    public void setOrderOwner(String orderOwner) { this.orderOwner = orderOwner; }

    public List<String> getTypeChanged() { return typeChanged; }
    public void setTypeChanged(List<String> typeChanged) { this.typeChanged = typeChanged; }

    public Map<String, Object> getDataInitial() { return dataInitial; }
    public void setDataInitial(Map<String, Object> dataInitial) { this.dataInitial = dataInitial; }

    public Map<String, Object> getDataChanged() { return dataChanged; }
    public void setDataChanged(Map<String, Object> dataChanged) { this.dataChanged = dataChanged; }

    public Map<String, String> getIsRead() {
        return isRead;
    }

    public void setIsRead(Map<String, String> isRead) {
        this.isRead = (isRead != null) ? isRead : new HashMap<>();
    }

    @Override
    public String toString() {
        return "ChangeLog{" +
                "changeId=" + changeId +
                ", orderId=" + orderId +
                ", modifiedAt=" + modifiedAt +
                ", modifiedBy='" + modifiedBy + '\'' +
                ", orderOwner='" + orderOwner + '\'' +
                ", typeChanged=" + typeChanged +
                ", dataInitial='" + dataInitial + '\'' +
                ", dataChanged='" + dataChanged + '\'' +
                ", changeStatus='" + changeStatus + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}
