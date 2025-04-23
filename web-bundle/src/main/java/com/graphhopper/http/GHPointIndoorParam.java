package com.graphhopper.http;

import com.graphhopper.util.shapes.GHPoint;
import com.graphhopper.util.shapes.GHPointIndoor;
import io.dropwizard.jersey.params.AbstractParam;


public class GHPointIndoorParam extends AbstractParam<GHPointIndoor> {

    public GHPointIndoorParam(String input) {
        super(input);
    }

    public GHPointIndoorParam(String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected GHPointIndoor parse(String input) {
        if (input == null) {
            return null;
        }
        return GHPointIndoor.parse(input);
    }

}
