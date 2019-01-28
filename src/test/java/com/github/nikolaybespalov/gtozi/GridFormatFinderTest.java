package com.github.nikolaybespalov.gtozi;

import org.geotools.TestData;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class GridFormatFinderTest {
    @Test
    public void gridFormatFinderFindFormatShouldReturnOziMapFormat() throws Exception {
        AbstractGridFormat format = GridFormatFinder.findFormat(TestData.file(GridFormatFinderTest.class, "02-merc/merc-nad27.map"));

        assertTrue(format instanceof OziMapFormat);
    }
}
