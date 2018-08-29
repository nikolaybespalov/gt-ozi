package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;
import org.geotools.factory.GeoTools;
import org.geotools.factory.Hints;
import org.geotools.util.URLs;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverageWriter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.geotools.util.logging.Logging.getLogger;

@SuppressWarnings("WeakerAccess")
public class OziMapFormat extends AbstractGridFormat implements Format {
    private static final Logger LOGGER = getLogger(OziMapFormat.class);

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
    public boolean accepts(Object source, Hints hints) {
        File inputFile = inputAsFile(source);

        if (inputFile == null) {
            return false;
        }

        return inputFile.getName().endsWith(".map");
    }

    @Override
    public AbstractGridCoverage2DReader getReader(Object o) {
        return getReader(o, GeoTools.getDefaultHints());
    }

    @Override
    public AbstractGridCoverage2DReader getReader(Object source, Hints hints) {
        File inputFile = inputAsFile(source);

        if (inputFile == null) {
            return null;
        }

        try {
            return new OziMapReader(inputFile);
        } catch (IOException | FactoryException | TransformException e) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }

            return null;
        }
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

    private static File inputAsFile(Object input) {
        if (input == null) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Input cannot be null");
            }

            return null;
        }

        File inputFile = null;
        if (input instanceof File) {
            inputFile = (File) input;
        } else if (input instanceof URL && (((URL) input).getProtocol().equals("file"))) {
            inputFile = URLs.urlToFile((URL) input);
        }

        if (inputFile == null) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "Unknown input: " + input.getClass());
            }

            return null;
        }

        return inputFile;
    }
}
