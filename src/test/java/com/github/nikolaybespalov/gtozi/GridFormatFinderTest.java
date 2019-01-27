package com.github.nikolaybespalov.gtozi;

import org.geotools.TestData;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class GridFormatFinderTest {
    @Test
    public void testFindFormat() throws Exception {
        AbstractGridFormat format = GridFormatFinder.findFormat(TestData.file(CrsTest.class, "02-merc/merc-nad27.map"));

        assertNotNull(format);
        assertNotEquals(format.getName(), "Unknown Format");
    }
}
