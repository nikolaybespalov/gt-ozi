package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.data.DataSourceException;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.parameter.DefaultParameterDescriptorGroup;
import org.geotools.parameter.ParameterGroup;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.parameter.GeneralParameterDescriptor;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.geotools.util.logging.Logging.getLogger;

@SuppressWarnings("WeakerAccess")
public final class OziMapFormat extends AbstractGridFormat implements Format {
    private static final Logger LOGGER = getLogger(OziMapFormat.class);

    public OziMapFormat() {
        mInfo = new HashMap<>();
        mInfo.put("name", "Ozi");
        mInfo.put("description", "OziExplorer Map File Format");
        mInfo.put("vendor", "nikolaybespalov");
        mInfo.put("version", "0.1.17");
        mInfo.put("docURL", "https://github.com/nikolaybespalov/gt-ozi");

        readParameters = new ParameterGroup(new DefaultParameterDescriptorGroup(mInfo,
                new GeneralParameterDescriptor[]{
                        READ_GRIDGEOMETRY2D,
                        SUGGESTED_TILE_SIZE
                }));
    }

    @Override
    public boolean accepts(Object source, Hints hints) {
        AbstractGridCoverage2DReader reader = getReader(source, hints);

        if (reader == null) {
            return false;
        }

        reader.dispose();

        return true;
    }

    @Override
    public AbstractGridCoverage2DReader getReader(Object o) {
        return getReader(o, GeoTools.getDefaultHints());
    }

    @Override
    public AbstractGridCoverage2DReader getReader(Object source, Hints hints) {
        if (source == null) {
            LOGGER.severe("source should not be null");
            return null;
        }

        try {
            return new OziMapReader(source);
        } catch (DataSourceException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }

        return null;
    }

    @Override
    public GridCoverageWriter getWriter(Object o) {
        return getWriter(o, GeoTools.getDefaultHints());
    }

    @Override
    public GridCoverageWriter getWriter(Object o, Hints hints) {
        return null;
    }

    @Override
    public GeoToolsWriteParams getDefaultImageIOWriteParameters() {
        return null;
    }
}
