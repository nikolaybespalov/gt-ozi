package com.github.nikolaybespalov.gtoziexplorermap;

import org.opengis.referencing.datum.Ellipsoid;

final class OziDatum {
    final Ellipsoid ellipsoid;
    final double dx;
    final double dy;
    final double dz;

    public OziDatum(Ellipsoid ellipsoid, double dx, double dy, double dz) {
        this.ellipsoid = ellipsoid;
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }
}
