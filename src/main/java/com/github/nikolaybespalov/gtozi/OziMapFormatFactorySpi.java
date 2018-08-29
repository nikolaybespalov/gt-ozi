package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;

import java.awt.*;
import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unused")
public class OziMapFormatFactorySpi implements GridFormatFactorySpi {
    @Override
    public AbstractGridFormat createFormat() {
        return new OziMapFormat();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public Map<RenderingHints.Key, ?> getImplementationHints() {
        return Collections.emptyMap();
    }
}
