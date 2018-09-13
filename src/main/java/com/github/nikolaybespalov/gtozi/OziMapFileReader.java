package com.github.nikolaybespalov.gtozi;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.geotools.data.DataSourceException;
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
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.DefiningConversion;
import org.geotools.referencing.operation.projection.MapProjection;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.opengis.geometry.DirectPosition;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;

import javax.measure.unit.SI;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

@SuppressWarnings("WeakerAccess")
final class OziMapFileReader {
    public static final Map<String, String> OZI_PROJECTION_NAME_TO_GEOTOOLS = new HashMap<>();
    private static final Map<String, Ellipsoid> ELLIPS = new HashMap<>();
    private static final Map<String, GeodeticDatum> DATUMS = new HashMap<>();

    static {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    static {
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("Mercator", "Mercator_1SP");
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("Transverse Mercator", "Transverse_Mercator");
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("Lambert Conformal Conic", "Lambert_Conformal_Conic_2SP");
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("Sinusoidal", "Sinusoidal");
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("Albers Equal Area", "Albers_Conic_Equal_Area");

        //
        // Read ozi_datum.csv and ozi_ellips.csv
        //

        try (CSVParser csvParser = new CSVParser(new InputStreamReader(OziMapFileReader.class.getClassLoader().getResourceAsStream("com/github/nikolaybespalov/gtozi/data/ozi_ellips.csv")), CSVFormat.RFC4180.withFirstRecordAsHeader().withCommentMarker('#'))) {
            for (CSVRecord csvRecord : csvParser) {
                String ellipsoidCode = csvRecord.get("ELLIPSOID_CODE");
                String name = csvRecord.get("NAME");
                String a = csvRecord.get("A");
                String invf = csvRecord.get("INVF");

                Ellipsoid ellipsoid = DefaultEllipsoid.createFlattenedSphere(name, NumberUtils.toDouble(a), NumberUtils.toDouble(invf), SI.METER);

                ELLIPS.put(ellipsoidCode, ellipsoid);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }

        try (CSVParser csvParser = new CSVParser(new InputStreamReader(OziMapFileReader.class.getClassLoader().getResourceAsStream("com/github/nikolaybespalov/gtozi/data/ozi_datum.csv")), CSVFormat.RFC4180.withFirstRecordAsHeader().withCommentMarker('#'))) {
            for (CSVRecord csvRecord : csvParser) {
                String name = csvRecord.get("NAME");
                String epsgDatumCode = csvRecord.get("EPSG_DATUM_CODE");
                String ellipsoidCode = csvRecord.get("ELLIPSOID_CODE");
                String dx = csvRecord.get("DELTAX");
                String dy = csvRecord.get("DELTAY");
                String dz = csvRecord.get("DELTAZ");

                GeodeticDatum datum;

                if (!StringUtils.isEmpty(epsgDatumCode)) {
                    GeographicCRS geoCrs = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null).createGeographicCRS(epsgDatumCode);
                    datum = geoCrs.getDatum();
                } else {
                    Ellipsoid ellipsoid = ELLIPS.get(ellipsoidCode);

                    datum = createGeodeticDatum(name, ellipsoid, NumberUtils.toDouble(dx), NumberUtils.toDouble(dy), NumberUtils.toDouble(dz));
                }

                DATUMS.put(name, datum);
            }
        } catch (IOException | FactoryException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private CoordinateReferenceSystem crs;
    private MathTransform grid2Crs;
    private File imageFile;

    public OziMapFileReader(File file) throws DataSourceException {
        try {
            if (!Files.isReadable(file.toPath())) {
                throw new DataSourceException("File " + file.getAbsolutePath() + " can not be read.");
            }

            //
            // Read all lines of the file
            //

            List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("windows-1251"));

            if (lines.size() < 5) {
                throw new DataSourceException("Not enough data");
            }

            //
            // Parse and validate file header
            //

            String header = parseHeader(lines);

            if (!StringUtils.startsWith(header, "OziExplorer Map Data File")) {
                throw new DataSourceException("File " + file.getAbsolutePath() + " does not a OziExplorer map data file.");
            }

            //
            // Parse and validate image filename
            //

            String imageFilename = parseImageFilename(lines);

            if (StringUtils.isEmpty(imageFilename)) {
                throw new DataSourceException("Map file does not contain an image filename");
            }

            imageFile = new File(imageFilename);

            if (!imageFile.exists()) {
                imageFile = new File(file.getParent(), imageFilename);
            }

            if (!Files.isReadable(imageFile.toPath())) {
                throw new DataSourceException("Image file " + file.getAbsolutePath() + " can not be read.");
            }

            //
            // Parse datum
            //

            GeodeticDatum datum = parseDatum(lines);

            GeographicCRS geoCrs = ReferencingFactoryFinder.getCRSFactory(null).createGeographicCRS(Collections.singletonMap("name", datum.getName().getCode()), datum, DefaultEllipsoidalCS.GEODETIC_2D);

            //
            // Parse projection
            //

            String projectionName = parseMapProjection(lines);

            if ("Albers Equal Area".equals(projectionName)) {
                geoCrs = ReferencingFactoryFinder.getCRSAuthorityFactory("EPSG", null).createGeographicCRS("NAD27");
            }

            crs = parseProjectionSetup(lines, projectionName, geoCrs);

            MathTransform world2Crs = CRS.findMathTransform(geoCrs, crs, true);

            List<CalibrationPoint> calibrationPoints = parseCalibrationPoints(lines, world2Crs);

            if (calibrationPoints.size() < 2) {
                throw new DataSourceException("Too few calibration points!");
            }

            double xPixelSize;
            double yPixelSize;
            double xULC;
            double yULC;

            if (calibrationPoints.size() == 2) {
                CalibrationPoint cp1 = calibrationPoints.get(calibrationPoints.size() - 1);
                CalibrationPoint cp0 = calibrationPoints.get(0);

                xPixelSize = (cp1.getXy().x - cp0.getXy().x) / (double) (cp1.getPixelLine().x - cp0.getPixelLine().x);
                yPixelSize = (cp1.getXy().y - cp0.getXy().y) / (double) (cp1.getPixelLine().y - cp0.getPixelLine().y);
                xULC = cp0.getXy().x - (double) cp0.getPixelLine().x * xPixelSize;
                yULC = cp0.getXy().y - (double) cp0.getPixelLine().y * yPixelSize;

                grid2Crs = new AffineTransform2D(xPixelSize, 0, 0, yPixelSize, xULC, yULC);
            } else {
                int nGCPCount = calibrationPoints.size();

                CalibrationPoint cp0 = calibrationPoints.get(0);

                double min_pixel = cp0.pixelLine.x;
                double max_pixel = cp0.pixelLine.x;
                double min_line = cp0.pixelLine.y;
                double max_line = cp0.pixelLine.y;
                double min_geox = cp0.xy.x;
                double max_geox = cp0.xy.x;
                double min_geoy = cp0.xy.y;
                double max_geoy = cp0.xy.y;

                for (int i = 1; i < calibrationPoints.size(); ++i) {
                    CalibrationPoint cp = calibrationPoints.get(i);

                    min_pixel = Math.min(min_pixel, cp.pixelLine.x);
                    max_pixel = Math.max(max_pixel, cp.pixelLine.x);
                    min_line = Math.min(min_line, cp.pixelLine.y);
                    max_line = Math.max(max_line, cp.pixelLine.y);
                    min_geox = Math.min(min_geox, cp.xy.x);
                    max_geox = Math.max(max_geox, cp.xy.x);
                    min_geoy = Math.min(min_geoy, cp.xy.y);
                    max_geoy = Math.max(max_geoy, cp.xy.y);
                }

                double EPS = 1.0e-12;

                if (Math.abs(max_pixel - min_pixel) < EPS
                        || Math.abs(max_line - min_line) < EPS
                        || Math.abs(max_geox - min_geox) < EPS
                        || Math.abs(max_geoy - min_geoy) < EPS) {
                    throw new DataSourceException("Degenerate in at least one dimension");
                }

                double pl_normalize[] = new double[6];
                double geo_normalize[] = new double[6];

                pl_normalize[0] = -min_pixel / (max_pixel - min_pixel);
                pl_normalize[1] = 1.0 / (max_pixel - min_pixel);
                pl_normalize[2] = 0.0;
                pl_normalize[3] = -min_line / (max_line - min_line);
                pl_normalize[4] = 0.0;
                pl_normalize[5] = 1.0 / (max_line - min_line);

                AffineTransform2D pl_normalize2 = new AffineTransform2D(pl_normalize[1], pl_normalize[2], pl_normalize[4], pl_normalize[5], pl_normalize[0], pl_normalize[3]);

                geo_normalize[0] = -min_geox / (max_geox - min_geox);
                geo_normalize[1] = 1.0 / (max_geox - min_geox);
                geo_normalize[2] = 0.0;
                geo_normalize[3] = -min_geoy / (max_geoy - min_geoy);
                geo_normalize[4] = 0.0;
                geo_normalize[5] = 1.0 / (max_geoy - min_geoy);

                AffineTransform2D geo_normalize2 = new AffineTransform2D(geo_normalize[1], geo_normalize[2], geo_normalize[4], geo_normalize[5], geo_normalize[0], geo_normalize[3]);


                /* -------------------------------------------------------------------- */
                /* In the general case, do a least squares error approximation by       */
                /* solving the equation Sum[(A - B*x + C*y - Lon)^2] = minimum          */
                /* -------------------------------------------------------------------- */

                double sum_x = 0.0;
                double sum_y = 0.0;
                double sum_xy = 0.0;
                double sum_xx = 0.0;
                double sum_yy = 0.0;
                double sum_Lon = 0.0;
                double sum_Lonx = 0.0;
                double sum_Lony = 0.0;
                double sum_Lat = 0.0;
                double sum_Latx = 0.0;
                double sum_Laty = 0.0;


                for (CalibrationPoint cp : calibrationPoints) {
                    DirectPosition2D pixelLine = new DirectPosition2D();

                    pl_normalize2.transform(cp.pixelLine, pixelLine);

                    double pixel = pixelLine.x;
                    double line = pixelLine.y;

                    DirectPosition2D xy = new DirectPosition2D();

                    geo_normalize2.transform((DirectPosition) cp.xy, xy);

                    double geox = xy.x;
                    double geoy = xy.y;

                    sum_x += pixel;
                    sum_y += line;
                    sum_xy += pixel * line;
                    sum_xx += pixel * pixel;
                    sum_yy += line * line;
                    sum_Lon += geox;
                    sum_Lonx += geox * pixel;
                    sum_Lony += geox * line;
                    sum_Lat += geoy;
                    sum_Latx += geoy * pixel;
                    sum_Laty += geoy * line;
                }

                double divisor =
                        nGCPCount * (sum_xx * sum_yy - sum_xy * sum_xy)
                                + 2 * sum_x * sum_y * sum_xy - sum_y * sum_y * sum_xx
                                - sum_x * sum_x * sum_yy;

                /* -------------------------------------------------------------------- */
                /*      If the divisor is zero, there is no valid solution.             */
                /* -------------------------------------------------------------------- */
                if (divisor == 0.0) {
                    throw new DataSourceException("Divisor is zero, there is no valid solution");
                }


                /* -------------------------------------------------------------------- */
                /*      Compute top/left origin.                                        */
                /* -------------------------------------------------------------------- */
                double gt_normalized[] = new double[]{0, 0, 0, 0, 0, 0};

                gt_normalized[0] = (sum_Lon * (sum_xx * sum_yy - sum_xy * sum_xy)
                        + sum_Lonx * (sum_y * sum_xy - sum_x * sum_yy)
                        + sum_Lony * (sum_x * sum_xy - sum_y * sum_xx))
                        / divisor;

                gt_normalized[3] = (sum_Lat * (sum_xx * sum_yy - sum_xy * sum_xy)
                        + sum_Latx * (sum_y * sum_xy - sum_x * sum_yy)
                        + sum_Laty * (sum_x * sum_xy - sum_y * sum_xx))
                        / divisor;

                /* -------------------------------------------------------------------- */
                /*      Compute X related coefficients.                                 */
                /* -------------------------------------------------------------------- */
                gt_normalized[1] = (sum_Lon * (sum_y * sum_xy - sum_x * sum_yy)
                        + sum_Lonx * (nGCPCount * sum_yy - sum_y * sum_y)
                        + sum_Lony * (sum_x * sum_y - sum_xy * nGCPCount))
                        / divisor;

                gt_normalized[2] = (sum_Lon * (sum_x * sum_xy - sum_y * sum_xx)
                        + sum_Lonx * (sum_x * sum_y - nGCPCount * sum_xy)
                        + sum_Lony * (nGCPCount * sum_xx - sum_x * sum_x))
                        / divisor;

                /* -------------------------------------------------------------------- */
                /*      Compute Y related coefficients.                                 */
                /* -------------------------------------------------------------------- */
                gt_normalized[4] = (sum_Lat * (sum_y * sum_xy - sum_x * sum_yy)
                        + sum_Latx * (nGCPCount * sum_yy - sum_y * sum_y)
                        + sum_Laty * (sum_x * sum_y - sum_xy * nGCPCount))
                        / divisor;

                gt_normalized[5] = (sum_Lat * (sum_x * sum_xy - sum_y * sum_xx)
                        + sum_Latx * (sum_x * sum_y - nGCPCount * sum_xy)
                        + sum_Laty * (nGCPCount * sum_xx - sum_x * sum_x))
                        / divisor;

                AffineTransform2D gt_normalized2 = new AffineTransform2D(gt_normalized[1], gt_normalized[2], 0, 0, gt_normalized[0], gt_normalized[3]);

                /* -------------------------------------------------------------------- */
                /*      Compose the resulting transformation with the normalization     */
                /*      geotransformations.                                             */
                /* -------------------------------------------------------------------- */
                MathTransform2D inv_geo_normalize2 = geo_normalize2.inverse();
                MathTransform gt1p2 = ConcatenatedTransform.create(pl_normalize2, gt_normalized2);

                grid2Crs = ConcatenatedTransform.create(gt1p2, inv_geo_normalize2);
            }
        } catch (IOException | FactoryException | TransformException e) {
            throw new DataSourceException(e);
        }
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

    private String parseHeader(List<String> lines) {
        return lines.get(0);
    }

    private String parseImageFilename(List<String> lines) {
        return lines.get(2);
    }

    private GeodeticDatum parseDatum(List<String> lines) throws DataSourceException {
        String[] values = lineValues(lines.get(4));

        String datumName = values[0];

        GeodeticDatum datum = DATUMS.get(datumName);

        if (datum == null) {
            throw new DataSourceException("Unknown datum: " + datumName);
        }

        return datum;
    }

    private String parseMapProjection(List<String> lines) throws DataSourceException {
        String projectionName = null;

        for (String line : lines) {
            if (StringUtils.startsWith(line, "Map Projection")) {
                String[] values = lineValues(line);

                if (values.length < 2) {
                    throw new DataSourceException("Not enough data");
                }

                projectionName = values[1];
            }
        }

        return projectionName;
    }

    private CoordinateReferenceSystem parseProjectionSetup(List<String> lines, String projectionName, GeographicCRS geoCrs) throws DataSourceException, FactoryException {
        CoordinateReferenceSystem crs;

        //  Parameters:
        //    1. Latitude Origin
        //    2. Longitude Origin
        //    3. K Factor
        //    4. False Easting
        //    5. False Northing
        //    6. Latitude 1
        //    7. Latitude 2
        //    8. Height - used in the Vertical Near-Sided Perspective Projection
        //    9. Sat - not used
        //    10. Path - not used

        if ("Latitude/Longitude".equals(projectionName)) {
            crs = geoCrs;
        } else {
            String[] values = null;

            for (String line : lines) {
                if (StringUtils.startsWith(line, "Projection Setup")) {
                    values = lineValues(line);
                }
            }

            if (values == null) {
                throw new DataSourceException("'Projection Setup' is required");
            }

            Conversion conversion = createConversion(projectionName, values);

            crs = ReferencingFactoryFinder.getCRSFactory(null).createProjectedCRS(Collections.singletonMap("name", "unnamed"), geoCrs, conversion, DefaultCartesianCS.PROJECTED);
        }

        return crs;
    }

    private List<CalibrationPoint> parseCalibrationPoints(List<String> lines, MathTransform world2Crs) throws TransformException {
        List<CalibrationPoint> calibrationPoints = new ArrayList<>();

        for (String line : lines) {
            if (!StringUtils.startsWith(line, "Point")) {
                continue;
            }

            String[] values = lineValues(line);

            String v0 = values[0];
            String v2 = values.length > 2 ? values[2] : "";
            String v3 = values.length > 3 ? values[3] : "";
            String v6 = values.length > 6 ? values[6] : "";
            String v7 = values.length > 7 ? values[7] : "";
            String v8 = values.length > 8 ? values[8] : "";
            String v9 = values.length > 9 ? values[9] : "";
            String v10 = values.length > 10 ? values[10] : "";
            String v11 = values.length > 11 ? values[11] : "";
            String v14 = values.length > 14 ? values[14] : "";
            String v15 = values.length > 15 ? values[15] : "";

            if (v0.startsWith("Point")) {
                Point pixelLine = null;

                if (NumberUtils.isCreatable(v2) && NumberUtils.isCreatable(v3)) {
                    pixelLine = new Point(NumberUtils.toInt(v2), NumberUtils.toInt(v3));
                }

                DirectPosition2D xy = null;

                if (NumberUtils.isCreatable(v6) && NumberUtils.isCreatable(v7) &&
                        NumberUtils.isCreatable(v9) && NumberUtils.isCreatable(v10)) {
                    DirectPosition2D latLon = new DirectPosition2D(DefaultGeographicCRS.WGS84,
                            NumberUtils.toDouble(v9) + NumberUtils.toDouble(v10) / 60.0,
                            NumberUtils.toDouble(v6) + NumberUtils.toDouble(v7) / 60.0);

                    if ("W".equals(v11)) {
                        latLon.x = -latLon.x;
                    }

                    if ("S".equals(v8)) {
                        latLon.y = -latLon.y;
                    }

                    DirectPosition2D p = new DirectPosition2D(crs);

                    MapProjection.SKIP_SANITY_CHECKS = true;

                    if (world2Crs.transform(latLon, p) != null) {
                        xy = p;
                    }
                } else if (NumberUtils.isCreatable(v14) && NumberUtils.isCreatable(v15)) {
                    xy = new DirectPosition2D(crs, NumberUtils.toDouble(v14), NumberUtils.toDouble(v15));
                }

                if (pixelLine != null && xy != null) {
                    calibrationPoints.add(new CalibrationPoint(pixelLine, xy));
                }
            }
        }

        return calibrationPoints;
    }

    private static GeodeticDatum createGeodeticDatum(String name, Ellipsoid ellipsoid, double dx, double dy, double dz) throws FactoryException {
        Map<String, Object> parameters = new HashMap<>();

        parameters.put("name", name);

        final BursaWolfParameters bursaWolfParameters = new BursaWolfParameters(DefaultGeodeticDatum.WGS84);

        bursaWolfParameters.dx = dx;
        bursaWolfParameters.dy = dy;
        bursaWolfParameters.dz = dz;

        if (!bursaWolfParameters.isIdentity()) {
            parameters.put(DefaultGeodeticDatum.BURSA_WOLF_KEY, bursaWolfParameters);
        }

        DatumFactory datumFactory = ReferencingFactoryFinder.getDatumFactory(null);

        return datumFactory.createGeodeticDatum(parameters, ellipsoid, DefaultPrimeMeridian.GREENWICH);
    }

    private static final class CalibrationPoint {
        private final Point pixelLine;
        private final Point.Double xy;

        public CalibrationPoint(Point pixelLine, Point.Double xy) {
            this.pixelLine = pixelLine;
            this.xy = xy;
        }

        public Point getPixelLine() {
            return pixelLine;
        }

        public Point.Double getXy() {
            return xy;
        }
    }

    private static String[] lineValues(String line) {
        return Arrays.stream(line.split(",", -1)).map(String::trim).toArray(String[]::new);
    }

    // нужно замутить универсальную функцию, которая мапит параметры на геотулс имена
    // http://docs.geotools.org/latest/userguide/library/referencing/transform.html
    private static Conversion createConversion(String projectionName, String[] values) throws DataSourceException, NoSuchIdentifierException {
        final String methodName = OZI_PROJECTION_NAME_TO_GEOTOOLS.get(projectionName);

        DefaultMathTransformFactory mathTransformFactory = new DefaultMathTransformFactory();
        ParameterValueGroup parameters = mathTransformFactory.getDefaultParameters(methodName);

        if (values.length < 6) {
            throw new DataSourceException("Not enough data");
        }

        if (!"Sinusoidal".equals(projectionName)) {
            parameters.parameter("latitude_of_origin").setValue(NumberUtils.toDouble(values[1]));
        }

        parameters.parameter("central_meridian").setValue(NumberUtils.toDouble(values[2]));
        parameters.parameter("false_easting").setValue(NumberUtils.toDouble(values[4]));
        parameters.parameter("false_northing").setValue(NumberUtils.toDouble(values[5]));

        if (("Mercator".equals(projectionName) || "Transverse Mercator".equals(projectionName)) && NumberUtils.isCreatable(values[3])) {
            parameters.parameter("scale_factor").setValue(NumberUtils.toDouble(values[3]));
        } else if ("Lambert Conformal Conic".equals(projectionName) || "Albers Equal Area".equals(projectionName)) {
            if (values.length > 6 && NumberUtils.isCreatable(values[6])) {
                parameters.parameter("standard_parallel_1").setValue(NumberUtils.toDouble(values[6]));
            }

            if (values.length > 7 && NumberUtils.isCreatable(values[7])) {
                parameters.parameter("standard_parallel_2").setValue(NumberUtils.toDouble(values[7]));
            }
        }

        return new DefiningConversion(methodName, parameters);
    }
}
