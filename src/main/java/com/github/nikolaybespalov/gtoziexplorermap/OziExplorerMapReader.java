package com.github.nikolaybespalov.gtoziexplorermap;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.WorldFileWriter;
import org.geotools.gce.image.WorldImageReader;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.metadata.iso.spatial.PixelTranslation;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.datum.BursaWolfParameters;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
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
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;

import javax.imageio.stream.FileImageInputStream;
import javax.measure.unit.SI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public final class OziExplorerMapReader extends AbstractGridCoverage2DReader {
    private static final Map<String, Ellipsoid> OZI_ELLIPSOIDS = ImmutableMap.of(
            "Airy 1830", DefaultEllipsoid.createFlattenedSphere("Airy 1830", 6377563.396, 299.3249646, SI.METER)
    );

    private static final Ellipsoid AIRY_1830 = DefaultEllipsoid.createFlattenedSphere("Airy 1830", 6377563.396, 299.3249646, SI.METER);

    private static final Map<String, OziDatum> OZI_DATUMS = ImmutableMap.of(
            "Adindan", new OziDatum(AIRY_1830, -162, -12, 206),
            "WGS 84", new OziDatum(DefaultEllipsoid.WGS84, 0, 0, 0)
    );

    private GeographicCRS geoCRS;
    private String projectionName;
    private MathTransform world2Model;
    private File imageFile;
    private WorldImageReader worldImageReader;

    @SuppressWarnings("WeakerAccess")
    public OziExplorerMapReader(Path path) throws DataSourceException {
        super(path);

        Rectangle gridRange = new Rectangle();
        GeneralEnvelope envelope = new GeneralEnvelope(2);
        List<Point> pixelLineGcps = new ArrayList<>();
        List<DirectPosition2D> xyGcps = new ArrayList<>();

        List<String> lines;

        try {
            lines = Files.readAllLines(path, Charset.forName("windows-1251"));
        } catch (IOException e) {
            throw new DataSourceException(e);
        }

        Collections.swap(lines, 9, 39);

        for (int lineIndex = 0; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);
            String[] values = Arrays.stream(line.split(",", -1)).map(String::trim).toArray(String[]::new);

            if (values.length < 1) {
                return;
            }

            String key = values[0];

            if (StringUtils.isEmpty(key)) {
                continue;
            }

            switch (lineIndex) {
                case 2:
                    imageFile = new File(line);

                    if (!imageFile.exists()) {
                        imageFile = new File(path.getParent().toFile(), imageFile.getName());

                        if (!imageFile.exists()) {
                            return;
                        }
                    }
                    break;
                case 4:
                    if (values.length < 5) {
                        return;
                    }

                    OziDatum oziDatum = OZI_DATUMS.get(key);

                    if (oziDatum == null) {
                        return;
                    }

                    Ellipsoid ellipsoid = null;

                    String ellipsoidName = values[1];

                    if (!StringUtils.isEmpty(ellipsoidName)) {
                        if (NumberUtils.isCreatable(values[2]) && NumberUtils.isCreatable(values[3])) {
                            ellipsoid = DefaultEllipsoid.createFlattenedSphere(ellipsoidName, NumberUtils.toDouble(values[2]), NumberUtils.toDouble(values[3]), SI.METER);
                        }
                    }

                    if (ellipsoid == null) {
                        ellipsoid = oziDatum.ellipsoid;
                    }

                    String targetDatumName = values[4];

                    if (StringUtils.isEmpty(targetDatumName)) {
                        return;
                    }

                    GeodeticDatum targetDatum;

                    switch (targetDatumName) {
                        case "WGS 84":
                            targetDatum = DefaultGeodeticDatum.WGS84;
                            break;
                        default:
                            return;
                    }

                    try {
                        Map<String, Object> map = new HashMap<>();

                        map.put("name", key);

                        final BursaWolfParameters bursaWolfParameters = new BursaWolfParameters(targetDatum);

                        bursaWolfParameters.dx = oziDatum.dx;
                        bursaWolfParameters.dy = oziDatum.dy;
                        bursaWolfParameters.dz = oziDatum.dz;

                        if (!bursaWolfParameters.isIdentity()) {
                            map.put(DefaultGeodeticDatum.BURSA_WOLF_KEY, bursaWolfParameters);
                        }

                        DatumFactory datumFactory = ReferencingFactoryFinder.getDatumFactory(null);
                        GeodeticDatum datum = datumFactory.createGeodeticDatum(map, ellipsoid, DefaultPrimeMeridian.GREENWICH);

                        map.clear();
                        map.put("name", key);

                        CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);
                        geoCRS = crsFactory.createGeographicCRS(map, datum, DefaultEllipsoidalCS.GEODETIC_2D);
                    } catch (FactoryException e) {
                        return;
                    }
                    break;
                default: {
                    if (key.startsWith("Map Projection")) {
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
                                case "Latitude/Longitude": {
                                    this.crs = this.geoCRS;
                                    break;
                                }
                                case "Mercator": {
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

                                    this.crs = crsFactory.createProjectedCRS(properties, geoCRS, conversion, cartCS);
                                    break;
                                }
                            }

                            world2Model = CRS.findMathTransform(DefaultGeographicCRS.WGS84, this.crs, true);
                        } catch (FactoryException e) {
                            return;
                        }
                    } else if (key.startsWith("Point")) {
                        Point pixelLine = null;

                        if (NumberUtils.isCreatable(values[2]) && NumberUtils.isCreatable(values[3])) {
                            pixelLine = new Point(NumberUtils.toInt(values[2]), NumberUtils.toInt(values[3]));
                        }

                        DirectPosition2D xy = null;

                        if (NumberUtils.isCreatable(values[6]) && NumberUtils.isCreatable(values[7]) &&
                                NumberUtils.isCreatable(values[9]) && NumberUtils.isCreatable(values[10])) {
                            DirectPosition2D latLon = new DirectPosition2D(DefaultGeographicCRS.WGS84,
                                    NumberUtils.toDouble(values[9]) + NumberUtils.toDouble(values[10]) / 60.0,
                                    NumberUtils.toDouble(values[6]) + NumberUtils.toDouble(values[7]) / 60.0);

                            if (values[11].equals("W")) {
                                latLon.x = -latLon.x;
                            }

                            if (values[8].equals("S")) {
                                latLon.y = -latLon.y;
                            }

                            try {
                                DirectPosition2D p = new DirectPosition2D(crs);

                                if (world2Model.transform(latLon, p) != null) {
                                    xy = p;
                                }
                            } catch (TransformException e) {

                            }

                            int asd = 0;
                            int asdf = asd;
                        } else if (NumberUtils.isCreatable(values[14]) && NumberUtils.isCreatable(values[15])) {
                            xy = new DirectPosition2D(crs,
                                    NumberUtils.toDouble(values[15]),
                                    NumberUtils.toDouble(values[14]));
                        }

                        if (pixelLine != null && xy != null) {
                            //gridRange.add(pixelLine);
                            //envelope.add(xy);
                            pixelLineGcps.add(pixelLine);
                            xyGcps.add(xy);
                            int asd = 0;
                            int asdf = asd;
                        }
                    } else if (key.startsWith("MMPXY")) {
//                        if (values.length < 4) {
//                            return;
//                        }
//
//                        if (!NumberUtils.isCreatable(values[2]) || !NumberUtils.isCreatable(values[3])) {
//                            return;
//                        }
//
//                        Point borderPoint = new Point(NumberUtils.toInt(values[2]), NumberUtils.toInt(values[3]));
//
//                        gridRange.add(borderPoint);
                    } else if (key.startsWith("MMPLL")) {
//                        if (values.length < 4) {
//                            return;
//                        }
//
//                        if (!NumberUtils.isCreatable(values[2]) || !NumberUtils.isCreatable(values[3])) {
//                            return;
//                        }
//
//                        DirectPosition2D borderPosition = new DirectPosition2D(NumberUtils.toDouble(values[2]), NumberUtils.toDouble(values[3]));
//
//                        if (crs == null) {
//                            return;
//                        }
//
//                        envelope.setCoordinateReferenceSystem(crs);
//
//                        try {
//                            envelope.add(world2Model.transform(borderPosition, null));
//                        } catch (TransformException e) {
//                            return;
//                        }
                    }
                }
            }
        }

//        if (gridRange.isEmpty() || envelope.isEmpty()) {
//            return;
//        }

        if (pixelLineGcps.isEmpty() || xyGcps.isEmpty()) {
            return;
        }

        OzfImageReader imageReader = new OzfImageReader(null);

        int width = 0;
        int height = 0;

        try {
            imageReader.setInput(new FileImageInputStream(imageFile));

            width = imageReader.getWidth(0);
            height = imageReader.getHeight(0);

        } catch (IOException e) {

        }

        double xPixelSize = (xyGcps.get(0).x - xyGcps.get(1).x) / (double) (pixelLineGcps.get(0).x - pixelLineGcps.get(1).x),
                yPixelSize = (xyGcps.get(0).y - xyGcps.get(1).y) / (double) (pixelLineGcps.get(0).y - pixelLineGcps.get(1).y),
                xULC = xyGcps.get(1).x,
                yULC = xyGcps.get(1).y;

        xULC = xyGcps.get(1).x
                - pixelLineGcps.get(1).x * xPixelSize;

        yULC = xyGcps.get(1).y
                - pixelLineGcps.get(1).y * yPixelSize;

//        double xPixelSize = (envelope.getMaximum(0) - envelope.getMinimum(0)) / width,
//                yPixelSize = (envelope.getMaximum(1) - envelope.getMinimum(1)) / height,
//                xULC = envelope.getMinimum(0),
//                yULC = envelope.getMinimum(1);

//        Pixel Size = (0.000585447394297,-0.000525901875902)
//        Corner Coordinates:
//        Upper Left  ( 152.2880561, -26.4493203) (152d17'17.00"E, 26d26'57.55"S)
//        Lower Left  ( 152.2880561, -26.8584719) (152d17'17.00"E, 26d51'30.50"S)
//        Upper Right ( 152.9917639, -26.4493203) (152d59'30.35"E, 26d26'57.55"S)
//        Lower Right ( 152.9917639, -26.8584719) (152d59'30.35"E, 26d51'30.50"S)

//        double xPixelSize = 0.000585447394297,
//                yPixelSize = -0.000525901875902,
//                xULC = 152.2880561,
//                yULC = -26.4493203;

        MathTransform transform = new AffineTransform2D(xPixelSize, 0, 0, yPixelSize, xULC, yULC);

        try {
            MathTransform tempTransform =
                    PixelTranslation.translate(
                            transform, PixelInCell.CELL_CORNER, PixelInCell.CELL_CENTER);

            String baseName = FilenameUtils.removeExtension(path.toString());

            Path wldPath = Paths.get(baseName + ".wld");

            if (wldPath.toFile().exists()) {
                Files.delete(Paths.get(baseName + ".wld"));
            }

            Files.createFile(wldPath);

            new WorldFileWriter(wldPath.toFile(), tempTransform); // Нет необходимости закрывать WorldFileWriter

            Path prjPath = Paths.get(baseName + ".prj");

            if (prjPath.toFile().exists()) {
                Files.delete(Paths.get(baseName + ".prj"));
            }

            Files.createFile(prjPath);

            final FileWriter prjWriter = new FileWriter(prjPath.toFile());

            prjWriter.write(((Formattable) crs).toWKT(Citations.OGC, 2));

            prjWriter.close();
        } catch (IOException e) {
            throw new DataSourceException(e);
        }

        AffineTransform tempTransform = new AffineTransform((AffineTransform) transform);
        tempTransform.translate(-0.5D, -0.5D);

        GeneralEnvelope originalEnvelope2 = null;

        try {
            this.originalEnvelope = new GeneralEnvelope(new GridEnvelope2D(gridRange), PixelInCell.CELL_CORNER, transform, crs);
            originalEnvelope2 = CRS.transform(ProjectiveTransform.create(tempTransform), new GeneralEnvelope(gridRange));
        } catch (TransformException e) {

        }

        worldImageReader = new WorldImageReader(imageFile);
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
    public GeneralEnvelope getOriginalEnvelope() {
        return worldImageReader.getOriginalEnvelope();
    }

    @Override
    public GeneralEnvelope getOriginalEnvelope(String coverageName) {
        return worldImageReader.getOriginalEnvelope(coverageName);
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return worldImageReader.getCoordinateReferenceSystem();
    }

    // TODO: определить остальные методы
}
