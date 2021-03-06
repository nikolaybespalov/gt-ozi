package com.github.nikolaybespalov.gtozi;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.geotools.data.DataSourceException;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.datum.BursaWolfParameters;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.geotools.referencing.operation.DefiningConversion;
import org.geotools.referencing.operation.projection.MapProjection;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.opengis.geometry.DirectPosition;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.crs.ProjectedCRS;
import org.opengis.referencing.cs.EllipsoidalCS;
import org.opengis.referencing.datum.DatumFactory;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.*;

import javax.measure.unit.SI;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;

import static org.geotools.util.logging.Logging.getLogger;
import static org.opengis.referencing.IdentifiedObject.NAME_KEY;

@SuppressWarnings("WeakerAccess")
final class OziMapFileReader {
    private static final Logger LOGGER = getLogger(OziMapFormat.class);

    public static final Map<String, String> OZI_PROJECTION_NAME_TO_GEOTOOLS = new HashMap<>();
    private static final Map<String, Ellipsoid> ELLIPS = new HashMap<>();
    private static final Map<String, GeodeticDatum> DATUMS = new HashMap<>();

    static {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    static {
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("Mercator", "Mercator_1SP");
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("Transverse Mercator", "Transverse_Mercator");
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("(UTM) Universal Transverse Mercator", "Transverse_Mercator");
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("Lambert Conformal Conic", "Lambert_Conformal_Conic_2SP");
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("Sinusoidal", "Sinusoidal");
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("Albers Equal Area", "Albers_Conic_Equal_Area");
        OZI_PROJECTION_NAME_TO_GEOTOOLS.put("Van Der Grinten", "Van_der_Grinten_I");


        //
        // Read ozi_datum.csv and ozi_ellips.csv
        //

        InputStream oziEllipsIs = OziMapFileReader.class.getClassLoader().getResourceAsStream("com/github/nikolaybespalov/gtozi/data/ozi_ellips.csv");

        if (oziEllipsIs == null) {
            throw new ExceptionInInitializerError("ozi_ellips.csv not found");
        }

        try (CSVParser ellipsParser = new CSVParser(new InputStreamReader(oziEllipsIs), CSVFormat.RFC4180.withFirstRecordAsHeader().withCommentMarker('#'))) {
            for (CSVRecord ellipsRecord : ellipsParser) {
                String ellipsoidCode = ellipsRecord.get("ELLIPSOID_CODE");
                String name = ellipsRecord.get("NAME");
                String a = ellipsRecord.get("A");
                String invf = ellipsRecord.get("INVF");

                Ellipsoid ellipsoid = DefaultEllipsoid.createFlattenedSphere(name, NumberUtils.toDouble(a), NumberUtils.toDouble(invf), SI.METER);

                ELLIPS.put(ellipsoidCode, ellipsoid);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }


        InputStream oziDatumIs = OziMapFileReader.class.getClassLoader().getResourceAsStream("com/github/nikolaybespalov/gtozi/data/ozi_datum.csv");

        if (oziDatumIs == null) {
            throw new ExceptionInInitializerError("ozi_datum.csv not found");
        }

        try (CSVParser datumParser = new CSVParser(new InputStreamReader(oziDatumIs), CSVFormat.RFC4180.withFirstRecordAsHeader().withCommentMarker('#').withTrim())) {
            for (CSVRecord datumRecord : datumParser) {
                String name = datumRecord.get("NAME");
                String epsgDatumCode = datumRecord.get("EPSG_DATUM_CODE");
                String ellipsoidCode = datumRecord.get("ELLIPSOID_CODE");
                String dx = datumRecord.get("DELTAX");
                String dy = datumRecord.get("DELTAY");
                String dz = datumRecord.get("DELTAZ");

                // CSV Parser cannot process a comment that does not start from the beginning.
                // Need to trim tail of dz.
                if (dz.indexOf('#') != -1) {
                    dz = dz.substring(0, dz.indexOf('#')).trim();
                }

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

    private String title;
    private CoordinateReferenceSystem crs;
    private MathTransform grid2Crs;
    private File rasterFile;

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
            // Parse and validate file header
            //

            title = parseTitle(lines);

            if (StringUtils.isEmpty(title)) {
                LOGGER.warning("File " + file.getAbsolutePath() + " does not contain a title.");

                title = FilenameUtils.removeExtension(file.getName());
            }

            //
            // Parse and validate raster filename
            //

            String rasterFilename = parseRasterFilename(lines);

            if (StringUtils.isEmpty(rasterFilename)) {
                throw new DataSourceException("Map file does not contain an raster filename");
            }

            rasterFile = new File(rasterFilename);

            if (!rasterFile.exists()) {
                rasterFile = new File(file.getParent(), rasterFile.getName());
            }

            if (!Files.isReadable(rasterFile.toPath())) {
                throw new DataSourceException("Raster file " + rasterFile.getAbsolutePath() + " can not be read.");
            }

            //
            // Parse datum
            //

            String datumName = parseDatum(lines);

            //
            // Parse projection
            //

            String projectionName = parseMapProjection(lines);

            //
            // Parse 'Projection Setup'
            //

            String[] projectionSetup = parseProjectionSetup(lines);

            GeographicCRS geoCrs = createGeoCrs(datumName);

            crs = createCrs(lines, projectionName, projectionSetup, geoCrs);

            MathTransform world2Crs = CRS.findMathTransform(geoCrs, crs, true);

            List<CalibrationPoint> calibrationPoints = parseCalibrationPoints(lines, world2Crs);

            grid2Crs = createGrid2Crs(calibrationPoints);
        } catch (DataSourceException e) {
            throw e;
        } catch (IOException | FactoryException | TransformException e) {
            throw new DataSourceException(e);
        }
    }

    public String getTitle() {
        return title;
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return crs;
    }

    public MathTransform getGrid2Crs() {
        return grid2Crs;
    }

    public File getRasterFile() {
        return rasterFile;
    }

    private String parseHeader(List<String> lines) {
        return lines.get(0);
    }

    private String parseTitle(List<String> lines) {
        return lines.get(1);
    }

    private String parseRasterFilename(List<String> lines) {
        return lines.get(2);
    }

    private String parseDatum(List<String> lines) throws DataSourceException {
        if (lines.size() < 5) {
            throw new DataSourceException("Not enough data");
        }

        String[] values = lineValues(lines.get(4));

        return values[0];
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

    private String[] parseProjectionSetup(List<String> lines) throws DataSourceException {
        String[] values = null;

        for (String line : lines) {
            if (StringUtils.startsWith(line, "Projection Setup")) {
                values = lineValues(line);
            }
        }

        if (values == null) {
            throw new DataSourceException("'Projection Setup' not found");
        }

        return values;
    }

    private GeographicCRS createGeoCrs(String datumName) throws FactoryException {
        CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);

        Map<String, Object> geoCsProperties = new HashMap<>();

        geoCsProperties.put(NAME_KEY, datumName);

        GeodeticDatum geodeticDatum = DATUMS.get(datumName);

        EllipsoidalCS ellipsoidalCS = new DefaultEllipsoidalCS("",
                DefaultCoordinateSystemAxis.LONGITUDE,
                DefaultCoordinateSystemAxis.LATITUDE);

        return crsFactory.createGeographicCRS(geoCsProperties, geodeticDatum, ellipsoidalCS);
    }

    private CoordinateReferenceSystem createCrs(List<String> lines, String projectionName, String[] projectionSetup, GeographicCRS geoCrs) throws DataSourceException, FactoryException {
//        MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);
//        CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);

        if ("Latitude/Longitude".equals(projectionName)) {
            return geoCrs;
        } else if ("Mercator".equals(projectionName)) {
            return createProjectedCrs("unnamed", projectionName, projectionSetup, geoCrs);
        } else if ("Transverse Mercator".equals(projectionName)) {
            return createProjectedCrs("unnamed", projectionName, projectionSetup, geoCrs);
        } else if ("(UTM) Universal Transverse Mercator".equals(projectionName)) {
            int zone = -1;
            boolean north = true;

            // Try to guess the UTM zone
            for (String line : lines) {
                if (StringUtils.startsWith(line, "Point")) {
                    String[] values = lineValues(line);

                    if (values.length < 17 || StringUtils.isEmpty(values[13]) || StringUtils.isEmpty(values[16])) {
                        continue;
                    }

                    zone = NumberUtils.toInt(values[13], -1);
                    north = "N".equals(values[16]);
                    break;
                }
            }

            if (zone == -1) {
                throw new DataSourceException("Failed to guess UTM zone");
            }

            if (!north) {
                throw new DataSourceException("Southern hemisphere UTM are not supported. Sorry. Contact me, please");
            }

            String projCsName = "UTM Zone " + zone + ", Northern Hemisphere";

            return createProjectedCrs(projCsName, projectionName, new String[]{"", "0", String.valueOf(zone * 6 - 183), "0.9996", "500000.0", ""}, geoCrs);
        } else {
            throw new DataSourceException("Unsupported projection: " + projectionName);
        }
    }

    private ProjectedCRS createProjectedCrs(String name, String projectionName, String[] projectionSetup, GeographicCRS geoCrs) throws FactoryException {
        //  Projection Setup:
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

        MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);
        CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);

        String geotoolsProjectionName = OZI_PROJECTION_NAME_TO_GEOTOOLS.get(projectionName);

        ParameterValueGroup projectionParameters = mtFactory.getDefaultParameters(geotoolsProjectionName);

        if (NumberUtils.isCreatable(projectionSetup[1])) {
            projectionParameters.parameter("latitude_of_origin").setValue(NumberUtils.toDouble(projectionSetup[1]));
        }

        if (NumberUtils.isCreatable(projectionSetup[2])) {
            projectionParameters.parameter("central_meridian").setValue(NumberUtils.toDouble(projectionSetup[2]));
        }

        double scaleFactor = NumberUtils.toDouble(projectionSetup[3], 1.0);
        projectionParameters.parameter("scale_factor").setValue(scaleFactor);

        if (NumberUtils.isCreatable(projectionSetup[4])) {
            projectionParameters.parameter("false_easting").setValue(NumberUtils.toDouble(projectionSetup[4]));
        }

        if (NumberUtils.isCreatable(projectionSetup[5])) {
            projectionParameters.parameter("false_northing").setValue(NumberUtils.toDouble(projectionSetup[5]));
        }

        Map<String, Object> projCsProperties = new HashMap<>();

        projCsProperties.put(NAME_KEY, name);

        Conversion conversion = new DefiningConversion(geotoolsProjectionName, projectionParameters);

        return crsFactory.createProjectedCRS(projCsProperties, geoCrs, conversion, DefaultCartesianCS.GENERIC_2D);
    }

    private List<CalibrationPoint> parseCalibrationPoints(List<String> lines, MathTransform world2Crs) throws TransformException {
        // calc overlap 180
        int mpli = 0;
        double[] mpl = new double[4];

        for (String line : lines) {
            if (!StringUtils.startsWith(line, "MMPLL")) {
                continue;
            }

            String[] values = lineValues(line);

            if (values.length < 4) {
                continue;
            }

            mpl[mpli++] = NumberUtils.toDouble(values[2]);
        }

        boolean asd = false;

        if (mpli != 0 && mpl[1] < mpl[0] && mpl[2] < mpl[3]) {
            asd = true;
        }

        List<CalibrationPoint> calibrationPoints = new ArrayList<>();

        for (String line : lines) {
            if (!StringUtils.startsWith(line, "Point")) {
                continue;
            }

            String[] values = lineValues(line);

            if (values.length < 16) {
                continue;
            }

            String v2 = values[2];
            String v3 = values[3];
            String v6 = values[6];
            String v7 = values[7];
            String v8 = values[8];
            String v9 = values[9];
            String v10 = values[10];
            String v11 = values[11];
            String v14 = values[14];
            String v15 = values[15];

            Point pixelLine;

            if (NumberUtils.isCreatable(v2) && NumberUtils.isCreatable(v3)) {
                pixelLine = new Point(NumberUtils.toInt(v2), NumberUtils.toInt(v3));
            } else {
                continue;
            }

            DirectPosition2D xy = new DirectPosition2D();
            DirectPosition2D latLon;

            if (NumberUtils.isCreatable(v6) && NumberUtils.isCreatable(v7) &&
                    NumberUtils.isCreatable(v9) && NumberUtils.isCreatable(v10)) {
                latLon = new DirectPosition2D(DefaultGeographicCRS.WGS84,
                        NumberUtils.toDouble(v9) + NumberUtils.toDouble(v10) / 60.0,
                        NumberUtils.toDouble(v6) + NumberUtils.toDouble(v7) / 60.0);

                if ("W".equals(v11)) {
                    latLon.x = -latLon.x;

                    if (asd) {
                        latLon.x = 360 + latLon.x;
                    }
                }

                if ("S".equals(v8)) {
                    latLon.y = -latLon.y;
                }

                MapProjection.SKIP_SANITY_CHECKS = true;

                world2Crs.transform(latLon, xy);
            } else if (NumberUtils.isCreatable(v14) && NumberUtils.isCreatable(v15)) {
                xy = new DirectPosition2D(crs, NumberUtils.toDouble(v14), NumberUtils.toDouble(v15));
            } else {
                continue;
            }

            calibrationPoints.add(new CalibrationPoint(pixelLine, xy));
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

        parameters.put(DefaultGeodeticDatum.BURSA_WOLF_KEY, bursaWolfParameters);

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
    }

    /**
     * Parse line values.
     *
     * @param line line of .MAP file
     * @return non empty array
     */
    private static String[] lineValues(String line) {
        return Arrays.stream(line.split(",", -1)).map(String::trim).toArray(String[]::new);
    }

    private MathTransform createGrid2Crs(List<CalibrationPoint> calibrationPoints) throws DataSourceException, NoninvertibleTransformException {
        if (calibrationPoints.size() < 2) {
            throw new DataSourceException("Too few calibration points!");
        }

        if (calibrationPoints.size() == 2) {
            CalibrationPoint cp1 = calibrationPoints.get(calibrationPoints.size() - 1);
            CalibrationPoint cp0 = calibrationPoints.get(0);

            double xPixelSize = (cp1.xy.x - cp0.xy.x) / (double) (cp1.pixelLine.x - cp0.pixelLine.x);
            double yPixelSize = (cp1.xy.y - cp0.xy.y) / (double) (cp1.pixelLine.y - cp0.pixelLine.y);
            double xULC = cp0.xy.x - (double) cp0.pixelLine.x * xPixelSize;
            double yULC = cp0.xy.y - (double) cp0.pixelLine.y * yPixelSize;

            return new AffineTransform2D(xPixelSize, 0, 0, yPixelSize, xULC, yULC);
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

            AffineTransform2D plNormalize = new AffineTransform2D(1.0 / (max_pixel - min_pixel), 0.0, 0.0, 1.0 / (max_line - min_line), -min_pixel / (max_pixel - min_pixel), -min_line / (max_line - min_line));

            AffineTransform2D geoNormalize = new AffineTransform2D(1.0 / (max_geox - min_geox), 0.0, 0.0, 1.0 / (max_geoy - min_geoy), -min_geox / (max_geox - min_geox), -min_geoy / (max_geoy - min_geoy));

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

                plNormalize.transform(cp.pixelLine, pixelLine);

                double pixel = pixelLine.x;
                double line = pixelLine.y;

                DirectPosition2D xy = new DirectPosition2D();

                geoNormalize.transform((DirectPosition) cp.xy, xy);

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
            double[] gt_normalized = new double[]{0, 0, 0, 0, 0, 0};

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

            AffineTransform2D gt_normalized2 = new AffineTransform2D(gt_normalized[1], gt_normalized[2], gt_normalized[4], gt_normalized[5], gt_normalized[0], gt_normalized[3]);

            /* -------------------------------------------------------------------- */
            /*      Compose the resulting transformation with the normalization     */
            /*      geotransformations.                                             */
            /* -------------------------------------------------------------------- */
            MathTransform2D inv_geo_normalize2 = geoNormalize.inverse();
            MathTransform gt1p2 = ConcatenatedTransform.create(plNormalize, gt_normalized2);

            return ConcatenatedTransform.create(gt1p2, inv_geo_normalize2);
        }
    }
}
