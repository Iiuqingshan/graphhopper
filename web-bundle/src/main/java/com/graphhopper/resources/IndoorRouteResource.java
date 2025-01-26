/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.resources;

import com.graphhopper.*;
import com.graphhopper.http.*;
import com.graphhopper.jackson.MultiException;
import com.graphhopper.jackson.ResponsePathSerializer;
import com.graphhopper.util.*;
import com.graphhopper.util.shapes.GHPoint;
import io.dropwizard.jersey.params.AbstractParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Arrays;
import java.util.List;

import static com.graphhopper.resources.RouteResource.*;
import static com.graphhopper.util.Parameters.Details.PATH_DETAILS;
import static com.graphhopper.util.Parameters.Routing.*;
import static com.graphhopper.util.Parameters.Routing.SNAP_PREVENTION;
import static java.util.stream.Collectors.toList;

/**
 * 室内导航路线api
 */
@Path("/indoor-route")
public class IndoorRouteResource {

    private static final Logger logger = LoggerFactory.getLogger(RouteResource.class);

    private final GraphHopperConfig config;
    private final GraphHopper graphHopper;
    private final ProfileResolver profileResolver;
    private final GHRequestTransformer ghRequestTransformer;
    private final Boolean hasElevation;
    private final String osmDate;
    private final List<String> snapPreventionsDefault;

    @Inject
    public IndoorRouteResource(GraphHopperConfig config, GraphHopper graphHopper, ProfileResolver profileResolver, GHRequestTransformer ghRequestTransformer, @Named("hasElevation") Boolean hasElevation) {
        this.config = config;
        this.graphHopper = graphHopper;
        this.profileResolver = profileResolver;
        this.ghRequestTransformer = ghRequestTransformer;
        this.hasElevation = hasElevation;
        this.osmDate = graphHopper.getProperties().getAll().get("datareader.data.date");
        this.snapPreventionsDefault = Arrays.stream(config.getString("routing.snap_preventions_default", "")
                .split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, "application/gpx+xml"})
    public Response doGet(
            @Context HttpServletRequest httpReq,
            @Context UriInfo uriInfo,
            @QueryParam(WAY_POINT_MAX_DISTANCE) @DefaultValue("0.5") double minPathPrecision,
            @QueryParam(ELEVATION_WAY_POINT_MAX_DISTANCE) Double minPathElevationPrecision,
            @QueryParam("point") @NotNull List<GHPointParam> pointParams,
            @QueryParam("type") @DefaultValue("json") String type,
            @QueryParam(INSTRUCTIONS) @DefaultValue("true") boolean instructions,
            @QueryParam(CALC_POINTS) @DefaultValue("true") boolean calcPoints,
            @QueryParam("elevation") @DefaultValue("false") boolean enableElevation,
            @QueryParam("points_encoded") @DefaultValue("false") boolean pointsEncoded,
            @QueryParam("points_encoded_multiplier") @DefaultValue("1e5") double pointsEncodedMultiplier,
            @QueryParam("profile") @DefaultValue("foot") String profileName,
            @QueryParam(ALGORITHM) @DefaultValue(Parameters.Algorithms.ALT_ROUTE) String algoStr,
            @QueryParam("locale") @DefaultValue("en") String localeStr,
            @QueryParam(POINT_HINT) List<String> pointHints,
            @QueryParam(CURBSIDE) List<String> curbsides,
            @QueryParam(SNAP_PREVENTION) List<String> snapPreventions,
            @QueryParam(PATH_DETAILS) List<String> pathDetails,
            @QueryParam("heading") @NotNull List<Double> headings,
            @QueryParam("gpx.route") @DefaultValue("true") boolean withRoute /* default to false for the route part in next API version, see #437 */,
            @QueryParam("gpx.track") @DefaultValue("true") boolean withTrack,
            @QueryParam("gpx.waypoints") @DefaultValue("false") boolean withWayPoints,
            @QueryParam("gpx.trackname") @DefaultValue("GraphHopper Track") String trackName,
            @QueryParam("gpx.millis") String timeString) {
        StopWatch sw = new StopWatch().start();
        List<GHPoint> points = pointParams.stream().map(AbstractParam::get).collect(toList());
        boolean writeGPX = "gpx".equalsIgnoreCase(type);
        instructions = writeGPX || instructions;
        if (enableElevation && !hasElevation)
            throw new IllegalArgumentException("Elevation not supported!");

        GHRequest request = new GHRequest();
        initHints(request.getHints(), uriInfo.getQueryParameters());

        if (minPathElevationPrecision != null)
            request.getHints().putObject(ELEVATION_WAY_POINT_MAX_DISTANCE, minPathElevationPrecision);

        request.setPoints(points).
                setProfile(profileName).
                setAlgorithm(algoStr).
                setLocale(localeStr).
                setHeadings(headings).
                setPointHints(pointHints).
                setCurbsides(curbsides).
                setPathDetails(pathDetails).
                getHints().
                putObject(CALC_POINTS, calcPoints).
                putObject(INSTRUCTIONS, instructions).
                putObject(WAY_POINT_MAX_DISTANCE, minPathPrecision);

        if (uriInfo.getQueryParameters().containsKey(SNAP_PREVENTION)) {
            if (snapPreventions.size() == 1 && snapPreventions.contains(""))
                request.setSnapPreventions(List.of());
            else
                request.setSnapPreventions(snapPreventions);
        } else {
            request.setSnapPreventions(snapPreventionsDefault);
        }

        request = ghRequestTransformer.transformRequest(request);

        PMap profileResolverHints = new PMap(request.getHints());
        profileResolverHints.putObject("profile", profileName);
        profileResolverHints.putObject("has_curbsides", !curbsides.isEmpty());
        profileName = profileResolver.resolveProfile(profileResolverHints);
        removeLegacyParameters(request.getHints());
        request.setProfile(profileName);

        GHResponse ghResponse = graphHopper.indoorRoute(request);

        double took = sw.stop().getMillisDouble();
        String logStr = (httpReq.getRemoteAddr() + " " + httpReq.getLocale() + " " + httpReq.getHeader("User-Agent")) + " " + points + ", took: " + String.format("%.1f", took) + "ms, algo: " + algoStr + ", profile: " + profileName;

        if (ghResponse.hasErrors()) {
            logger.info(logStr + " " + ghResponse);
            return Response.status(Response.Status.BAD_REQUEST).
                    entity(new MultiException(ghResponse.getErrors())).
                    type(writeGPX ? "application/gpx+xml" : MediaType.APPLICATION_JSON).
                    build();
        } else {
            logger.info(logStr + ", alternatives: " + ghResponse.getAll().size()
                    + ", distance0: " + ghResponse.getBest().getDistance()
                    + ", weight0: " + ghResponse.getBest().getRouteWeight()
                    + ", time0: " + Math.round(ghResponse.getBest().getTime() / 60000f) + "min"
                    + ", points0: " + ghResponse.getBest().getPoints().size()
                    + ", debugInfo: " + ghResponse.getDebugInfo());
            return Response.ok(ResponsePathSerializer.indoorJsonObject(ghResponse, new ResponsePathSerializer.Info(config.getCopyrights(), Math.round(took), osmDate), instructions, calcPoints, enableElevation, pointsEncoded, pointsEncodedMultiplier)).
                    header("X-GH-Took", "" + Math.round(took)).
                    type(MediaType.APPLICATION_JSON).
                    build();
        }
    }

}
