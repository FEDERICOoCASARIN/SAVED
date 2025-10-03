package org.saved4.saved4.dto;

public class Coordinate {
    private double x;
    private double y;

    public Coordinate() {
        // Required for Jackson
    }

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    // Getters
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    // Optional: toString()
    @Override
    public String toString() {
        return "Coordinate(" + x + ", " + y + ")";
    }
}
