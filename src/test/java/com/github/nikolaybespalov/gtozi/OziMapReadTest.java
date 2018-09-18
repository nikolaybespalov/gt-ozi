package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.gce.image.WorldImageReader;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.FactoryException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class OziMapReadTest {
    static {
        System.setProperty(GeoTiffReader.OVERRIDE_CRS_SWITCH, "true");
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    @Test
    public void testMer() throws DataSourceException {
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
    public void testTransMer() throws DataSourceException {
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
    public void testLcc2() throws DataSourceException {
        File merMap = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/lcc-2.map");
        File merTif = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/lcc-2.map.tiff");

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
    public void testGauss_() throws IOException, FactoryException {
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
        assertEquals(28405, CRS.lookupEpsgCode(r2.getCoordinateReferenceSystem(), false).intValue());
        assertTrue(r1.getOriginalEnvelope().equals(r2.getOriginalEnvelope(), 0.001, true));
    }

    @Test
    public void testAce() throws DataSourceException {
        File merMap = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/ace.map");
        File merTif = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/ace.map.tiff");

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
    public void testWorldSinus() throws DataSourceException {
        File merMap = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/worldsinus.map");
        File merTif = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/worldsinus.map.tiff");

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
    public void testVandg() throws DataSourceException {
        File merMap = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/vandg.map");
        File merTif = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/vandg.map.tiff");

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
    public void testUtm112() throws DataSourceException, FactoryException {
        File merMap = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/utm11-2.map");
        File merTif = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/utm11-2.map.tiff");

        AbstractGridCoverage2DReader r1 = new GeoTiffReader(merTif);
        AbstractGridCoverage2DReader r2 = new OziMapReader(merMap);

        System.out.println(r1.getCoordinateReferenceSystem().toWKT());
        System.out.println(r2.getCoordinateReferenceSystem().toWKT());

        System.out.println(r1.getOriginalEnvelope());
        System.out.println(r2.getOriginalEnvelope());
        assertTrue(CRS.equalsIgnoreMetadata(r1.getCoordinateReferenceSystem(), r2.getCoordinateReferenceSystem()));
        assertEquals(26711, CRS.lookupEpsgCode(r2.getCoordinateReferenceSystem(), false).intValue());
        assertTrue(r1.getOriginalEnvelope().equals(r2.getOriginalEnvelope(), 0.001, true));
    }

    @Test
    public void test() throws IOException {
        File merMap = ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/utm11-2.map");

        Files.walk(merMap.toPath().getParent()).filter(f -> f.getFileName().endsWith(".map")).forEach(f -> {
            try {
                AbstractGridCoverage2DReader oziMapReader = new OziMapReader(f);
                AbstractGridCoverage2DReader worldImageReader = new WorldImageReader(f);

                assertTrue(CRS.equalsIgnoreMetadata(oziMapReader.getCoordinateReferenceSystem(), worldImageReader.getCoordinateReferenceSystem()));
            } catch (DataSourceException e) {
                fail(e);
            }

        });
    }

    @Test
    public void testBad() {
        assertThrows(DataSourceException.class, () -> new OziMapReader(new File("unknown.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noheader.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noimagefile1.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noimagefile2.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/nodatum1.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/nodatum2.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/nodatumparameters.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/unknowndatum.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noprojection.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noprojectionsetup.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noprojectionsetupparameters.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/nogcps.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/badgcps1.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/badgcps2.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/badgcps3.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/badgcps4.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/nolines.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noutmzone1.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noutmzone2.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noutmzone3.map")));
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