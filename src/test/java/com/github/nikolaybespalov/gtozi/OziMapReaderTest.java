package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.GeneralEnvelope;
import org.junit.Before;
import org.junit.Test;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

import java.awt.*;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OziMapReaderTest {
    private AbstractGridCoverage2DReader reader;

    @Before
    public void setUp() throws DataSourceException {
        reader = new OziMapReader(ResourceUtils.getResourceAsFile("com/github/nikolaybespalov/gtozi/test-data/mer.map"));
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
}
