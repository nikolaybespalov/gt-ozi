package com.github.nikolaybespalov.gtozi;

import org.geotools.TestData;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.referencing.CRS;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class CrsTest {
//    static {
//        System.setProperty("org.geotools.referencing.forceXY", "true");
//        Hints.putSystemDefault(Hints.FORCE_AXIS_ORDER_HONORING, "http");
//        // setup the referencing tolerance to make it more tolerant to tiny differences
//        // between projections (increases the chance of matching a random prj file content
//        // to an actual EPSG code
//        Hints.putSystemDefault(Hints.COMPARISON_TOLERANCE, 1e-9);
//    }

    // Latitude/Longitude

    @Test
    public void latlonNad27CrsShouldBeEqualsToGdalInfo() throws Exception {
        AbstractGridCoverage2DReader r = new OziMapReader(TestData.file(CrsTest.class, "01-latlon/latlon-nad27.map"));

        assertTrue(CRS.equalsIgnoreMetadata(CRS.parseWKT("GEOGCS[\"NAD27 Central\",\n" +
                "    DATUM[\"NAD27 Central\",\n" +
                "        SPHEROID[\"Clarke 1866\",6378206.4,294.9786982],\n" +
                "        TOWGS84[0,125,194,0,0,0,0]],\n" +
                "    PRIMEM[\"Greenwich\",0],\n" +
                "    UNIT[\"degree\",0.0174532925199433]]"), r.getCoordinateReferenceSystem()));
    }

    @Test
    public void latlonWgs84CrsShouldBeEqualsToGdalInfo() throws Exception {
        AbstractGridCoverage2DReader r = new OziMapReader(TestData.file(CrsTest.class, "01-latlon/latlon-wgs84.map"));

        assertTrue(CRS.equalsIgnoreMetadata(CRS.parseWKT("GEOGCS[\"WGS 84\",\n" +
                "    DATUM[\"WGS_1984\",\n" +
                "        SPHEROID[\"WGS 84\",6378137,298.257223563,\n" +
                "            AUTHORITY[\"EPSG\",\"7030\"]],\n" +
                "        AUTHORITY[\"EPSG\",\"6326\"]],\n" +
                "    PRIMEM[\"Greenwich\",0,\n" +
                "        AUTHORITY[\"EPSG\",\"8901\"]],\n" +
                "    UNIT[\"degree\",0.0174532925199433,\n" +
                "        AUTHORITY[\"EPSG\",\"9122\"]],\n" +
                "    AUTHORITY[\"EPSG\",\"4326\"]]"), r.getCoordinateReferenceSystem()));
    }

    // Mercator

    @Test
    public void mercNad27CrsShouldBeEqualsToGdalInfo() throws Exception {
        AbstractGridCoverage2DReader r = new OziMapReader(TestData.file(CrsTest.class, "02-merc/merc-nad27.map"));

        assertTrue(CRS.equalsIgnoreMetadata(CRS.parseWKT("PROJCS[\"unnamed\",\n" +
                "    GEOGCS[\"NAD27 Central\",\n" +
                "        DATUM[\"NAD27 Central\",\n" +
                "            SPHEROID[\"Clarke 1866\",6378206.4,294.9786982],\n" +
                "            TOWGS84[0,125,194,0,0,0,0]],\n" +
                "        PRIMEM[\"Greenwich\",0],\n" +
                "        UNIT[\"degree\",0.0174532925199433]],\n" +
                "    PROJECTION[\"Mercator_1SP\"],\n" +
                "    PARAMETER[\"central_meridian\",-117.47454],\n" +
                "    PARAMETER[\"scale_factor\",1],\n" +
                "    PARAMETER[\"false_easting\",0],\n" +
                "    PARAMETER[\"false_northing\",0],\n" +
                "    UNIT[\"Meter\",1]]\n").toWKT(), r.getCoordinateReferenceSystem().toWKT()));
    }

    @Test
    public void mercWgs84CrsShouldBeEqualsToGdalInfo() throws Exception {
        AbstractGridCoverage2DReader r = new OziMapReader(TestData.file(CrsTest.class, "02-merc/merc-wgs84.map"));

        assertTrue(CRS.equalsIgnoreMetadata(CRS.parseWKT("PROJCS[\"unnamed\",\n" +
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
                "    UNIT[\"Meter\",1]]"), r.getCoordinateReferenceSystem()));
    }

    // Transverse Mercator

    @Test
    public void gk5Pul42CrsShouldBeEqualsToGdalInfo() throws Exception {
        AbstractGridCoverage2DReader r = new OziMapReader(TestData.file(CrsTest.class, "03-tmerc/gk5-pul42.map"));

        assertTrue(CRS.equalsIgnoreMetadata(CRS.parseWKT("PROJCS[\"unnamed\",\n" +
                "    GEOGCS[\"Pulkovo 1942\",\n" +
                "        DATUM[\"Pulkovo_1942\",\n" +
                "            SPHEROID[\"Krassowsky 1940\",6378245,298.3,\n" +
                "                AUTHORITY[\"EPSG\",\"7024\"]],\n" +
                "            TOWGS84[23.92,-141.27,-80.9,0,0.35,0.82,-0.12],\n" +
                "            AUTHORITY[\"EPSG\",\"6284\"]],\n" +
                "        PRIMEM[\"Greenwich\",0,\n" +
                "            AUTHORITY[\"EPSG\",\"8901\"]],\n" +
                "        UNIT[\"degree\",0.0174532925199433,\n" +
                "            AUTHORITY[\"EPSG\",\"9122\"]],\n" +
                "        AUTHORITY[\"EPSG\",\"4284\"]],\n" +
                "    PROJECTION[\"Transverse_Mercator\"],\n" +
                "    PARAMETER[\"latitude_of_origin\",0],\n" +
                "    PARAMETER[\"central_meridian\",27],\n" +
                "    PARAMETER[\"scale_factor\",1],\n" +
                "    PARAMETER[\"false_easting\",5500000],\n" +
                "    PARAMETER[\"false_northing\",0],\n" +
                "    UNIT[\"Meter\",1]]"), r.getCoordinateReferenceSystem()));
    }

    // (UTM) Universal Transverse Mercator

    @Test
    public void utm11Nad27CrsShouldBeEqualsToGdalInfo() throws Exception {
        AbstractGridCoverage2DReader r = new OziMapReader(TestData.file(CrsTest.class, "04-utm/utm11-nad27.map"));

        assertTrue(CRS.equalsIgnoreMetadata(CRS.parseWKT("PROJCS[\"UTM Zone 11, Northern Hemisphere\",\n" +
                "    GEOGCS[\"NAD27 Central\",\n" +
                "        DATUM[\"NAD27 Central\",\n" +
                "            SPHEROID[\"Clarke 1866\",6378206.4,294.9786982],\n" +
                "            TOWGS84[0,125,194,0,0,0,0]],\n" +
                "        PRIMEM[\"Greenwich\",0],\n" +
                "        UNIT[\"degree\",0.0174532925199433]],\n" +
                "    PROJECTION[\"Transverse_Mercator\"],\n" +
                "    PARAMETER[\"latitude_of_origin\",0],\n" +
                "    PARAMETER[\"central_meridian\",-117],\n" +
                "    PARAMETER[\"scale_factor\",0.9996],\n" +
                "    PARAMETER[\"false_easting\",500000],\n" +
                "    PARAMETER[\"false_northing\",0],\n" +
                "    UNIT[\"Meter\",1]]"), r.getCoordinateReferenceSystem()));
    }

    @Test
    public void utm21Nad83CrsShouldBeEqualsToGdalInfo() throws Exception {
        AbstractGridCoverage2DReader r = new OziMapReader(TestData.file(CrsTest.class, "04-utm/utm21-nad83.map"));

        assertTrue(CRS.equalsIgnoreMetadata(CRS.parseWKT("PROJCS[\"UTM Zone 21, Northern Hemisphere\",\n" +
                "    GEOGCS[\"NAD83\",\n" +
                "        DATUM[\"North_American_Datum_1983\",\n" +
                "            SPHEROID[\"GRS 1980\",6378137,298.257222101,\n" +
                "                AUTHORITY[\"EPSG\",\"7019\"]],\n" +
                "            TOWGS84[0,0,0,0,0,0,0],\n" +
                "            AUTHORITY[\"EPSG\",\"6269\"]],\n" +
                "        PRIMEM[\"Greenwich\",0,\n" +
                "            AUTHORITY[\"EPSG\",\"8901\"]],\n" +
                "        UNIT[\"degree\",0.0174532925199433,\n" +
                "            AUTHORITY[\"EPSG\",\"9122\"]],\n" +
                "        AUTHORITY[\"EPSG\",\"4269\"]],\n" +
                "    PROJECTION[\"Transverse_Mercator\"],\n" +
                "    PARAMETER[\"latitude_of_origin\",0],\n" +
                "    PARAMETER[\"central_meridian\",-57],\n" +
                "    PARAMETER[\"scale_factor\",0.9996],\n" +
                "    PARAMETER[\"false_easting\",500000],\n" +
                "    PARAMETER[\"false_northing\",0],\n" +
                "    UNIT[\"Meter\",1]]"), r.getCoordinateReferenceSystem()));
    }

    @Test
    public void utm17Wgs84CrsShouldBeEqualsToGdalInfo() throws Exception {
        AbstractGridCoverage2DReader r = new OziMapReader(TestData.file(CrsTest.class, "04-utm/utm17-wgs84.map"));

        assertTrue(CRS.equalsIgnoreMetadata(CRS.parseWKT("PROJCS[\"UTM Zone 17, Northern Hemisphere\",\n" +
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
                "    PROJECTION[\"Transverse_Mercator\"],\n" +
                "    PARAMETER[\"latitude_of_origin\",0],\n" +
                "    PARAMETER[\"central_meridian\",-81],\n" +
                "    PARAMETER[\"scale_factor\",0.9996],\n" +
                "    PARAMETER[\"false_easting\",500000],\n" +
                "    PARAMETER[\"false_northing\",0],\n" +
                "    UNIT[\"Meter\",1]]"), r.getCoordinateReferenceSystem()));
    }
}
