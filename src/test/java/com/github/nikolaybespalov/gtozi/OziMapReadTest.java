package com.github.nikolaybespalov.gtozi;

import org.geotools.data.DataSourceException;
import org.geotools.gce.geotiff.GeoTiffReader;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.FactoryException;

import java.io.File;
import java.io.IOException;

import static com.github.nikolaybespalov.gtozi.TestUtils.assertReaderEquals;
import static com.github.nikolaybespalov.gtozi.TestUtils.getResourceAsFile;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OziMapReadTest {
    static {
        System.setProperty(GeoTiffReader.OVERRIDE_CRS_SWITCH, "true");
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    private static final String TEST_DATA_ROOT_PATH = "com/github/nikolaybespalov/gtozi/test-data/";

    @Test
    public void testMercatorProjection() throws DataSourceException {
        assertReaderEquals(
                new GeoTiffReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "mer.map.tiff")),
                new OziMapReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "mer.map")));
    }

    @Test
    public void testTransverseMercatorProjection() throws DataSourceException {
        assertReaderEquals(
                new GeoTiffReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "trans_mer.map.tiff")),
                new OziMapReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "trans_mer.map")));
    }

    @Test
    public void testLcc2() throws DataSourceException {
        assertReaderEquals(
                new GeoTiffReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "lcc-2.map.tiff")),
                new OziMapReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "lcc-2.map")));
    }

    @Test
    public void testGauss_() throws IOException, FactoryException {
        OziMapReader r2;

        assertReaderEquals(
                new GeoTiffReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "gauss_.map.tiff")),
                r2 = new OziMapReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "gauss_.map")));

        assertEquals(28405, CRS.lookupEpsgCode(r2.getCoordinateReferenceSystem(), false).intValue());

    }

    @Test
    public void testAce() throws DataSourceException {
        assertReaderEquals(
                new GeoTiffReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "ace.map.tiff")),
                new OziMapReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "ace.map")));
    }

    @Test
    public void testWorldSinus() throws DataSourceException {
        assertReaderEquals(
                new GeoTiffReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "worldsinus.map.tiff")),
                new OziMapReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "worldsinus.map")));
    }

    @Test
    public void testVanDerGrintenProjection() throws DataSourceException {
        assertReaderEquals(
                new GeoTiffReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "vandg.map.tiff")),
                new OziMapReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "vandg.map")));
    }

    @Test
    public void testUtm112() throws DataSourceException, FactoryException {
        OziMapReader r2;

        assertReaderEquals(
                new GeoTiffReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "utm11-2.map.tiff")),
                r2 = new OziMapReader(getResourceAsFile(TEST_DATA_ROOT_PATH + "utm11-2.map")));

        assertEquals(26711, CRS.lookupEpsgCode(r2.getCoordinateReferenceSystem(), false).intValue());
    }

    @Test
    public void testBad() {
        assertThrows(DataSourceException.class, () -> new OziMapReader(new File("unknown.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noheader.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noimagefile1.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noimagefile2.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/nodatum1.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/nodatum2.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/nodatumparameters.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/unknowndatum.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noprojection.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noprojectionsetup.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noprojectionsetupparameters.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/nogcps.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/badgcps1.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/badgcps2.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/badgcps3.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/badgcps4.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/nolines.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noutmzone1.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noutmzone2.map")));
        assertThrows(DataSourceException.class, () -> new OziMapReader(getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noutmzone3.map")));
    }
}