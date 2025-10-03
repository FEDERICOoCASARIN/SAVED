package org.saved4.saved4.dto;

import java.io.Serializable;

public class Costs implements Serializable {
    private Float emptyCtr;
    private Float fullCtr;
    private Float perKm;

    public Costs() {
    }

    public Costs(Float emptyCtr, Float fullCtr, Float perKm) {
        this.emptyCtr = emptyCtr;
        this.fullCtr = fullCtr;
        this.perKm = perKm;
    }


    public Float getEmptyCtr() {
        return emptyCtr;
    }

    public void setEmptyCtr(Float emptyCtr) {
        this.emptyCtr = emptyCtr;
    }

    public Float getFullCtr() {
        return fullCtr;
    }

    public void setFullCtr(Float fullCtr) {
        this.fullCtr = fullCtr;
    }

    public Float getPerKm() {
        return perKm;
    }

    public void setPerKm(Float perKm) {
        this.perKm = perKm;
    }
}
