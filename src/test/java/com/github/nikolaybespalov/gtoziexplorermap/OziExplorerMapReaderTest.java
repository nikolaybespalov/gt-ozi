package com.github.nikolaybespalov.gtoziexplorermap;

import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.extent.GeographicBoundingBoxImpl;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class OziExplorerMapReaderTest {

    @Test
    public void test1() throws IOException, FactoryException, TransformException {
        OziExplorerMapReader reader = new OziExplorerMapReader(Paths.get("c:\\Users\\Nikolay Bespalov\\Documents\\github.com\\nikolaybespalov\\gt-oziexplorermap\\src\\test\\resources\\Maps\\Demo1.map"));

        //assertEquals(DefaultGeographicCRS.WGS84, reader.getCoordinateReferenceSystem());
        assertEquals(new GridEnvelope2D(new Rectangle(0, 0, 1202, 778)), reader.getOriginalGridRange());
        GeneralEnvelope e = new GeneralEnvelope(new Rectangle2D.Double(152.288056, -26.858472, 152.991764 - 152.288056, -26.44932 - -26.858472));
        e.setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
        assertEquals(e, reader.getOriginalEnvelope());
    }
}