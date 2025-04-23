package com.graphhopper.routing;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.EnumEncodedValue;
import com.graphhopper.routing.ev.IndoorRouteType;
import com.graphhopper.routing.util.parsers.TagParser;
import com.graphhopper.storage.IntsRef;

public class IndoorRouteTypeParser implements TagParser {

    private final EnumEncodedValue<IndoorRouteType> indoorRouteTypeEnc;

    public IndoorRouteTypeParser(EnumEncodedValue<IndoorRouteType> indoorRouteTypeEnc) {
        this.indoorRouteTypeEnc = indoorRouteTypeEnc;
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        IndoorRouteType indoorRouteType = IndoorRouteType.NORMAL;

        if (way.hasTag("highway", "elevator") || way.hasTag("indoor", "elevator")) {
            indoorRouteType = IndoorRouteType.ELEVATOR;
        } else if (way.hasTag("highway", "stairs") || way.hasTag("indoor", "stairs")) {
            indoorRouteType = IndoorRouteType.STAIRS;
        } else if (way.hasTag("highway", "escalator") || way.hasTag("indoor", "escalator")) {
            indoorRouteType = IndoorRouteType.ESCALATOR;
        }
        indoorRouteTypeEnc.setEnum(false, edgeId, edgeIntAccess, indoorRouteType);
    }
}
