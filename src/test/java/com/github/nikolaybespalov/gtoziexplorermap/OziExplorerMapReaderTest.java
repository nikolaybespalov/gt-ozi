package com.github.nikolaybespalov.gtoziexplorermap;

import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class OziExplorerMapReaderTest {

    @Test
    public void test1() throws IOException, FactoryException, TransformException {
        OziExplorerMapReader reader = new OziExplorerMapReader(Paths.get("c:\\Program Files\\OziExplorer\\Maps\\Demo1.map"));

        assertEquals(DefaultGeographicCRS.WGS84, reader.getCoordinateReferenceSystem());
        //assertEquals(new GridEnvelope2D(new Rectangle(0, 0, 1202, 778)), reader.getOriginalGridRange());
//        assertEquals(new GeneralGridEnvelope(new Rectangle(0.0, 0.0, 0.0, 0.0)), reader.getOriginalEnvelope());

        GeneralEnvelope generalEnvelope = new GeneralEnvelope(new double[]{0, 1,}, new double[]{2, 3,});

        ParameterValue<GridGeometry2D> geometry = AbstractGridFormat.READ_GRIDGEOMETRY2D.createValue();
        geometry.setValue(new GridGeometry2D(new GeneralGridEnvelope(new Rectangle(0, 0, 256, 256)), generalEnvelope));
        reader.read(new GeneralParameterValue[]{geometry});
    }
}