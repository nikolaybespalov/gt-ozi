package com.github.nikolaybespalov.gtoziexplorermap;

import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import javax.imageio.spi.IIORegistry;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.*;

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

        assertTrue(expectedOriginalEnvelope.equals(originalEnvelope, 0.0001, false));
    }

    @Test
    public void testWorld() throws IOException, FactoryException, TransformException {
        IIORegistry.getDefaultInstance().registerServiceProvider(new OzfImageReaderSpi());

        OziExplorerMapReader reader = new OziExplorerMapReader(Paths.get("c:\\Users\\Nikolay Bespalov\\Documents\\github.com\\nikolaybespalov\\gt-oziexplorermap\\src\\test\\resources\\Maps\\World.map"));

        CoordinateReferenceSystem crs = reader.getCoordinateReferenceSystem();

        assertNotNull(crs);
        CoordinateReferenceSystem expectedCrs = CRS.parseWKT("PROJCS[\"unnamed\",\n" +
                "    GEOGCS[\"WGS 84\",\n" +
                "        DATUM[\"WGS_1984\",\n" +
                "            SPHEROID[\"WGS 84\",6378137,298.257223563,\n" +
                "                AUTHORITY[\"EPSG\",\"7030\"]],\n" +
                "            AUTHORITY[\"EPSG\",\"6326\"]],\n" +
                "        PRIMEM[\"Greenwich\",0,\n" +
                "            AUTHORITY[\"EPSG\",\"8901\"]],\n" +
                "        UNIT[\"degree\",0.0174532925199433,\n" +
                "            AUTHORITY[\"EPSG\",\"9122\"]],\n" +
                "        AUTHORITY[\"EPSG\",\"4326\"]],\n" +
                "    PROJECTION[\"Mercator_1SP\"],\n" +
                "    PARAMETER[\"central_meridian\",0],\n" +
                "    PARAMETER[\"scale_factor\",1],\n" +
                "    PARAMETER[\"false_easting\",0],\n" +
                "    PARAMETER[\"false_northing\",0],\n" +
                "    UNIT[\"Meter\",1]]");
        assertTrue(CRS.equalsIgnoreMetadata(expectedCrs, crs));

        GridEnvelope originalGridRange = reader.getOriginalGridRange();

        assertNotNull(originalGridRange);
        assertEquals(new GridEnvelope2D(new Rectangle(0, 0, 2108, 2048)), originalGridRange);

        GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();

        assertNotNull(originalEnvelope);

        GeneralEnvelope expectedOriginalEnvelope = new GeneralEnvelope(expectedCrs);
        expectedOriginalEnvelope.add(new DirectPosition2D(-1.992618885199597E7, -1.790989307498489E7));
        expectedOriginalEnvelope.add(new DirectPosition2D(1.992618885199597E7, 1.832794874451037E7));

        assertTrue(expectedOriginalEnvelope.equals(originalEnvelope, 0.0001, false));
    }
}