package com.graphhopper.routing.ev;

import com.graphhopper.util.Helper;

public enum IndoorRouteType {
    MISSING, NORMAL, ELEVATOR, STAIRS, ESCALATOR;

    public static final String KEY = "indoor_route_type";

    public static EnumEncodedValue<IndoorRouteType> create() {
        return new EnumEncodedValue<>(KEY, IndoorRouteType.class);
    }

    @Override
    public String toString() {
        return Helper.toLowerCase(super.toString());
    }

    public static IndoorRouteType find(String name) {
        if (name == null || name.isEmpty())
            return MISSING;

        try {
            return IndoorRouteType.valueOf(Helper.toUpperCase(name));
        } catch (IllegalArgumentException ex) {
            return MISSING;
        }
    }

}
