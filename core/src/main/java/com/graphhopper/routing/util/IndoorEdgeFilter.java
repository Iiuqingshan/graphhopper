package com.graphhopper.routing.util;

import com.graphhopper.util.EdgeIteratorState;

public class IndoorEdgeFilter implements EdgeFilter {

    private final int level;

    public IndoorEdgeFilter(int level) {
        this.level = level;
    }

    @Override
    public boolean accept(EdgeIteratorState edgeState) {


        return false;
    }
}
