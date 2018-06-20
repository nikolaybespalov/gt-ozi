package com.github.nikolaybespalov.gtoziexplorermap;

import org.geotools.geometry.Envelope2D;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
//    private final String imageFile;
//    private final OziDatum datum;

    public OziMapFile(Path path) {
        try {
            String[] lines = Files.lines(path).toArray(String[]::new);

            if (lines.length < MAX_LINES) {
                throw new IllegalArgumentException(MESSAGES.format("too.few.lines", path));
            }

            if (!lines[HEADER_AND_VERSION_LINE_INDEX].startsWith(HEADER)) {
                throw new IllegalArgumentException(MESSAGES.format("is.not.a.map.file", path));
            }

            String[] datumParameters = lines[DATUM_LINE_INDEX].split(",");

            if (datumParameters.length < DATUM_PARAMETERS) {
                throw new IllegalArgumentException(MESSAGES.format("too.few.datum.parameters", path));
            }

            String datumName = datumParameters[DATUM_NAME_INDEX];
            int epsgCode = Integer.parseInt(datumParameters[DATUM_EPSG_CODE_INDEX]);
            int deltaX = Integer.parseInt(datumParameters[DATUM_DELTA_X_INDEX]);
            int deltaY = Integer.parseInt(datumParameters[DATUM_DELTA_Y_INDEX]);
            int deltaZ = Integer.parseInt(datumParameters[DATUM_DELTA_Z_INDEX]);
            String ellipsoidName = datumParameters[DATUM_ELLIPSOID_NAME_INDEX];

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

        } catch (IOException e) {
            int asd = 0;
            int asdf = asd;
        }
    }
}
