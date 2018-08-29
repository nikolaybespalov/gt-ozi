package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.Hints;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
public class OziMapFormat extends AbstractGridFormat implements Format {
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(OziMapFormat.class);

    public OziMapFormat() {
        writeParameters = null;
        mInfo = new HashMap<>();
        mInfo.put("name", "Ozi");
        mInfo.put("description", "OziExplorer Map File Format");
        mInfo.put("vendor", "nikolaybespalov");
        mInfo.put("version", "0.1");
        mInfo.put("docURL", "https://github.com/nikolaybespalov/gt-ozi");
    }

    @Override
    public AbstractGridCoverage2DReader getReader(Object o) {
        if (o instanceof File) {
            try {
                return new OziMapReader(o);
            } catch (IOException | FactoryException | TransformException e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);

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
        return true;
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
