package com.github.nikolaybespalov.gtoziexplorermap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.coverage.grid.Format;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class OziExplorerMapReader extends AbstractGridCoverage2DReader {

    public OziExplorerMapReader(Path path) throws DataSourceException {
        if (path == null) {
            throw new IllegalArgumentException("asdasd");
        }

//        OziMapFile mapFile = new OziMapFile(path);
//
//        OziProjection oziProjection = mapFile.getProjection();
//
//        if (oziProjection == null) {
//            throw new IllegalArgumentException("asdasd");
//        }

        this.crs = DefaultGeographicCRS.WGS84;
        this.originalGridRange = new GeneralGridEnvelope(new Rectangle(0, 0, 123, 123));
        //originalEnvelope = CRS.transform(CRS.findMathTransform(DefaultCartesianCS.GENERIC_2D, ), new GeneralEnvelope(actualDim));
        //originalEnvelope.setCoordinateReferenceSystem(crs);

        MathTransform latLon2Model;

        try {
            latLon2Model = CRS.findMathTransform(DefaultGeographicCRS.WGS84, crs, true);

            double a = 152.991764 - 152.288056;
            double b = -26.858472 - -26.449320;

            AffineTransform t = AffineTransform.getScaleInstance(1202 / b, 778 / b);

            AffineTransform t2 = AffineTransform.getTranslateInstance(26.449320, -152.288056);

            AffineTransform t1 = new AffineTransform();
            t1.concatenate(t);
            t1.concatenate(t2);

            Point2D p = t2.createInverse().transform(new DirectPosition2D(0, 0), null);

            int asd = 0;
            int asdf = asd;
        } catch (FactoryException | NoninvertibleTransformException e) {
            return;
        }

        try (Stream<String> lines = Files.lines(path, Charset.forName("windows-1251"))) {
            lines.forEach(line -> {
                String[] values = Arrays.stream(line.split(",")).map(String::trim).toArray(String[]::new);

                if (values.length < 1) {
                    return;
                }

                String key = values[0];

                if (StringUtils.isEmpty(key)) {
                    return;
                }

                if (key.startsWith("Point")) {
                    if (values.length < 17) {
                        return;
                    }

                    if (!NumberUtils.isCreatable(values[2]) || !NumberUtils.isCreatable(values[3])) {
                        return;
                    }

                    double gcpY;
                    double gcpX;

                    if (NumberUtils.isCreatable(values[6]) && NumberUtils.isCreatable(values[7]) &&
                            NumberUtils.isCreatable(values[9]) && NumberUtils.isCreatable(values[10])) {
                        double lat = (NumberUtils.toDouble(values[6]) + NumberUtils.toDouble(values[7])) / 60.0;
                        double lon = (NumberUtils.toDouble(values[9]) + NumberUtils.toDouble(values[10])) / 60.0;

                        if (values[8].equals("S")) {
                            lat = -lat;
                        }

                        if (values[11].equals("W")) {
                            lon = -lon;
                        }

                        try {
                            DirectPosition p = latLon2Model.transform(new DirectPosition2D(DefaultGeographicCRS.WGS84, lon, lat), null);

                            gcpX = p.getOrdinate(0);
                            gcpY = p.getOrdinate(1);
                        } catch (TransformException e) {
                            return;
                        }
                    } else if (NumberUtils.isCreatable(values[14]) && NumberUtils.isCreatable(values[15])) {
                        gcpY = NumberUtils.toDouble(values[15]);
                        gcpX = NumberUtils.toDouble(values[14]);
                    } else {
                        return;
                    }

                    double gcpPixel = NumberUtils.toDouble(values[2]);
                    double gcpLine = NumberUtils.toDouble(values[3]);

                }


            });
        } catch (IOException e) {
            throw new DataSourceException(e);
        }



        int asd = 0;
        int asdf = asd;

//        if (StringUtils.compare(OziProjection.LatitudeLongitude, oziProjection.getName()) == 0) {
//            crs = DefaultGeographicCRS.WGS84;
//
//        } else {
//            throw new IllegalArgumentException("Unsupported projection");
//        }
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
//            param.setSourceRegion(sourceRegion); // Set region
//
//            BufferedImage image = reader.read(0, param); // Will read only the region specified
//        }
    }
}
