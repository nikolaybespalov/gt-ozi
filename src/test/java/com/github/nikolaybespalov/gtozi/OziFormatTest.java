package com.github.nikolaybespalov.gtozi;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.junit.Test;

import static org.junit.Assert.*;

public class OziFormatTest {
    AbstractGridFormat format = new OziMapFormat();

    @Test
    public void testFindFormat() {
        AbstractGridFormat format = GridFormatFinder.findFormat(Resources.getResource("Maps/Demo1.map"));

        assertNotNull(format);
        assertNotEquals(format.getName(), "Unknown Format");
    }

    @Test
    public void testFormatDescription() {
        AbstractGridFormat format = GridFormatFinder.findFormat(Resources.getResource("Maps/Demo1.map"));

        assertEquals("Ozi", format.getName());
        assertEquals("OziExplorer Map File Format", format.getDescription());
        assertEquals("nikolaybespalov", format.getVendor());
        assertEquals("0.1", format.getVersion());
        assertEquals("https://github.com/nikolaybespalov/gt-ozi", format.getDocURL());
    }

    @Test
    public void testAccepts() {
        AbstractGridFormat format = new OziMapFormat();

        assertTrue(format.accepts(Resources.getResource("Maps/Demo1.map")));
        assertTrue(format.accepts(Resources.getResource("Maps/World.map")));
        assertTrue(format.accepts(Resources.getResource("Maps/SeamlessMaps/SeamlessMap.map")));
    }

    @Test
    public void testReader() {
        assertNotNull(format.getReader(FileUtils.toFile(Resources.getResource("Maps/Demo1.map"))));
        assertNotNull(format.getReader(FileUtils.toFile(Resources.getResource("Maps/World.map"))));
        assertNull(format.getReader(FileUtils.toFile(Resources.getResource("Maps/SeamlessMaps/SeamlessMap.map"))));
    }
}
