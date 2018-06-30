package com.github.nikolaybespalov.gtozi;

import org.geotools.geometry.DirectPosition2D;

import java.awt.*;

public final class OziCalibrationPoint {
    private final Point pixelLine;
    private final DirectPosition2D xy;

    public OziCalibrationPoint(Point pixelLine, DirectPosition2D xy) {
        this.pixelLine = pixelLine;
        this.xy = xy;
    }

    public Point getPixelLine() {
        return pixelLine;
    }

    public DirectPosition2D getXy() {
        return xy;
    }
}
