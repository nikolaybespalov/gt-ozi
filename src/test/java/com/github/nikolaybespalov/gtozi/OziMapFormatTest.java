package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.factory.GeoTools;
import org.junit.Test;

import static org.junit.Assert.*;

public class OziMapFormatTest {
    private AbstractGridFormat format = new OziMapFormat();

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
        assertFalse(format.accepts(ResourceUtils.getResourceAsUrl("Maps/World.ozf2")));
        assertTrue(format.accepts(ResourceUtils.getResourceAsFile(("Maps/World.map"))));
        assertTrue(format.accepts(ResourceUtils.getResourceAsUrl("Maps/SeamlessMaps/SeamlessMap.map")));
    }

    @Test
    public void testGetReader() {
        assertNull(format.getReader(null));
        assertNull(format.getReader("string"));
        assertNotNull(format.getReader(ResourceUtils.getResourceAsUrl("Maps/World.map")));
        assertNull(format.getReader(ResourceUtils.getResourceAsFile(("Maps/SeamlessMaps/SeamlessMap.map"))));
        assertNotNull(format.getReader(ResourceUtils.getResourceAsFile(("Maps/World.map")), GeoTools.getDefaultHints()));
        assertNull(format.getReader(ResourceUtils.getResourceAsFile(("Maps/SeamlessMaps/SeamlessMap.map")), GeoTools.getDefaultHints()));
    }

    @Test
    public void testGetWriter() {
        assertNull(format.getWriter(ResourceUtils.getResourceAsFile(("Maps/World.map"))));
        assertNull(format.getWriter(ResourceUtils.getResourceAsFile(("Maps/SeamlessMaps/SeamlessMap.map"))));
        assertNull(format.getWriter(ResourceUtils.getResourceAsFile(("Maps/World.map")), GeoTools.getDefaultHints()));
        assertNull(format.getWriter(ResourceUtils.getResourceAsFile(("Maps/SeamlessMaps/SeamlessMap.map")), GeoTools.getDefaultHints()));
    }

    @Test
    public void testGetDefaultImageIOWriteParameters() {
        assertNull(format.getDefaultImageIOWriteParameters());
    }


}
