package com.github.nikolaybespalov.gtozi;

import org.geotools.TestData;
import org.geotools.coverage.grid.GridCoverage2D;
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
    private static final String MAP_FILE_PATH = "02-merc/" + COVERAGE_NAME + ".map";
    private AbstractGridCoverage2DReader reader;

    @Before
    public void setUp() throws Exception {
        reader = new OziMapReader(TestData.file(CrsTest.class, MAP_FILE_PATH));
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

    @Test
    public void readWithSuggestedTileSizeShouldReturnTheSameResult() throws IOException {
        final ParameterValue<String> sts = AbstractGridFormat.SUGGESTED_TILE_SIZE.createValue();
        sts.setValue("256,256");

        GridCoverage2D coverage = reader.read(new GeneralParameterValue[]{sts});

        assertEquals(coverage.getRenderedImage().getTileWidth(), 256);
        assertEquals(coverage.getRenderedImage().getTileHeight(), 256);
    }

    @Test
    public void readWithGridGeometryShouldReturnTheSameResult() throws IOException {

        GeneralParameterValue[] params = new GeneralParameterValue[1];

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
        params[0] = gg;

        GridCoverage2D coverage = reader.read(params);

        assertEquals(range, coverage.getGridGeometry().getGridRange2D());
    }
}
