package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;

import java.awt.*;
import java.util.Map;

public class OziMapFormatFactorySpi implements GridFormatFactorySpi {
    @Override
    public AbstractGridFormat createFormat() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Map<RenderingHints.Key, ?> getImplementationHints() {
        return null;
    }
}
