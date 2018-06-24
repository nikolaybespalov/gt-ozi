package com.github.nikolaybespalov.gtoziexplorermap;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.geotools.geometry.DirectPosition2D;
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
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.cs.CartesianCS;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;

import javax.measure.unit.SI;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public final class OziMapFileReader {
    private static final Ellipsoid AIRY_1830 = DefaultEllipsoid.createFlattenedSphere("Airy 1830", 6377563.396, 299.3249646, SI.METER);
    private static final Ellipsoid MODIFIED_AIRY = DefaultEllipsoid.createFlattenedSphere("Modified Airy", 6377340.189, 299.3249646, SI.METER);
    private static final Ellipsoid AUSTRALIAN_NATIONAL = DefaultEllipsoid.createFlattenedSphere("Australian National", 6378160.0, 298.25, SI.METER);

    private static final Ellipsoid KRASSOVSKY = DefaultEllipsoid.createFlattenedSphere("Krassovsky", 6377563.396, 299.3249646, SI.METER);
    private static final Ellipsoid WGS84 = DefaultEllipsoid.WGS84;

    private static final Map<String, OziDatum> OZI_DATUMS = ImmutableMap.of(
            "Adindan", new OziDatum(AIRY_1830, -162, -12, 206),
            "Afgooye", new OziDatum(KRASSOVSKY, -43, -163, 45),
            "Anna 1 Astro 1965", new OziDatum(AUSTRALIAN_NATIONAL, -491, -22, 435),
            "Ireland 1965", new OziDatum(MODIFIED_AIRY, 506, -122, 611),
            "WGS 84", new OziDatum(WGS84, 0, 0, 0)
    );

    private CoordinateReferenceSystem crs;
    private GeographicCRS geoCrs;
    private String projectionName;
    private MathTransform world2Crs;
    private MathTransform grid2Crs;
    private File imageFile;

    public OziMapFileReader(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path, Charset.forName("windows-1251"));

        if (lines.size() < 40) {
            throw new IOException("Too few");
        }

        Collections.swap(lines, 9, 39);

        List<OziCalibrationPoint> calibrationPoints = new ArrayList<>();

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
                        geoCrs = crsFactory.createGeographicCRS(map, datum, DefaultEllipsoidalCS.GEODETIC_2D);
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
                                    this.crs = this.geoCrs;
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

                                    this.crs = crsFactory.createProjectedCRS(properties, geoCrs, conversion, cartCS);
                                    break;
                                }
                            }

                            world2Crs = CRS.findMathTransform(DefaultGeographicCRS.WGS84, this.crs, true);
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

                                if (world2Crs.transform(latLon, p) != null) {
                                    xy = p;
                                }
                            } catch (TransformException e) {
                                // TODO: implement
                            }
                        } else if (NumberUtils.isCreatable(values[14]) && NumberUtils.isCreatable(values[15])) {
                            xy = new DirectPosition2D(crs, NumberUtils.toDouble(values[15]), NumberUtils.toDouble(values[14]));
                        }

                        if (pixelLine != null && xy != null) {
                            calibrationPoints.add(new OziCalibrationPoint(pixelLine, xy));
                        }
                    }
                }
            }
        }

        if (calibrationPoints.size() < 2) {
            return;
        }

        double xPixelSize;
        double yPixelSize;
        double xULC;
        double yULC;

        if (calibrationPoints.size() == 2) {
            OziCalibrationPoint cp1 = calibrationPoints.get(1);
            OziCalibrationPoint cp0 = calibrationPoints.get(0);

            xPixelSize = (cp1.getXy().x - cp0.getXy().x) / (double) (cp1.getPixelLine().x - cp0.getPixelLine().x);
            yPixelSize = (cp1.getXy().y - cp0.getXy().y) / (double) (cp1.getPixelLine().y - cp0.getPixelLine().y);
            xULC = cp0.getXy().x - cp0.getPixelLine().x * xPixelSize;
            yULC = cp0.getXy().y - cp0.getPixelLine().y * yPixelSize;
        } else {
            // TODO: implement
            return;
        }

        grid2Crs = new AffineTransform2D(xPixelSize, 0, 0, yPixelSize, xULC, yULC);
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    public MathTransform getGrid2Crs() {
        return grid2Crs;
    }

    public File getImageFile() {
        return imageFile;
    }
}
