package com.github.nikolaybespalov.gtozi;

import org.geotools.TestData;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.factory.GeoTools;
import org.junit.Test;
import org.opengis.parameter.ParameterValueGroup;

import static org.junit.Assert.*;

public class OziMapFormatTest {
    private final AbstractGridFormat format = new OziMapFormat();

    @Test
    public void testDescription() {
        assertEquals("Ozi", format.getName());
        assertEquals("OziExplorer Map File Format", format.getDescription());
        assertEquals("nikolaybespalov", format.getVendor());
        assertEquals("0.1.17", format.getVersion());
        assertEquals("https://github.com/nikolaybespalov/gt-ozi", format.getDocURL());
    }

    @Test
    public void acceptsWithNullShouldReturnFalse() {
        assertFalse(format.accepts(null));
    }

    @Test
    public void acceptsWithNonFileShouldReturnFalse() {
        assertFalse(format.accepts("string"));
    }

    @Test
    public void acceptsWithNonMapFileShouldReturnFalse() throws Exception {
        assertFalse(format.accepts(TestData.file(OziMapFormatTest.class, "02-merc/merc-nad27.jpg")));
    }

    @Test
    public void acceptsWithMapFileWithoutRasterFileLineShouldReturnFalse() throws Exception {
        assertFalse(format.accepts(TestData.url(OziMapFormatTest.class, "bad/noimagefile1.map")));
    }

    @Test
    public void acceptsWithCorrectMapFileShouldReturnTrue() throws Exception {
        assertTrue(format.accepts(TestData.file(OziMapFormatTest.class, "02-merc/merc-nad27.map")));
    }

    @Test
    public void getReaderWithNullShouldReturnNull() {
        assertNull(format.getReader(null));
    }

    @Test
    public void getReaderWithNonFileShouldReturnNull() {
        assertNull(format.getReader("string"));
    }

    @Test
    public void getReaderWithNonMapFileShouldReturnNull() throws Exception {
        assertNull(format.getReader(TestData.file(OziMapFormatTest.class, "02-merc/merc-nad27.jpg")));
    }

    @Test
    public void getReaderWithMapFileWithoutRasterFileLineShouldReturnNull() throws Exception {
        assertNull(format.getReader(TestData.file(OziMapFormatTest.class, "bad/noimagefile1.map"), GeoTools.getDefaultHints()));
    }

    @Test
    public void getReaderWithCorrectMapFileShouldReturnReader() throws Exception {
        assertNotNull(format.getReader(TestData.file(OziMapFormatTest.class, "02-merc/merc-nad27.map")));
    }

    @Test
    public void getWriterShouldReturnNull() throws Exception {
        assertNull(format.getWriter(TestData.file(OziMapFormatTest.class, "02-merc/merc-nad27.map")));
        assertNull(format.getWriter(TestData.file(OziMapFormatTest.class, "02-merc/merc-nad27.map"), GeoTools.getDefaultHints()));
    }

    @Test
    public void getDefaultImageIOWriteParametersShouldReturnNull() {
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
