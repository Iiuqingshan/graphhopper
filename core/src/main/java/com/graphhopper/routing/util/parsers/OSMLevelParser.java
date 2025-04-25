package com.graphhopper.routing.util.parsers;

import com.graphhopper.reader.ReaderWay;
import com.graphhopper.routing.ev.EdgeIntAccess;
import com.graphhopper.routing.ev.IntEncodedValue;
import com.graphhopper.routing.ev.StringEncodedValue;
import com.graphhopper.storage.IntsRef;

public class OSMLevelParser implements TagParser {

    IntEncodedValue levelEnc;

    public OSMLevelParser(StringEncodedValue levelEnc) {
        this.levelEnc = levelEnc;
    }

    @Override
    public void handleWayTags(int edgeId, EdgeIntAccess edgeIntAccess, ReaderWay way, IntsRef relationFlags) {
        String level = way.getTag("level");
        if (level == null) {
            return;
        }
        ((StringEncodedValue) levelEnc).setString(false, edgeId, edgeIntAccess, level);
    }
}
