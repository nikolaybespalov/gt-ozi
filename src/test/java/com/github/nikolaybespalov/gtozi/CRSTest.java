package com.github.nikolaybespalov.gtozi;

import org.geotools.TestData;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.referencing.CRS;
import org.junit.Assert;
import org.junit.Test;

public class CRSTest {
    @Test
    public void merMapFileShouldBeInMercatorProjectionOnNad27CentralDatum() throws Exception {
        AbstractGridCoverage2DReader r = new OziMapReader(TestData.file(CRSTest.class, "mer.map"));

        Assert.assertEquals(CRS.parseWKT("PROJCS[\"unnamed\"," +
                "  GEOGCS[\"NAD27 Central\"," +
                "    DATUM[\"NAD27 Central\"," +
                "      SPHEROID[\"Clarke 1866\", 6378206.4, 294.9786982]," +
                "      TOWGS84[0.0, 125.0, 194.0, 0.0, 0.0, 0.0, 0.0]]," +
                "    PRIMEM[\"Greenwich\", 0.0]," +
                "    UNIT[\"degree\", 0.017453292519943295]," +
                "    AXIS[\"Geodetic longitude\", EAST]," +
                "    AXIS[\"Geodetic latitude\", NORTH]]," +
                "  PROJECTION[\"Mercator_1SP\"]," +
                "  PARAMETER[\"latitude_of_origin\", 0.0]," +
                "  PARAMETER[\"central_meridian\", -90.0]," +
                "  PARAMETER[\"scale_factor\", 0.829916312080482]," +
                "  PARAMETER[\"false_easting\", 0.001]," +
                "  PARAMETER[\"false_northing\", 0.002]," +
                "  UNIT[\"m\", 1.0]," +
                "  AXIS[\"Easting\", EAST]," +
                "  AXIS[\"Northing\", NORTH]]").toWKT(), r.getCoordinateReferenceSystem().toWKT());
    }

    @Test
    public void transMerMapFileShouldBeInTransverseMercatorProjectionOnNad27CentralDatum() throws Exception {
        AbstractGridCoverage2DReader r = new OziMapReader(TestData.file(CRSTest.class, "trans_mer.map"));

        Assert.assertEquals(CRS.parseWKT("PROJCS[\"unnamed\"," +
                "  GEOGCS[\"NAD27 Central\"," +
                "    DATUM[\"NAD27 Central\"," +
                "      SPHEROID[\"Clarke 1866\", 6378206.4, 294.9786982]," +
                "      TOWGS84[0.0, 125.0, 194.0, 0.0, 0.0, 0.0, 0.0]]," +
                "    PRIMEM[\"Greenwich\", 0.0]," +
                "    UNIT[\"degree\", 0.017453292519943295]," +
                "    AXIS[\"Geodetic longitude\", EAST]," +
                "    AXIS[\"Geodetic latitude\", NORTH]]," +
                "  PROJECTION[\"Transverse_Mercator\"]," +
                "  PARAMETER[\"central_meridian\", -90.0]," +
                "  PARAMETER[\"latitude_of_origin\", 30.0]," +
                "  PARAMETER[\"scale_factor\", 0.99999]," +
                "  PARAMETER[\"false_easting\", 0.001]," +
                "  PARAMETER[\"false_northing\", 0.002]," +
                "  UNIT[\"m\", 1.0]," +
                "  AXIS[\"Easting\", EAST]," +
                "  AXIS[\"Northing\", NORTH]]").toWKT(), r.getCoordinateReferenceSystem().toWKT());
    }
}
