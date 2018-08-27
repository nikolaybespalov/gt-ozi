package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OziMapFormat extends AbstractGridFormat implements Format {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(OziMapFormat.class);

    @Override
    public AbstractGridCoverage2DReader getReader(Object o) {
        if (o instanceof File) {
            try {
                return new OziMapReader((File) o);
            } catch (DataSourceException e) {
                if (LOGGER.isLoggable(Level.WARNING))
                    LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                return null;
            }
        }

        return null;
    }

    @Override
    public AbstractGridCoverage2DReader getReader(Object o, Hints hints) {
        return null;
    }

    @Override
    public GridCoverageWriter getWriter(Object o) {
        return null;
    }

    @Override
    public boolean accepts(Object o, Hints hints) {
        return false;
    }

    @Override
    public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        return null;
    }

    @Override
    public GridCoverageWriter getWriter(Object o, Hints hints) {
        return null;
    }
}
