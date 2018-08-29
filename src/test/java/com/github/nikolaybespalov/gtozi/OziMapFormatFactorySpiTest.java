package com.github.nikolaybespalov.gtozi;

import com.google.common.io.Resources;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class OziMapFormatFactorySpiTest {
    GridFormatFactorySpi spi = new OziMapFormatFactorySpi();

    @Test
    public void testCreateFormat() {
        assertNotNull(spi.createFormat());
    }

    @Test
    public void testIsAvailable() {
        assertTrue(spi.isAvailable());
    }

    @Test
    public void testGetImplementationHints() {
        assertEquals(Collections.emptyMap(), spi.getImplementationHints());
    }
}
