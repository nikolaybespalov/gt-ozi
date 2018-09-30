package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataSourceException;
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
    private AbstractGridCoverage2DReader reader;

    @Before
    public void setUp() throws DataSourceException {
        reader = new OziMapReader(TestUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/mer.map"));
    }

    @After
    public void tearDown() {
        reader.dispose();
    }

    @Test
    public void testRead() throws IOException {
        // prepare to read a sub.sampled image
        GeneralParameterValue[] params = new GeneralParameterValue[1];
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
        params[0] = gg;

        assertNotNull(reader.read(params));
    }

    @Test
    public void testGetOriginalEnvelop() {
        assertNotNull(reader.getOriginalEnvelope());
        assertNotNull(reader.getOriginalEnvelope("mer"));
    }

    @Test
    public void testGetFormat() {
        assertNotNull(reader.getFormat());
        assertEquals(OziMapFormat.class, reader.getFormat().getClass());
    }

    @Test
    public void testGetOriginalGridRange() {
        assertNotNull(reader.getOriginalGridRange());
    }

    @Test
    public void testGetInfo() {
        assertTrue(reader.getInfo() instanceof FileServiceInfo);
        assertTrue(((FileServiceInfo) reader.getInfo()).getFiles(null).hasNext());
        assertEquals("mer.map", ((FileServiceInfo) reader.getInfo()).getFiles(null).next().getMainFile().getName());
        assertEquals("mer.jpg", ((FileServiceInfo) reader.getInfo()).getFiles(null).next().getSupportFiles().get(0).getName());
    }
}
