package com.github.nikolaybespalov.gtoziexplorermap;

import org.junit.Assert;
import org.junit.Test;

public class OziDatumTest {

    @Test
    public void testGet() {
        OziDatum datum = OziDatum.get("WGS 84");

        Assert.assertNotNull(datum);
        Assert.assertEquals("WGS 84", datum.getName());
    }

    @Test
    public void testGetUnknown() {
        OziDatum datum = OziDatum.get("Unknown");

        Assert.assertNull(datum);
    }

    @Test
    public void testWgs84() {
        OziDatum datum = OziDatum.get("WGS 84");

        Assert.assertNotNull(datum);
        Assert.assertEquals("WGS 84", datum.getName());
        Assert.assertEquals(4326, datum.getEpsgCode());
        Assert.assertEquals(OziEllipsoid.WGS_84, datum.getEllipsoid());
        Assert.assertEquals(0, datum.getDeltaX());
        Assert.assertEquals(0, datum.getDeltaY());
        Assert.assertEquals(0, datum.getDeltaZ());
    }
}