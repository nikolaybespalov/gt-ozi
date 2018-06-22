package com.github.nikolaybespalov.gtoziexplorermap;

import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class OziExplorerMapReaderTest {

    @Test
    public void testDemo1() throws IOException, FactoryException, TransformException {
        OziExplorerMapReader reader = new OziExplorerMapReader(Paths.get("c:\\Users\\Nikolay Bespalov\\Documents\\github.com\\nikolaybespalov\\gt-oziexplorermap\\src\\test\\resources\\Maps\\Demo1.map"));

        CoordinateReferenceSystem crs = reader.getCoordinateReferenceSystem();

        assertNotNull(crs);
        assertTrue(CRS.equalsIgnoreMetadata(DefaultGeographicCRS.WGS84, crs));

        GridEnvelope originalGridRange = reader.getOriginalGridRange();

        assertNotNull(originalGridRange);
        assertEquals(new GridEnvelope2D(new Rectangle(0, 0, 1202, 778)), originalGridRange);

        GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();

        assertNotNull(originalEnvelope);

        GeneralEnvelope expectedOriginalEnvelope = new GeneralEnvelope(DefaultGeographicCRS.WGS84);
        expectedOriginalEnvelope.add(new DirectPosition2D(152.288056, -26.449320));
        expectedOriginalEnvelope.add(new DirectPosition2D(152.991764, -26.449320));
        expectedOriginalEnvelope.add(new DirectPosition2D(152.991764, -26.858472));
        expectedOriginalEnvelope.add(new DirectPosition2D(152.288056, -26.858472));

        assertEquals(expectedOriginalEnvelope, originalEnvelope);
    }
}