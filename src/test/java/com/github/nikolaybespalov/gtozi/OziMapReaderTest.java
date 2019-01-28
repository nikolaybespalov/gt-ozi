package com.github.nikolaybespalov.gtozi;

import org.geotools.TestData;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.FileServiceInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class OziMapReaderTest {
    private static final String COVERAGE_NAME = "merc-nad27";
    private AbstractGridCoverage2DReader reader;

    @Before
    public void setUp() throws Exception {
        reader = new OziMapReader(TestData.file(CrsTest.class, "02-merc/" + COVERAGE_NAME + ".map"));
    }

    @After
    public void tearDown() {
        reader.dispose();
    }

    @Test
    public void getGridCoverageCountShouldReturnOne() {
        assertEquals(reader.getGridCoverageCount(), 1);
    }

    @Test
    public void getCoverageNamesShouldReturnCorrectName() {
        assertEquals(COVERAGE_NAME, reader.getGridCoverageNames()[0]);
    }

    @Test
    public void readShouldReturnNonNull() throws IOException {
        assertNotNull(reader.read(null));
    }

    @Test
    public void getOriginalEnvelopShouldReturnNonNull() {
        assertNotNull(reader.getOriginalEnvelope());
        assertNotNull(reader.getOriginalEnvelope(COVERAGE_NAME));
    }

    @Test
    public void getFormatShouldReturnEqualsToOziMapFormatClass() {
        assertNotNull(reader.getFormat());
        assertEquals(OziMapFormat.class, reader.getFormat().getClass());
    }

    @Test
    public void getOriginalGridRangeShouldReturnNonNull() {
        assertNotNull(reader.getOriginalGridRange());
    }

    @Test
    public void getInfoShouldReturnMainFileAndSupportFile() {
        assertTrue(reader.getInfo() instanceof FileServiceInfo);
        assertTrue(((FileServiceInfo) reader.getInfo()).getFiles(null).hasNext());
        assertEquals(COVERAGE_NAME + ".map", ((FileServiceInfo) reader.getInfo()).getFiles(null).next().getMainFile().getName());
        assertEquals(COVERAGE_NAME + ".jpg", ((FileServiceInfo) reader.getInfo()).getFiles(null).next().getSupportFiles().get(0).getName());
    }
}
