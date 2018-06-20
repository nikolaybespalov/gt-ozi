package com.github.nikolaybespalov.gtoziexplorermap;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.image.ImageWorker;
import org.opengis.coverage.grid.Format;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.ReferenceIdentifier;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;

@SuppressWarnings("unused")
public class OziExplorerMapReader extends AbstractGridCoverage2DReader {

    public OziExplorerMapReader(Path path) {
    }

    @Override
    public Format getFormat() {
        return null;
    }

    @Override
    public GridCoverage2D read(GeneralParameterValue[] params) throws IllegalArgumentException, IOException {
        if (params == null) {
            return null;
        }

        GeneralEnvelope requestedEnvelope = null;
        Rectangle dim = null;

        for (int i = 0; i < params.length; i++) {
            final ParameterValue param = (ParameterValue) params[i];
            final ReferenceIdentifier name = param.getDescriptor().getName();
            if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName())) {
                final GridGeometry2D gg = (GridGeometry2D) param.getValue();
                requestedEnvelope = new GeneralEnvelope((Envelope) gg.getEnvelope2D());
                dim = gg.getGridRange2D().getBounds();
                continue;
            }
        }

        if (requestedEnvelope == null) {
            return null;
        }

        return null;

//        Rectangle sourceRegion = new Rectangle(x, y, w, h); // The region you want to extract
//
//        ImageInputStream stream = ImageIO.createImageInputStream(input); // File or input stream
//        Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
//
//        if (readers.hasNext()) {
//            ImageReader reader = readers.next();
//            reader.setInput(stream);
//
        new ImageWorker().warp()
//            param.setSourceRegion(sourceRegion); // Set region
//
//            BufferedImage image = reader.read(0, param); // Will read only the region specified
//        }
    }
}
