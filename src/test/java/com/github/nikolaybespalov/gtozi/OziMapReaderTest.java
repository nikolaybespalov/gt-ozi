package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Before;
import org.junit.Test;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class OziMapReaderTest {
    @Before
    public void setUp() {
        System.setProperty(GeoTiffReader.OVERRIDE_CRS_SWITCH, "True");
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    @Test
    public void testDemo1() throws IOException, FactoryException, TransformException {
        OziMapReader reader = new OziMapReader(ResourceUtils.getResourceAsFile(("Maps/Demo1.map")));

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

        assertTrue(expectedOriginalEnvelope.equals(originalEnvelope, 0.001, false));
    }

    @Test
    public void testWorld() throws IOException, FactoryException, TransformException {
        OziMapReader reader = new OziMapReader(ResourceUtils.getResourceAsFile(("Maps/World.map")));

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

        expectedOriginalEnvelope.add(new DirectPosition2D(20046458.654, -20551246.174));
        expectedOriginalEnvelope.add(new DirectPosition2D(-20046458.654, -20551246.174));
        expectedOriginalEnvelope.add(new DirectPosition2D(20046458.654, 18365954.163));
        expectedOriginalEnvelope.add(new DirectPosition2D(20046458.654, -20551246.174));

        assertTrue(expectedOriginalEnvelope.equals(originalEnvelope, 0.001, false));
    }

    @Test
    public void testSas() throws IOException, FactoryException, TransformException {
        OziMapReader reader = new OziMapReader(ResourceUtils.getResourceAsFile(("Maps/testSAS.map")));

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
        assertEquals(new GridEnvelope2D(new Rectangle(0, 0, 360, 306)), originalGridRange);

        GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();

        assertNotNull(originalEnvelope);

        GeneralEnvelope expectedOriginalEnvelope = new GeneralEnvelope(expectedCrs);
        expectedOriginalEnvelope.add(new DirectPosition2D(3209132.19552484, 8125562.398880421));
        expectedOriginalEnvelope.add(new DirectPosition2D(3209132.19552484 + 440277.2874353309, 8125562.398880421 + 374235.69432003144));

        assertTrue(expectedOriginalEnvelope.equals(originalEnvelope, 1.0, true));
    }

    @Test
    public void testMer() throws IOException, FactoryException, TransformException {
        File merMap = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/mer.map");
        File merTif = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/mer.map.tiff");

        AbstractGridCoverage2DReader r1 = new GeoTiffReader(merTif);
        AbstractGridCoverage2DReader r2 = new OziMapReader(merMap);

        System.out.println(r1.getCoordinateReferenceSystem().toWKT());
        System.out.println(r2.getCoordinateReferenceSystem().toWKT());

        System.out.println(r1.getOriginalEnvelope());
        System.out.println(r2.getOriginalEnvelope());

        assertTrue(CRS.equalsIgnoreMetadata(r1.getCoordinateReferenceSystem(), r2.getCoordinateReferenceSystem()));
        assertTrue(r1.getOriginalEnvelope().equals(r2.getOriginalEnvelope(), 10.0, true));
    }

    @Test
    public void testTransMer() throws IOException, FactoryException, TransformException {
        File merMap = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/trans_mer.map");
        File merTif = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/trans_mer.map.tiff");

        AbstractGridCoverage2DReader r1 = new GeoTiffReader(merTif);
        AbstractGridCoverage2DReader r2 = new OziMapReader(merMap);

        System.out.println(r1.getCoordinateReferenceSystem().toWKT());
        System.out.println(r2.getCoordinateReferenceSystem().toWKT());

        System.out.println(r1.getOriginalEnvelope());
        System.out.println(r2.getOriginalEnvelope());

        assertTrue(CRS.equalsIgnoreMetadata(r1.getCoordinateReferenceSystem(), r2.getCoordinateReferenceSystem()));
        assertTrue(r1.getOriginalEnvelope().equals(r2.getOriginalEnvelope(), 0.001, true));
    }

    @Test
    public void testLcc2() throws IOException, FactoryException, TransformException {
        File merMap = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/lcc-2.map");
        File merTif = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/lcc-2.map.tiff");

        AbstractGridCoverage2DReader r1 = new GeoTiffReader(merTif);
        AbstractGridCoverage2DReader r2 = new OziMapReader(merMap);

        System.out.println(r1.getCoordinateReferenceSystem().toWKT());
        System.out.println(r2.getCoordinateReferenceSystem().toWKT());

        System.out.println(r1.getOriginalEnvelope());
        System.out.println(r2.getOriginalEnvelope());

        assertTrue(CRS.equalsIgnoreMetadata(r1.getCoordinateReferenceSystem(), r2.getCoordinateReferenceSystem()));
        assertTrue(r1.getOriginalEnvelope().equals(r2.getOriginalEnvelope(), 1.0, true));
    }

    @Test
    public void testGauss_() throws IOException, FactoryException, TransformException {
        File merMap = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/gauss_.map");
        File merTif = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/gauss_.map.tiff");

        AbstractGridCoverage2DReader r1 = new GeoTiffReader(merTif);
        AbstractGridCoverage2DReader r2 = new OziMapReader(merMap);

        System.out.println(r1.getCoordinateReferenceSystem().toWKT());
        System.out.println(r2.getCoordinateReferenceSystem().toWKT());

        System.out.println(r1.getOriginalEnvelope());
        System.out.println(r2.getOriginalEnvelope());
        assertTrue(CRS.equalsIgnoreMetadata(r1.getCoordinateReferenceSystem(), r2.getCoordinateReferenceSystem()));
        assertTrue(CRS.equalsIgnoreMetadata(CRS.decode("EPSG:28405", true), r2.getCoordinateReferenceSystem()));
        assertEquals(28405,  CRS.lookupEpsgCode(r2.getCoordinateReferenceSystem(), false).intValue());
        assertTrue(r1.getOriginalEnvelope().equals(r2.getOriginalEnvelope(), 0.001, true));
    }

//    @Test
//    public void testSas2()
//            throws Exception {
//        testMap(
//                new BitmapOziMapRenderer(),
//                testSasPath,
//                "bitmap:map",
//                "EPSG:3395",
//                RendererTestUtils.EXTRACT_SRS,
//                new Rectangle2D.Double(3209132.19552484, 8125562.398880421, 440277.2874353309, 374235.69432003144),
//                RendererTestUtils.IS_NOT_OBJECT_INFO_SUPPORTED);
//    }
}