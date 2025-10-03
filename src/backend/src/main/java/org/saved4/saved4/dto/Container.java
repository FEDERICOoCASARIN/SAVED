package org.saved4.saved4.dto;

import org.saved4.saved4.enums.ContainerStatus;

import java.io.Serializable;

public class Container implements Serializable {
    private Integer containerId;
    private Float maxWeight;
    private ContainerStatus containerStatus;

    public Container() {
    }

    public Container(float maxWeight) {
        this.maxWeight = maxWeight;
    }

    public Container(int containerId, float maxWeight) {
        this.containerId = containerId;
        this.maxWeight = maxWeight;
    }

    public Integer getContainerId() {
        return containerId;
    }

    public void setContainerId(int containerId) {
        this.containerId = containerId;
    }

    public Float getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(float maxWeight) {
        this.maxWeight = maxWeight;
    }

    public ContainerStatus getContainerStatus() {
        return containerStatus;
    }

    public void setContainerStatus(ContainerStatus containerStatus) {
        this.containerStatus = containerStatus;
    }
}
