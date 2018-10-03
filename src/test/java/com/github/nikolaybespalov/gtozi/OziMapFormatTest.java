package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.factory.GeoTools;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class OziMapFormatTest {
    private final AbstractGridFormat format = new OziMapFormat();

    @Test
    public void testDescription() {
        assertEquals("Ozi", format.getName());
        assertEquals("OziExplorer Map File Format", format.getDescription());
        assertEquals("nikolaybespalov", format.getVendor());
        assertEquals("0.1", format.getVersion());
        assertEquals("https://github.com/nikolaybespalov/gt-ozi", format.getDocURL());
    }

    @Test
    public void testAccepts() {
        assertFalse(format.accepts(null));
        assertFalse(format.accepts("string"));
        assertFalse(format.accepts(TestUtils.getResourceAsUrl("com/github/nikolaybespalov/gtozi/test-data/mer.map.tiff")));
        assertTrue(format.accepts(TestUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/mer.map")));
        assertFalse(format.accepts(TestUtils.getResourceAsUrl("com/github/nikolaybespalov/gtozi/test-data/bad/noimagefile1.map")));
    }

    @Test
    public void testGetReader() {
        assertNull(format.getReader(null));
        assertThrows(IllegalArgumentException.class, () -> format.getReader("string"));
        assertNull(format.getReader(TestUtils.getResourceAsUrl("com/github/nikolaybespalov/gtozi/test-data/mer.map.tiff")));
        assertNotNull(format.getReader(TestUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/mer.map")));
        assertNull(format.getReader(TestUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/bad/noimagefile1.map"), GeoTools.getDefaultHints()));
    }

    @Test
    public void testGetWriter() {
        assertNull(format.getWriter(TestUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/mer.map")));
        assertNull(format.getWriter(TestUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/mer.map"), GeoTools.getDefaultHints()));
    }

    @Test
    public void testGetDefaultImageIOWriteParameters() {
        assertNull(format.getDefaultImageIOWriteParameters());
    }

    @Test
    public void testGetReadParameters() {
        ParameterValueGroup readParameters = format.getReadParameters();

        assertEquals(2, readParameters.values().size());

        assertEquals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName(), readParameters.values().get(0).getDescriptor().getName());
        assertEquals(AbstractGridFormat.SUGGESTED_TILE_SIZE.getName(), readParameters.values().get(1).getDescriptor().getName());
    }
}
