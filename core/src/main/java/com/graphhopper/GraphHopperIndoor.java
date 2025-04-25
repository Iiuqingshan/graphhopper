package com.graphhopper;

import com.graphhopper.routing.IndoorRouteTypeParser;
import com.graphhopper.routing.ev.*;
import com.graphhopper.routing.util.OSMParsers;
import com.graphhopper.routing.util.parsers.OSMLevelParser;
import com.graphhopper.util.PMap;

import java.util.List;
import java.util.Map;

public class GraphHopperIndoor extends GraphHopper {

    @Override
    protected OSMParsers buildOSMParsers(Map<String, PMap> encodedValuesWithProps, Map<String, ImportUnit> activeImportUnits, Map<String, List<String>> restrictionVehicleTypesByProfile, List<String> ignoredHighways) {
        OSMParsers parsers = super.buildOSMParsers(encodedValuesWithProps, activeImportUnits, restrictionVehicleTypesByProfile, ignoredHighways);
        if (encodingManager.hasEncodedValue(IndoorRouteType.KEY)) {
            EnumEncodedValue<IndoorRouteType> indoorRouteTypeEnc =
                    encodingManager.getEnumEncodedValue(IndoorRouteType.KEY, IndoorRouteType.class);
            parsers.addWayTagParser(new IndoorRouteTypeParser(indoorRouteTypeEnc));
        }
        if (encodingManager.hasEncodedValue(Level.KEY)) {
            StringEncodedValue levelEncodedValue = encodingManager.getStringEncodedValue(Level.KEY);
            parsers.addWayTagParser(new OSMLevelParser(levelEncodedValue));
        }

        return parsers;
    }
}
