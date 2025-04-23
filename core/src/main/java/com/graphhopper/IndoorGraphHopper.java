package com.graphhopper;

import com.graphhopper.routing.IndoorRouteTypeParser;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.ImportUnit;
import com.graphhopper.routing.ev.IndoorRouteType;
import com.graphhopper.routing.util.OSMParsers;
import com.graphhopper.util.PMap;

import java.util.List;
import java.util.Map;

public class IndoorGraphHopper extends GraphHopper {

    @Override
    protected OSMParsers buildOSMParsers(Map<String, PMap> encodedValuesWithProps, Map<String, ImportUnit> activeImportUnits, Map<String, List<String>> restrictionVehicleTypesByProfile, List<String> ignoredHighways) {
        OSMParsers parsers = super.buildOSMParsers(encodedValuesWithProps, activeImportUnits, restrictionVehicleTypesByProfile, ignoredHighways);
        if (encodingManager.hasEncodedValue(IndoorRouteType.KEY)) {
            EnumEncodedValue<IndoorRouteType> indoorRouteTypeEnc =
                    encodingManager.getEnumEncodedValue(IndoorRouteType.KEY, IndoorRouteType.class);
            parsers.addWayTagParser(new IndoorRouteTypeParser(indoorRouteTypeEnc));
        }

        return parsers;
    }
}
