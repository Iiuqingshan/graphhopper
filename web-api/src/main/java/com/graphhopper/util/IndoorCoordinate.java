package com.graphhopper.util;

import org.locationtech.jts.geom.Coordinate;

public class IndoorCoordinate extends Coordinate {

    private String level;

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public IndoorCoordinate(double x, double y, double z) {
        super(x, y, z);
    }

    public IndoorCoordinate(double x, double y) {
        super(x, y);
    }

    public IndoorCoordinate(double x, double y, String level) {
        super(x, y);
        this.level = level;
    }
}
