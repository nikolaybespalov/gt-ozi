package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class GridFormatFinderTest {
    @Test
    public void testFindFormat() {
        AbstractGridFormat format = GridFormatFinder.findFormat(ResourceUtils.getResourceAsFile("Maps/Demo1.map"));

        assertNotNull(format);
        assertNotEquals(format.getName(), "Unknown Format");
    }
}
