package com.graphhopper.routing.util;

import com.graphhopper.routing.ev.EncodedValueLookup;
import com.graphhopper.routing.ev.Level;
import com.graphhopper.routing.ev.StringEncodedValue;
import com.graphhopper.util.EdgeIteratorState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IndoorEdgeFilter implements EdgeFilter {
    private final StringEncodedValue levelEnc;
    private final int currentLevel;

    public IndoorEdgeFilter(EncodedValueLookup lookup, int level) {
        this.currentLevel = level;
        this.levelEnc = lookup.getStringEncodedValue(Level.KEY);
    }

    @Override
    public boolean accept(EdgeIteratorState edgeState) {
        String edgeLevel = edgeState.get(levelEnc);
        if (edgeLevel == null) {
            return false;
        }

        // 先按分号拆分
        String[] parts = edgeLevel.split(";");
        for (String part : parts) {
            if (part.matches("-?\\d+-\\d+")) {
                // 是区间，如 -5-4 或 1-3
                Matcher m = Pattern.compile("(-?\\d+)(-)(\\d+)").matcher(part);
                if (m.find()) {
                    int start = Integer.parseInt(m.group(1));
                    int end = Integer.parseInt(m.group(3));
                    if (start <= currentLevel && currentLevel <= end) {
                        return true;
                    }
                }
            } else if (part.matches("-?\\d+")) {
                // 是单个数字
                if (Integer.parseInt(part) == currentLevel) {
                    return true;
                }
            }
        }

        return false;
    }
}
