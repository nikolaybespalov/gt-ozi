package com.github.nikolaybespalov.gtoziexplorermap;

import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.junit.Test;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;

import java.awt.*;
import java.io.IOException;

public class OziExplorerMapReaderTest {

    @Test
    public void test1() throws IOException {
        OziExplorerMapReader reader = new OziExplorerMapReader(null);

        GeneralEnvelope generalEnvelope = new GeneralEnvelope(new double[]{0, 1,}, new double[]{2, 3,});

        ParameterValue<GridGeometry2D> geometry = AbstractGridFormat.READ_GRIDGEOMETRY2D.createValue();
        geometry.setValue(new GridGeometry2D(new GeneralGridEnvelope(new Rectangle(0, 0, 256, 256)), generalEnvelope));
        reader.read(new GeneralParameterValue[]{geometry});
    }
}