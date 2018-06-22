package com.github.nikolaybespalov.gtoziexplorermap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.WorldFileWriter;
import org.geotools.gce.image.WorldImageReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.operation.DefiningConversion;
import org.geotools.referencing.operation.projection.Mercator1SP;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.Datum;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class OziExplorerMapReader extends AbstractGridCoverage2DReader {
    private Datum datum;
    private MathTransform world2Model;
    private Path imageFilePath;
    private WorldImageReader worldImageReader;

    public OziExplorerMapReader(Path path) throws DataSourceException, FactoryException, TransformException {
        super(path);

        Rectangle gridRange = new Rectangle();
        GeneralEnvelope envelope = new GeneralEnvelope(2);

        try (Stream<String> lines = Files.lines(path, Charset.forName("windows-1251"))) {
            imageFilePath = Paths.get(lines.skip(2).findFirst().get());

            if (!imageFilePath.toFile().exists()) {
                imageFilePath = Paths.get(imageFilePath.toFile().getName());
            }
        } catch (IOException e) {
            throw new DataSourceException(e);
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

                if (key.startsWith("Datum")) {

                    Mercator1SP.Provider a = new Mercator1SP.Provider();



                } else if (key.startsWith("Map Projection")) {
                    try {
                        String name = values[1];

                        if (StringUtils.isEmpty(name)) {
                            return;
                        }

                        switch (name) {
                            case "Latitude/Longitude":
                                this.crs = DefaultGeographicCRS.WGS84;
                                break;
                            case "Mercator":
                                GeographicCRS geoCRS = org.geotools.referencing.crs.DefaultGeographicCRS.WGS84;
                                CartesianCS cartCS = org.geotools.referencing.cs.DefaultCartesianCS.GENERIC_2D;
                                MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);
                                ParameterValueGroup parameters = mtFactory.getDefaultParameters("Mercator_1SP");
//                                parameters.parameter("central_meridian").setValue(-111.0);
//                                parameters.parameter("latitude_of_origin").setValue(0.0);
//                                parameters.parameter("scale_factor").setValue(1.0);
//                                parameters.parameter("false_easting").setValue(500000.0);
//                                parameters.parameter("false_northing").setValue(0.0);
                                Conversion conversion = new DefiningConversion("Transverse_Mercator", parameters);

                                CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);

                                Map<String, ?> properties = Collections.singletonMap("name", "WGS 84 / UTM Zone 12N");

                                this.crs = crsFactory.createProjectedCRS(properties, geoCRS, conversion, cartCS);
                                break;
                            default:
                                return;
                        }

                        world2Model = CRS.findMathTransform(DefaultGeographicCRS.WGS84, this.crs, true);
                    } catch (FactoryException e) {
                        return;
                    }
                } else if (key.startsWith("Projection Setup")) {

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

                    DirectPosition2D borderPosition = new DirectPosition2D(NumberUtils.toDouble(values[2]), NumberUtils.toDouble(values[3]));


                    envelope.add(borderPosition);

//                    try {
//                        envelope.add(world2Model.transform(borderPosition, null));
//                    } catch (TransformException e) {
//                        return;
//                    }
                }
            });
        } catch (IOException e) {
            throw new DataSourceException(e);
        }

        if (gridRange.isEmpty() || envelope.isEmpty()) {
            return;
        }

        double xPixelSize = (envelope.getMaximum(0) - envelope.getMinimum(0)) / gridRange.width, yPixelSize = (envelope.getMaximum(1) - envelope.getMinimum(1)) / gridRange.height, xULC = envelope.getMinimum(0), yULC = envelope.getMinimum(1);

        AffineTransform t = new AffineTransform2D(xPixelSize, 0, 0, yPixelSize, xULC, yULC);

        this.raster2Model = ProjectiveTransform.create(t);

        AffineTransform tempTransform = new AffineTransform((AffineTransform) this.raster2Model);
        tempTransform.translate(+0.5D, +0.5D);

        try {
            originalEnvelope = CRS.transform(raster2Model, new GeneralEnvelope(gridRange));
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

            WorldFileWriter writer = new WorldFileWriter(wldPath.toFile(), tempTransform);

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

        worldImageReader = new WorldImageReader(new File(path.getParent().toString() + "/" + imageFilePath.toFile().getName()));
    }

    @Override
    public Format getFormat() {
        return null;
    }

    @Override
    public GridCoverage2D read(GeneralParameterValue[] params) throws IllegalArgumentException, IOException {
        return worldImageReader.read(params);
    }

    @Override
    public GridEnvelope getOriginalGridRange() {
        return worldImageReader.getOriginalGridRange();
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return worldImageReader.getCoordinateReferenceSystem();
    }
}
