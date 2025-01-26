package com.graphhopper.util;

import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.impl.PackedCoordinateSequence;

import java.util.Arrays;

public class IndoorPackedCoordinateSequence extends PackedCoordinateSequence {

    private static final long serialVersionUID = 5777450686367912719L;
    double[] coords;

    public IndoorPackedCoordinateSequence(double[] coords, int dimension, int measures) {
        super(dimension, measures);
        if (coords.length % dimension != 0) {
            throw new IllegalArgumentException("Packed array does not contain "
                    + "an integral number of coordinates");
        }
        this.coords = coords;
    }

    public IndoorPackedCoordinateSequence(Coordinate[] coordinates, int dimension) {
        this(coordinates, dimension, Math.max(0, dimension - 3));
    }

    public IndoorPackedCoordinateSequence(Coordinate[] coordinates, int dimension, int measures) {
        super(dimension, measures);
        if (coordinates == null) {
            coordinates = new Coordinate[0];
        }

        this.coords = new double[coordinates.length * this.dimension];

        for (int i = 0; i < coordinates.length; ++i) {
            int offset = i * dimension;
            this.coords[offset] = coordinates[i].x;
            this.coords[offset + 1] = coordinates[i].y;
            this.coords[offset + 2] = coordinates[i].getOrdinate(2);

            if (dimension >= 4) {
                this.coords[offset + 3] = coordinates[i].getOrdinate(3);
            }
        }

    }

    @Override
    public double getOrdinate(int index, int ordinate) {
        return coords[index * dimension + ordinate];
    }

    @Override
    public int size() {
        return coords.length / dimension;
    }

    @Override
    public Coordinate getCoordinateInternal(int i) {
        double x = coords[i * dimension];
        double y = coords[i * dimension + 1];
        if (dimension == 2 && measures == 0) {
            return new CoordinateXY(x, y);
        } else if (dimension == 3 && measures == 0) {
            double z = coords[i * dimension + 2];
            return new Coordinate(x, y, z);
        } else if (dimension == 3 && measures == 1) {
            double m = coords[i * dimension + 2];
            return new CoordinateXYM(x, y, m);
        } else if (dimension == 4) {
            double z = coords[i * dimension + 2];
            double m = coords[i * dimension + 3];
            return new CoordinateXYZM(x, y, z, m);
        }
        return new Coordinate(x, y);
    }

    @Override
    public Object clone() {
        return copy();
    }

    @Override
    public IndoorPackedCoordinateSequence copy() {
        double[] clone = Arrays.copyOf(coords, coords.length);
        return new IndoorPackedCoordinateSequence(clone, dimension, measures);
    }

    @Override
    public void setOrdinate(int index, int ordinate, double value) {
        coordRef = null;
        coords[index * dimension + ordinate] = value;
    }

    @Override
    public Envelope expandEnvelope(Envelope env) {
        for (int i = 0; i < coords.length; i += dimension) {
            // added to make static code analysis happy
            if (i + 1 < coords.length) {
                env.expandToInclude(coords[i], coords[i + 1]);
            }
        }
        return env;
    }

}
