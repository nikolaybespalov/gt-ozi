package com.github.nikolaybespalov.gtoziexplorermap;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class OziMapFile {
    private static Messages MESSAGES = new Messages("messages");
    private static final int MAX_LINES = 30;
    private static final int HEADER_AND_VERSION_LINE_INDEX = 0;
    private static final int DATUM_LINE_INDEX = 4;
    private static final int DATUM_NAME_INDEX = 0;
    private static final int DATUM_EPSG_CODE_INDEX = 1;
    private static final int DATUM_DELTA_X_INDEX = 2;
    private static final int DATUM_DELTA_Y_INDEX = 3;
    private static final int DATUM_DELTA_Z_INDEX = 4;
    private static final int DATUM_ELLIPSOID_NAME_INDEX = 5;
    private static final int DATUM_PARAMETERS = 5;
    private static final String HEADER = "OziExplorer Map Data File";
    private String version;
    private OziProjection projection;
    private OziProjectionSetup projectionSetup;
//    private final String imageFile;
//    private final OziDatum datum;

    public OziMapFile(Path path) {

        try (Stream<String> lines = Files.lines(path, Charset.forName("windows-1251"))) {

//            if (lines.count() < MAX_LINES) {
//                throw new IllegalArgumentException(MESSAGES.format("too.few.lines", path));
//            }

            lines.skip(5).forEach(line -> {
                if (line.startsWith("Map Projection")) {
                    String[] mapProjectionParameters = line.split(",");

                    if (mapProjectionParameters.length < 2) {
                        return;
                    }

                    String name = mapProjectionParameters[1];

                    if (StringUtils.isEmpty(name)) {
                        return;
                    }

                    projection = new OziProjection(name);

                    for (int i = 2; i < mapProjectionParameters.length; i += 2) {
                        String key = mapProjectionParameters[i];
                        String value = mapProjectionParameters[i + 1];

                        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
                            continue;
                        }

                        switch (key) {
                            case "PolyCal": {
                                Boolean b = BooleanUtils.toBooleanObject(value, "Yes", "No", "");

                                if (b != null) {
                                    projection.setPolyCal(b);
                                }

                                break;
                            }
                            case "AutoCalOnly": {
                                Boolean b = BooleanUtils.toBooleanObject(value, "Yes", "No", "");

                                if (b != null) {
                                    projection.setAutoCalOnly(b);
                                }
                                break;
                            }
                            case "BSBUseWPX": {
                                Boolean b = BooleanUtils.toBooleanObject(value, "Yes", "No", "");

                                if (b != null) {
                                    projection.setBsbUseWpx(b);
                                }
                                break;
                            }
                        }
                    }
                } else if (line.startsWith("Projection Setup")) {
                    projectionSetup = new OziProjectionSetup();

                    String[] projectionSetupParameters = line.split(",");

                    if (projectionSetupParameters.length < 1) {
                        return;
                    }

                    for (int i = 1; i < projectionSetupParameters.length; ++i) {
                        String value = projectionSetupParameters[i];

                        if (StringUtils.isEmpty(value)) {
                            continue;
                        }

                        switch (i) {
                            case 1:
                                if (NumberUtils.isCreatable(value)) {
                                    projectionSetup.setLatitudeOrigin(NumberUtils.toDouble(value));
                                }
                                break;
                            case 2:
                                if (NumberUtils.isCreatable(value)) {
                                    projectionSetup.setLongitudeOrigin(NumberUtils.toDouble(value));
                                }
                                break;
                        }
                    }

                }
            });
        } catch (IOException e) {
            throw new IllegalArgumentException("lines2");
        }


//        if (!lines[HEADER_AND_VERSION_LINE_INDEX].startsWith(HEADER)) {
//            throw new IllegalArgumentException(MESSAGES.format("is.not.a.map.file", path));
//        }
//
//        String[] datumParameters = lines[DATUM_LINE_INDEX].split(",");
//
//        if (datumParameters.length < DATUM_PARAMETERS) {
//            throw new IllegalArgumentException(MESSAGES.format("too.few.datum.parameters", path));
//        }
//
//        String datumName = datumParameters[DATUM_NAME_INDEX];
//        int epsgCode = Integer.parseInt(datumParameters[DATUM_EPSG_CODE_INDEX]);
//        int deltaX = Integer.parseInt(datumParameters[DATUM_DELTA_X_INDEX]);
//        int deltaY = Integer.parseInt(datumParameters[DATUM_DELTA_Y_INDEX]);
//        int deltaZ = Integer.parseInt(datumParameters[DATUM_DELTA_Z_INDEX]);
//        String ellipsoidName = datumParameters[DATUM_ELLIPSOID_NAME_INDEX];

        //OziDatum.valueOf(datumName);

        //new OziDatum(datumName, epsgCode, OziEllipsoid.valueOf(ellipsoidName), deltaX, deltaY, deltaZ);
        // if mercator
        // poSRS->SetMercator( OGR_FP( NatOriginLat ), OGR_FP( NatOriginLong ),
        //                            OGR_FP( NatOriginScaleFactor ),
        //                            OGR_FP( FalseEasting ), OGR_FP( FalseNorthing ) );

        //if( EQUAL( pszProjName, "Mercator" ) )
        //    {
        //        oSRS.SetMercator( dfCenterLat, dfCenterLong, dfScale, dfFalseEasting, dfFalseNorthing );
        //    }
        //    else if( EQUAL( pszProjName, "Transverse Mercator" ) )
        //    {
        //        oSRS.SetTM( dfCenterLat, dfCenterLong, dfScale, dfFalseEasting, dfFalseNorthing );
        //    }

        //DefaultGeographicCRS.WGS84;
        //Envelope2D()

        //OziProjection projection;


        if (projection == null) {
            throw new IllegalArgumentException("asdasd");
        }


    }

    public OziProjection getProjection() {
        return projection;
    }

    public OziProjectionSetup getProjectionSetup() {
        return projectionSetup;
    }
}
