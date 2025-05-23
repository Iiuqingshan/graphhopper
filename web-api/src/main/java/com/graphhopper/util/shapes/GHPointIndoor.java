package com.graphhopper.util.shapes;

public class GHPointIndoor extends GHPoint3D {
    public double lat;
    public double lon;
    public int level;

    public GHPointIndoor(double lat, double lon, int level) {
        super(lat, lon, Double.NaN);
        this.lat = lat;
        this.lon = lon;
        this.level = level;
    }

    public static GHPointIndoor parse(String str) {
        return parse(str, false);
    }

    private static GHPointIndoor parse(String str, boolean lonlatOrder) {
        String[] fromStrs = str.split(",");
        if (fromStrs.length == 3) {
            try {
                double fromLat = Double.parseDouble(fromStrs[0]);
                double fromLon = Double.parseDouble(fromStrs[1]);
                int level = Integer.parseInt(fromStrs[2].trim());
                if (lonlatOrder) {
                    return new GHPointIndoor(fromLon, fromLat, level);
                }
                return new GHPointIndoor(fromLat, fromLon, level);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    public int getLevel() {
        return this.level;
    }

    @Override
    public boolean equals(Object obj) {
        GHPointIndoor other = (GHPointIndoor) obj;
        return super.equals(obj) && other.level == this.level;
    }

    @Override
    public String toString() {
        return super.toString() + "," + this.level;
    }
}
