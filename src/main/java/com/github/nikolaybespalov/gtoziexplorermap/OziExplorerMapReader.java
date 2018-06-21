package com.github.nikolaybespalov.gtoziexplorermap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.WorldFileWriter;
import org.geotools.gce.image.WorldImageReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.coverage.grid.Format;
import org.opengis.geometry.BoundingBox;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class OziExplorerMapReader extends AbstractGridCoverage2DReader {
    private MathTransform world2Model;
    private WorldImageReader worldImageReader;

    public OziExplorerMapReader(Path path) throws DataSourceException, FactoryException, TransformException {
        super(path);

        Path imageFilePath = Paths.get("c:\\Program Files\\OziExplorer\\Maps\\Demo1.bmp");

        // разбор .MAP-файла
        // выделение информации о проекции
        // выделение информации о преобразовании растр->модель

        this.crs = DefaultGeographicCRS.WGS84;

        class Gcp {
            DirectPosition2D xy;
            Point linePixel;

            public Gcp(DirectPosition2D xy, Point linePixel) {
                this.xy = xy;
                this.linePixel = linePixel;
            }
        }

        List<Gcp> gcps = new ArrayList<>();

        Rectangle gridRange = new Rectangle();
        GeneralEnvelope envelope = new GeneralEnvelope(2);

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

                if (key.startsWith("Map Projection")) {
                    try {
                        String name = values[1];

                        if (StringUtils.isEmpty(name)) {
                            return;
                        }

                        switch (name) {
                            case "Latitude/Longitude":
                                this.crs = DefaultGeographicCRS.WGS84;
                                break;
                            default:
                                return;
                        }

                        world2Model = CRS.findMathTransform(DefaultGeographicCRS.WGS84, this.crs, true);
                    } catch (FactoryException e) {
                        return;
                    }
                } else if (key.startsWith("MMPXY")) {
                    if (values.length < 4) {
                        return;
                    }

                    if (!NumberUtils.isCreatable(values[2]) || !NumberUtils.isCreatable(values[3])) {
                        return;
                    }

                    Point borderPoint = new Point(NumberUtils.toInt(values[2]), NumberUtils.toInt(values[3]));

                    gridRange.add(borderPoint);
                } else if (key.startsWith("MMPLL")) {
                    if (values.length < 4) {
                        return;
                    }

                    if (!NumberUtils.isCreatable(values[2]) || !NumberUtils.isCreatable(values[3])) {
                        return;
                    }

                    // transform

                    DirectPosition2D borderPosition = new DirectPosition2D(NumberUtils.toDouble(values[2]), NumberUtils.toDouble(values[3]));



                    envelope.add(borderPosition);
                } else if (key.startsWith("Point")) {
                    if (values.length < 17) {
                        return;
                    }

                    if (!NumberUtils.isCreatable(values[2]) || !NumberUtils.isCreatable(values[3])) {
                        return;
                    }

                    Point gcpLinePixel = new Point(NumberUtils.toInt(values[2]), NumberUtils.toInt(values[3]));

                    DirectPosition2D gcpXY = new DirectPosition2D(this.crs);

                    if (NumberUtils.isCreatable(values[6]) && NumberUtils.isCreatable(values[7]) &&
                            NumberUtils.isCreatable(values[9]) && NumberUtils.isCreatable(values[10]) &&
                            world2Model != null) {
                        double lat = NumberUtils.toDouble(values[6]) + NumberUtils.toDouble(values[7]) / 60.0;
                        double lon = NumberUtils.toDouble(values[9]) + NumberUtils.toDouble(values[10]) / 60.0;

                        if (values[8].equals("S")) {
                            lat = -lat;
                        }

                        if (values[11].equals("W")) {
                            lon = -lon;
                        }

                        DirectPosition2D gcpLatLon = new DirectPosition2D(DefaultGeographicCRS.WGS84, lat, lon);

                        try {
                            world2Model.transform(gcpLatLon, gcpXY);
                        } catch (TransformException e) {
                            return;
                        }
                    } else if (NumberUtils.isCreatable(values[14]) && NumberUtils.isCreatable(values[15])) {
                        gcpXY.setLocation(NumberUtils.toDouble(values[14]), NumberUtils.toDouble(values[15]));
                    } else {
                        return;
                    }

                    gcps.add(new Gcp(gcpXY, gcpLinePixel));
                }
            });
        } catch (IOException e) {
            throw new DataSourceException(e);
        }

//        Gcp minGcp = gcps.get(0);
//        Gcp maxGcp = gcps.get(1);

        if (gcps.size() < 2) {
            // TODO: Parse MMPXY and MMPLL
        } else {
//            Gcp minGcpX = gcps.stream().min(Comparator.comparingDouble(a -> a.linePixel.x)).get();
//            Gcp maxGcpX = gcps.stream().max(Comparator.comparingDouble(a -> a.linePixel.x)).get();
//
//            Gcp minGcpY = gcps.stream().min(Comparator.comparingDouble(a -> a.linePixel.y)).get();
//            Gcp maxGcpY = gcps.stream().max(Comparator.comparingDouble(a -> a.linePixel.y)).get();
//
//            minGcp = new Gcp(new DirectPosition2D(minGcpX.xy.getCoordinateReferenceSystem(), minGcpX.xy.x, minGcpY.xy.y), new Point(minGcpX.linePixel.x, minGcpY.linePixel.y));
//            maxGcp = new Gcp(new DirectPosition2D(maxGcpX.xy.getCoordinateReferenceSystem(), maxGcpX.xy.x, maxGcpY.xy.y), new Point(maxGcpX.linePixel.x, maxGcpY.linePixel.y));
        }

        if (gridRange.isEmpty() || envelope.isEmpty()) {
            return;
        }

        double xPixelSize = (envelope.getMaximum(0) - envelope.getMinimum(0)) / gridRange.width, yPixelSize = (envelope.getMaximum(1) - envelope.getMinimum(1)) / gridRange.height, xULC = envelope.getMinimum(0), yULC = envelope.getMinimum(1);

        AffineTransform tt = new AffineTransform2D(xPixelSize, 0, 0, yPixelSize, xULC, yULC);

        Point2D p = tt.transform(new Point2D.Double(1202, 778), null);

        try {
            originalEnvelope = CRS.transform(ProjectiveTransform.create(tt), new GeneralEnvelope(new Rectangle(0, 0, 1202, 778)));
            originalEnvelope.setCoordinateReferenceSystem(crs);
        } catch (TransformException e) {
            throw new DataSourceException(e);
        }

        try {
            Path wldPath = Paths.get(path.getParent().toString(), "Demo1" + ".wld");

            if (wldPath.toFile().exists()) {
                Files.delete(Paths.get(path.getParent().toString(), "Demo1" + ".wld"));
            }

            Files.createFile(wldPath);

            WorldFileWriter writer = new WorldFileWriter(wldPath.toFile(), tt);

            Path prjPath = Paths.get(path.getParent().toString(), "Demo1" + ".prj");

            if (prjPath.toFile().exists()) {
                Files.delete(Paths.get(path.getParent().toString(), "Demo1" + ".prj"));
            }

            Files.createFile(prjPath);

            final FileWriter prjWriter = new FileWriter(prjPath.toFile());

            prjWriter.write(crs.toWKT());

            prjWriter.close();
        } catch (IOException e) {
            throw new DataSourceException(e);
        }

        worldImageReader = new WorldImageReader(imageFilePath.toFile());


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
        return worldImageReader.read(params);
    }
}
