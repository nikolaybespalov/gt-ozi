package com.github.nikolaybespalov.gtozi;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.factory.GeoTools;
import org.junit.Test;

import static org.junit.Assert.*;

public class OziMapFormatTest {
    AbstractGridFormat format = new OziMapFormat();

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
        assertFalse(format.accepts(Resources.getResource("Maps/World.ozf2")));
        assertTrue(format.accepts(FileUtils.toFile(Resources.getResource("Maps/World.map"))));
        assertTrue(format.accepts(Resources.getResource("Maps/SeamlessMaps/SeamlessMap.map")));
    }

    @Test
    public void testGetReader() {
        assertNull(format.getReader(null));
        assertNotNull(format.getReader(Resources.getResource("Maps/World.map")));
        assertNull(format.getReader(FileUtils.toFile(Resources.getResource("Maps/SeamlessMaps/SeamlessMap.map"))));
        assertNotNull(format.getReader(FileUtils.toFile(Resources.getResource("Maps/World.map")), GeoTools.getDefaultHints()));
        assertNull(format.getReader(FileUtils.toFile(Resources.getResource("Maps/SeamlessMaps/SeamlessMap.map")), GeoTools.getDefaultHints()));
    }

    @Test
    public void testGetWriter() {
        assertNull(format.getWriter(FileUtils.toFile(Resources.getResource("Maps/World.map"))));
        assertNull(format.getWriter(FileUtils.toFile(Resources.getResource("Maps/SeamlessMaps/SeamlessMap.map"))));
        assertNull(format.getWriter(FileUtils.toFile(Resources.getResource("Maps/World.map")), GeoTools.getDefaultHints()));
        assertNull(format.getWriter(FileUtils.toFile(Resources.getResource("Maps/SeamlessMaps/SeamlessMap.map")), GeoTools.getDefaultHints()));
    }

    @Test
    public void testGetDefaultImageIOWriteParameters() {
        assertNull(format.getDefaultImageIOWriteParameters());
    }


}
