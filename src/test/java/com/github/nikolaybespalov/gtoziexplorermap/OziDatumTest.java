package com.github.nikolaybespalov.gtoziexplorermap;

import org.junit.Assert;
import org.junit.Test;

public class OziDatumTest {

    @Test
    public void testCustom() {
        OziDatum datum = new OziDatum("CUSTOM", 1209, OziEllipsoid.WGS_84, 1, 2, 3);

        Assert.assertNotNull(datum);
        Assert.assertEquals("CUSTOM", datum.getName());
        Assert.assertEquals(1209, datum.getEpsgCode());
        Assert.assertEquals(OziEllipsoid.WGS_84, datum.getEllipsoid());
        Assert.assertEquals(1, datum.getDeltaX());
        Assert.assertEquals(2, datum.getDeltaY());
        Assert.assertEquals(3, datum.getDeltaZ());
    }

    @Test
    public void testWgs84() {
        Assert.assertNotNull(OziDatum.WGS_84);
        Assert.assertEquals("WGS 84", OziDatum.WGS_84.getName());
        Assert.assertEquals(4326, OziDatum.WGS_84.getEpsgCode());
        Assert.assertEquals(OziEllipsoid.WGS_84, OziDatum.WGS_84.getEllipsoid());
        Assert.assertEquals(0, OziDatum.WGS_84.getDeltaX());
        Assert.assertEquals(0, OziDatum.WGS_84.getDeltaY());
        Assert.assertEquals(0, OziDatum.WGS_84.getDeltaZ());
    }
}
