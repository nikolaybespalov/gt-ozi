package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.GridFormatFactorySpi;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class OziMapFormatFactorySpiTest {
    private GridFormatFactorySpi spi = new OziMapFormatFactorySpi();

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
