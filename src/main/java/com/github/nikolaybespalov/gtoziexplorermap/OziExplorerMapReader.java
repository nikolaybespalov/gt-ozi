package com.github.nikolaybespalov.gtoziexplorermap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.WorldFileWriter;
import org.geotools.gce.image.WorldImageReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.operation.DefiningConversion;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.referencing.wkt.Formattable;
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
import org.opengis.referencing.datum.*;
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
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class OziExplorerMapReader extends AbstractGridCoverage2DReader {
    private GeographicCRS geoCRS;
    private String projectionName;
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
                String[] values = Arrays.stream(line.split(",", -1)).map(String::trim).toArray(String[]::new);

                if (values.length < 1) {
                    return;
                }

                String key = values[0];

                if (StringUtils.isEmpty(key)) {
                    return;
                }

                if (key.startsWith("WGS 84")) {
                    String name = values[0];

                    if (StringUtils.isEmpty(name)) {
                        return;
                    }

                    String ellipsoidName = "asd";

                    DatumFactory datumFactory = ReferencingFactoryFinder.getDatumFactory(null);

                    Ellipsoid ellipsoid = DefaultEllipsoid.WGS84;

                    PrimeMeridian greenwichMeridian = org.geotools.referencing.datum.DefaultPrimeMeridian.GREENWICH;

                    Map<String, String> map = new HashMap<>();


                    try {
                        map.put("name", "WGS 84");
                        GeodeticDatum datum = datumFactory.createGeodeticDatum(map, ellipsoid, greenwichMeridian);

                        map.clear();
                        map.put("name", "WGS 84");
                        geoCRS = ReferencingFactoryFinder.getCRSFactory(null).createGeographicCRS(map, datum, DefaultEllipsoidalCS.GEODETIC_2D);
                    }
                    catch (FactoryException e) {
                        return;
                    }
                } else if (key.startsWith("Map Projection")) {
                    String name = values[1];

                    if (StringUtils.isEmpty(name)) {
                        return;
                    }

                    projectionName = name;
                } else if (key.startsWith("Projection Setup")) {
                    if (StringUtils.isEmpty(projectionName)) {
                        return;
                    }

                    try {
                        switch (projectionName) {
                            case "Latitude/Longitude":
                                this.crs = this.geoCRS;
                                break;
                            case "Mercator":
                                if (values.length < 6) {
                                    return;
                                }

                                CartesianCS cartCS = DefaultCartesianCS.GENERIC_2D;
                                MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);
                                ParameterValueGroup parameters = mtFactory.getDefaultParameters("Mercator_1SP");

                                if (NumberUtils.isCreatable(values[1])) {
                                    parameters.parameter("latitude_of_origin").setValue(NumberUtils.toDouble(values[1]));
                                }

                                if (NumberUtils.isCreatable(values[2])) {
                                    parameters.parameter("central_meridian").setValue(NumberUtils.toDouble(values[2]));
                                }

                                if (NumberUtils.isCreatable(values[3])) {
                                    parameters.parameter("scale_factor").setValue(NumberUtils.toDouble(values[3]));
                                }

                                if (NumberUtils.isCreatable(values[4])) {
                                    parameters.parameter("false_easting").setValue(NumberUtils.toDouble(values[4]));
                                }

                                if (NumberUtils.isCreatable(values[5])) {
                                    parameters.parameter("false_northing").setValue(NumberUtils.toDouble(values[5]));
                                }

                                Conversion conversion = new DefiningConversion("Mercator_1SP", parameters);

                                CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);

                                Map<String, ?> properties = Collections.singletonMap("name", "unnamed");

                                //GeographicCRS base = new DefaultGeographicCRS();

                                ProjectedCRS projectedCRS = crsFactory.createProjectedCRS(properties, geoCRS, conversion, cartCS);

                                this.crs = projectedCRS;
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

                    DirectPosition2D borderPosition = new DirectPosition2D(NumberUtils.toDouble(values[2]), NumberUtils.toDouble(values[3]));

                    if (crs == null) {
                        return;
                    }

                    envelope.setCoordinateReferenceSystem(crs);

                    try {
                        envelope.add(world2Model.transform(borderPosition, null));
                    } catch (TransformException e) {
                        return;
                    }
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

            String baseName = FilenameUtils.removeExtension(path.toString());

            Path wldPath = Paths.get(baseName + ".wld");

            if (wldPath.toFile().exists()) {
                Files.delete(Paths.get(baseName + ".wld"));
            }

            Files.createFile(wldPath);

            WorldFileWriter writer = new WorldFileWriter(wldPath.toFile(), tempTransform);

            Path prjPath = Paths.get(baseName + ".prj");

            if (prjPath.toFile().exists()) {
                Files.delete(Paths.get(baseName + ".prj"));
            }


            Files.createFile(prjPath);

            final FileWriter prjWriter = new FileWriter(prjPath.toFile());

            prjWriter.write(((Formattable) crs).toWKT(Citations.OGC, 4));

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

    // TODO: определить остальные методы
}
