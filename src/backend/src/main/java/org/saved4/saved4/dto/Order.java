package org.saved4.saved4.dto;


import org.saved4.saved4.enums.FreightType;
import org.saved4.saved4.enums.OperationType;
import org.saved4.saved4.enums.OrderStatus;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Order {
    private Integer orderId;
    private Integer containerId;
    private Integer vehicleId;
    private String requester;
    private String order_for;


    private Integer routeId;
    private Timestamp madeAt;
    private String source;
    private String destination;
    private Boolean preferredShared;
    private Boolean isShared;


    private Timestamp twStart;
    private Timestamp twEnd;
    private Timestamp eta;
    private OrderStatus status;
    private OperationType operationType;
    private BigDecimal freightValue;
    private Coordinate sourcePosition;
    private Coordinate destinationPosition;
    private BigDecimal freightWeight;
    private FreightType freightType;
    private Timestamp departureTime;

    public Timestamp getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Timestamp departureTime) {
        this.departureTime = departureTime;
    }


    public Boolean getPreferredShared() {
        return preferredShared;
    }

    public void setPreferredShared(Boolean preferredShared) {
        this.preferredShared = preferredShared;
    }

    public String getOrder_for() {
        return order_for;
    }

    public void setOrder_for(String order_for) {
        this.order_for = order_for;
    }


    public BigDecimal getFreightWeight() {
        return freightWeight;
    }

    public void setFreightWeight(BigDecimal freightWeight) {
        this.freightWeight = freightWeight;
    }

    public FreightType getFreightType() {
        return freightType;
    }

    public void setFreightType(FreightType freightType) {
        this.freightType = freightType;
    }

    public int getOrderId() {
        return orderId;
    }

    public Coordinate getSourcePosition() {
        return sourcePosition;
    }

    public void setSourcePosition(Coordinate sourcePosition) {
        this.sourcePosition = sourcePosition;
    }

    public Coordinate getDestinationPosition() {
        return destinationPosition;
    }

    public void setDestinationPosition(Coordinate destinationPosition) {
        this.destinationPosition = destinationPosition;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Integer getContainerId() {
        return containerId;
    }

    public void setContainerId(Integer containerId) {
        this.containerId = containerId;
    }

    public Integer getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Integer vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getRequester() {
        return requester;
    }

    public void setRequester(String requester) {
        this.requester = requester;
    }

    public Integer getRouteId() {
        return routeId;
    }

    public void setRouteId(Integer routeId) {
        this.routeId = routeId;
    }

    public Timestamp getMadeAt() {

        return madeAt;
    }

    public void setMadeAt(Timestamp madeAt) {
        this.madeAt = madeAt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Boolean getShared() {
        return isShared;
    }

    public void setShared(Boolean shared) {
        isShared = shared;
    }

    public Timestamp getTwStart() {
        return twStart;
    }

    public void setTwStart(Timestamp twStart) {
        this.twStart = twStart;
    }

    public Timestamp getTwEnd() {
        return twEnd;
    }

    public void setTwEnd(Timestamp twEnd) {
        this.twEnd = twEnd;
    }

    public Timestamp getEta() {
        return eta;
    }

    public void setEta(Timestamp eta) {
        this.eta = eta;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }


    public OperationType getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    public BigDecimal getFreightValue() {
        return freightValue;
    }

    public void setFreightValue(BigDecimal freightValue) {
        this.freightValue = freightValue;
    }

    @Override
    public String toString() {
        return "Order{" + "orderId=" + orderId + ", containerId=" + containerId + ", vehicleId=" +
                vehicleId + ", requester='" + requester + '\'' + ", routeId=" + routeId +
                ", madeAt=" + madeAt + ", source='" + source + '\'' + ", destination='" +
                destination + '\'' + ", isShared=" + isShared + ", twStart=" + twStart +
                ", twEnd=" + twEnd + ", eta=" + eta + ", status=" + status + ", operationType=" +
                operationType + ", freightValue=" + freightValue + ", sourcePosition=" +
                sourcePosition + ", destinationPosition=" + destinationPosition +
                ", freightWeight=" + freightWeight + ", freightType=" + freightType + '}';
    }
}
