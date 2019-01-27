package com.github.nikolaybespalov.gtozi;

import org.geotools.TestData;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.FileServiceInfo;
import org.geotools.geometry.GeneralEnvelope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

import java.awt.*;
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
    public void getCoverageNamesShouldReturnCorrectName() {
        assertEquals(reader.getGridCoverageCount(), 1);
        assertEquals(COVERAGE_NAME, reader.getGridCoverageNames()[0]);
    }

    @Test
    public void testRead() throws IOException {

        // Define a GridGeometry in order to reduce the output
        final ParameterValue<GridGeometry2D> gg =
                AbstractGridFormat.READ_GRIDGEOMETRY2D.createValue();
        final GeneralEnvelope envelope = reader.getOriginalEnvelope();
        final Dimension dim = new Dimension();
        dim.setSize(
                reader.getOriginalGridRange().getSpan(0) / 4,
                reader.getOriginalGridRange().getSpan(1) / 4);
        final Rectangle rasterArea = ((GridEnvelope2D) reader.getOriginalGridRange());
        rasterArea.setSize(dim);
        final GridEnvelope2D range = new GridEnvelope2D(rasterArea);
        gg.setValue(new GridGeometry2D(range, envelope));

        ParameterValue tileSize = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();

        assertNotNull(reader.read(new GeneralParameterValue[]{gg, tileSize}));

        tileSize.setValue("256");
        assertNotNull(reader.read(new GeneralParameterValue[]{gg, tileSize}));

        tileSize.setValue("128,128,128");
        assertNotNull(reader.read(new GeneralParameterValue[]{gg, tileSize}));
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
