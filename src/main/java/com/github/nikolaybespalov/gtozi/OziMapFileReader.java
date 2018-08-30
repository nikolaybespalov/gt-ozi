package com.github.nikolaybespalov.gtozi;

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
import java.util.*;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class OziMapFileReader {
    private static final Ellipsoid AIRY_1830 = DefaultEllipsoid.createFlattenedSphere("Airy 1830", 6377563.396, 299.3249646, SI.METER);
    private static final Ellipsoid MODIFIED_AIRY = DefaultEllipsoid.createFlattenedSphere("Modified Airy", 6377340.189, 299.3249646, SI.METER);
    private static final Ellipsoid AUSTRALIAN_NATIONAL = DefaultEllipsoid.createFlattenedSphere("Australian National", 6378160.0, 298.25, SI.METER);
    private static final Ellipsoid BESSEL_1841 = DefaultEllipsoid.createFlattenedSphere("Bessel 1841", 6377397.155, 299.1528128, SI.METER);
    private static final Ellipsoid CLARKE_1866 = DefaultEllipsoid.createFlattenedSphere("Clarke 1866", 6378206.4, 294.9786982, SI.METER);
    private static final Ellipsoid CLARKE_1880 = DefaultEllipsoid.createFlattenedSphere("Clarke 1880", 6378249.145, 293.465, SI.METER);
    private static final Ellipsoid EVEREST_INDIA_1830 = DefaultEllipsoid.createFlattenedSphere("Everest (India 1830)", 6377276.345, 300.8017, SI.METER);
    private static final Ellipsoid EVEREST_1948 = DefaultEllipsoid.createFlattenedSphere("Everest (1948)", 6377304.063, 300.8017, SI.METER);
    private static final Ellipsoid MODIFIED_FISCHER_1960 = DefaultEllipsoid.createFlattenedSphere("Modified Fischer 1960", 6378155.0, 298.3, SI.METER);
    @SuppressWarnings("unused")
    private static final Ellipsoid EVEREST_PAKISTAN = DefaultEllipsoid.createFlattenedSphere("Everest (Pakistan)", 6377309.613, 300.8017, SI.METER);
    @SuppressWarnings("unused")
    private static final Ellipsoid INDONESIAN_1974 = DefaultEllipsoid.createFlattenedSphere("Indonesian 1974", 6378160.0, 298.247, SI.METER);
    private static final Ellipsoid GRS_80 = DefaultEllipsoid.createFlattenedSphere("GRS 80", 6378137.0, 298.257222101, SI.METER);
    private static final Ellipsoid HELMERT_1906 = DefaultEllipsoid.createFlattenedSphere("Helmert 1906", 6378200.0, 298.3, SI.METER);
    private static final Ellipsoid HOUGH_1960 = DefaultEllipsoid.createFlattenedSphere("Hough 1960", 6378270.0, 297.0, SI.METER);
    private static final Ellipsoid INTERNATIONAL_1924 = DefaultEllipsoid.createFlattenedSphere("International 1924", 6378388.0, 297.0, SI.METER);
    private static final Ellipsoid KRASSOVSKY_1940 = DefaultEllipsoid.createFlattenedSphere("Krassovsky 1940", 6378245.0, 298.3, SI.METER);
    private static final Ellipsoid SOUTH_AMERICAN_1969 = DefaultEllipsoid.createFlattenedSphere("South American 1969", 6378160.0, 298.25, SI.METER);
    @SuppressWarnings("unused")
    private static final Ellipsoid EVEREST_MALAYSIA_1969 = DefaultEllipsoid.createFlattenedSphere("Everest (Malaysia 1969)", 6377295.664, 300.8017, SI.METER);
    @SuppressWarnings("unused")
    private static final Ellipsoid EVEREST_SABAH_SARAWAK = DefaultEllipsoid.createFlattenedSphere("Everest (Sabah Sarawak)", 6377298.556, 300.8017, SI.METER);
    private static final Ellipsoid WGS_72 = DefaultEllipsoid.createFlattenedSphere("WGS 72", 6378135.0, 298.26, SI.METER);
    private static final Ellipsoid WGS_84 = DefaultEllipsoid.WGS84;
    private static final Ellipsoid BESSEL_1841_NAMIBIA = DefaultEllipsoid.createFlattenedSphere("Bessel 1841 (Namibia)", 6377483.865, 299.1528128, SI.METER);
    @SuppressWarnings("unused")
    private static final Ellipsoid EVEREST_INDIA_1956 = DefaultEllipsoid.createFlattenedSphere("Everest (India 1956)", 6377301.243, 300.8017, SI.METER);
    private static final Ellipsoid CLARKE_1880_PALESTINE = DefaultEllipsoid.createFlattenedSphere("Clarke 1880 Palestine", 6378300.789, 293.466, SI.METER);
    private static final Ellipsoid CLARKE_1880_IGN = DefaultEllipsoid.createFlattenedSphere("Clarke 1880 IGN", 6378249.2, 293.466021, SI.METER);
    @SuppressWarnings("unused")
    private static final Ellipsoid HAYFORD_1909 = DefaultEllipsoid.createFlattenedSphere("Hayford 1909", 6378388.0, 296.959263, SI.METER);
    @SuppressWarnings("unused")
    private static final Ellipsoid CLARKE_1858 = DefaultEllipsoid.createFlattenedSphere("Clarke 1858", 6378350.87, 294.26, SI.METER);
    private static final Ellipsoid BESSEL_1841_NORWAY = DefaultEllipsoid.createFlattenedSphere("Bessel 1841 (Norway)", 6377492.0176, 299.1528, SI.METER);
    @SuppressWarnings("unused")
    private static final Ellipsoid PLESSIS_1817_FRANCE = DefaultEllipsoid.createFlattenedSphere("Plessis 1817 (France)", 6376523.0, 308.6409971, SI.METER);
    @SuppressWarnings("unused")
    private static final Ellipsoid HAYFORD_1924 = DefaultEllipsoid.createFlattenedSphere("Hayford 1924", 6378388.0, 297.0, SI.METER);

    private static final Map<String, GeodeticDatum> DATUMS = new HashMap<String, GeodeticDatum>() {{
        put("Adindan", createGeodeticDatum("Adindan", CLARKE_1880, -162, -12, 206));
        put("Afgooye", createGeodeticDatum("Afgooye", KRASSOVSKY_1940, -43, -163, 45));
        put("Ain el Abd 1970", createGeodeticDatum("Ain el Abd 1970", INTERNATIONAL_1924, -150, -251, -2));
        put("Anna 1 Astro 1965", createGeodeticDatum("Anna 1 Astro 1965", AUSTRALIAN_NATIONAL, -491, -22, 435));
        put("Arc 1950", createGeodeticDatum("Arc 1950", CLARKE_1880, -143, -90, -294));
        put("Arc 1960", createGeodeticDatum("Arc 1960", CLARKE_1880, -160, -8, -300));
        put("Ascension Island 1958", createGeodeticDatum("Ascension Island 1958", INTERNATIONAL_1924, -207, 107, 52));
        put("Astro B4 Sorol Atoll", createGeodeticDatum("Astro B4 Sorol Atoll", INTERNATIONAL_1924, 114, -116, -333));
        put("Astro Beacon 1945", createGeodeticDatum("Astro Beacon 1945", INTERNATIONAL_1924, 145, 75, -272));
        put("Astro DOS 71/4", createGeodeticDatum("Astro DOS 71/4", INTERNATIONAL_1924, -320, 550, -494));
        put("Astronomic Stn 1952", createGeodeticDatum("Astronomic Stn 1952", INTERNATIONAL_1924, 124, -234, -25));
        put("Australian Geodetic 1966", createGeodeticDatum("Australian Geodetic 1966", AUSTRALIAN_NATIONAL, -133, -48, 148));
        put("Australian Geodetic 1984", createGeodeticDatum("Australian Geodetic 1984", AUSTRALIAN_NATIONAL, -134, -48, 149));
        put("Australian Geocentric 1994 (GDA94)", createGeodeticDatum("Australian Geocentric 1994 (GDA94)", GRS_80, 0, 0, 0));
        put("Austrian", createGeodeticDatum("Austrian", BESSEL_1841, 594, 84, 471));
        put("Bellevue (IGN)", createGeodeticDatum("Bellevue (IGN)", INTERNATIONAL_1924, -127, -769, 472));
        put("Bermuda 1957", createGeodeticDatum("Bermuda 1957", CLARKE_1866, -73, 213, 296));
        put("Bogota Observatory", createGeodeticDatum("Bogota Observatory", INTERNATIONAL_1924, 307, 304, -318));
        put("Campo Inchauspe", createGeodeticDatum("Campo Inchauspe", INTERNATIONAL_1924, -148, 136, 90));
        put("Canton Astro 1966", createGeodeticDatum("Canton Astro 1966", INTERNATIONAL_1924, 298, -304, -375));
        put("Cape", createGeodeticDatum("Cape", CLARKE_1880, -136, -108, -292));
        put("Cape Canaveral", createGeodeticDatum("Cape Canaveral", CLARKE_1866, -2, 150, 181));
        put("Carthage", createGeodeticDatum("Carthage", CLARKE_1880, -263, 6, 431));
        put("CH-1903", createGeodeticDatum("CH-1903", BESSEL_1841, 674, 15, 405));
        put("Chatham 1971", createGeodeticDatum("Chatham 1971", INTERNATIONAL_1924, 175, -38, 113));
        put("Chua Astro", createGeodeticDatum("Chua Astro", INTERNATIONAL_1924, -134, 229, -29));
        put("Corrego Alegre", createGeodeticDatum("Corrego Alegre", INTERNATIONAL_1924, -206, 172, -6));
        put("Djakarta (Batavia)", createGeodeticDatum("Djakarta (Batavia)", BESSEL_1841, -377, 681, -50));
        put("DOS 1968", createGeodeticDatum("DOS 1968", INTERNATIONAL_1924, 230, -199, -752));
        put("Easter Island 1967", createGeodeticDatum("Easter Island 1967", INTERNATIONAL_1924, 211, 147, 111));
        put("Egypt", createGeodeticDatum("Egypt", INTERNATIONAL_1924, -130, -117, -151));
        put("European 1950", createGeodeticDatum("European 1950", INTERNATIONAL_1924, -87, -98, -121));
        put("European 1950 (Mean France)", createGeodeticDatum("European 1950 (Mean France)", INTERNATIONAL_1924, -87, -96, -120));
        put("European 1950 (Spain and Portugal)", createGeodeticDatum("European 1950 (Spain and Portugal)", INTERNATIONAL_1924, -84, -107, -120));
        put("European 1979", createGeodeticDatum("European 1979", INTERNATIONAL_1924, -86, -98, -119));
        put("Finland Hayford", createGeodeticDatum("Finland Hayford", INTERNATIONAL_1924, -78, -231, -97));
        put("Gandajika Base", createGeodeticDatum("Gandajika Base", INTERNATIONAL_1924, -133, -321, 50));
        put("Geodetic Datum 1949", createGeodeticDatum("Geodetic Datum 1949", INTERNATIONAL_1924, 84, -22, 209));
        put("GGRS 87", createGeodeticDatum("GGRS 87", GRS_80, -199.87, 74.79, 246.62));
        put("Guam 1963", createGeodeticDatum("Guam 1963", CLARKE_1866, -100, -248, 259));
        put("GUX 1 Astro", createGeodeticDatum("GUX 1 Astro", INTERNATIONAL_1924, 252, -209, -751));
        put("Hartebeeshoek94", createGeodeticDatum("Hartebeeshoek94", WGS_84, 0, 0, 0));
        put("Hermannskogel", createGeodeticDatum("Hermannskogel", BESSEL_1841, 653, -212, 449));
        put("Hjorsey 1955", createGeodeticDatum("Hjorsey 1955", INTERNATIONAL_1924, -73, 46, -86));
        put("Hong Kong 1963", createGeodeticDatum("Hong Kong 1963", INTERNATIONAL_1924, -156, -271, -189));
        put("Hu-Tzu-Shan", createGeodeticDatum("Hu-Tzu-Shan", INTERNATIONAL_1924, -634, -549, -201));
        put("Indian Bangladesh", createGeodeticDatum("Indian Bangladesh", EVEREST_INDIA_1830, 289, 734, 257));
        put("Indian Thailand", createGeodeticDatum("Indian Thailand", EVEREST_INDIA_1830, 214, 836, 303));
        put("Israeli", createGeodeticDatum("Israeli", CLARKE_1880_PALESTINE, -235, -85, 264));
        put("Ireland 1965", createGeodeticDatum("Ireland 1965", MODIFIED_AIRY, 506, -122, 611));
        put("ISTS 073 Astro 1969", createGeodeticDatum("ISTS 073 Astro 1969", INTERNATIONAL_1924, 208, -435, -229));
        put("Johnston Island", createGeodeticDatum("Johnston Island", INTERNATIONAL_1924, 191, -77, -204));
        put("Kandawala", createGeodeticDatum("Kandawala", EVEREST_INDIA_1830, -97, 787, 86));
        put("Kerguelen Island", createGeodeticDatum("Kerguelen Island", INTERNATIONAL_1924, 145, -187, 103));
        put("Kertau 1948", createGeodeticDatum("Kertau 1948", EVEREST_1948, -11, 851, 5));
        put("L.C. 5 Astro", createGeodeticDatum("L.C. 5 Astro", CLARKE_1866, 42, 124, 147));
        put("Liberia 1964", createGeodeticDatum("Liberia 1964", CLARKE_1880, -90, 40, 88));
        put("Luzon Mindanao", createGeodeticDatum("Luzon Mindanao", CLARKE_1866, -133, -79, -72));
        put("Luzon Philippines", createGeodeticDatum("Luzon Philippines", CLARKE_1866, -133, -77, -51));
        put("Mahe 1971", createGeodeticDatum("Mahe 1971", CLARKE_1880, 41, -220, -134));
        put("Marco Astro", createGeodeticDatum("Marco Astro", INTERNATIONAL_1924, -289, -124, 60));
        put("Massawa", createGeodeticDatum("Massawa", BESSEL_1841, 639, 405, 60));
        put("Merchich", createGeodeticDatum("Merchich", CLARKE_1880, 31, 146, 47));
        put("Midway Astro 1961", createGeodeticDatum("Midway Astro 1961", INTERNATIONAL_1924, 912, -58, 1227));
        put("Minna", createGeodeticDatum("Minna", CLARKE_1880, -92, -93, 122));
        put("NAD27 Alaska", createGeodeticDatum("NAD27 Alaska", CLARKE_1866, -5, 135, 172));
        put("NAD27 Bahamas", createGeodeticDatum("NAD27 Bahamas", CLARKE_1866, -4, 154, 178));
        put("NAD27 Canada", createGeodeticDatum("NAD27 Canada", CLARKE_1866, -10, 158, 187));
        put("NAD27 Canal Zone", createGeodeticDatum("NAD27 Canal Zone", CLARKE_1866, 0, 125, 201));
        put("NAD27 Caribbean", createGeodeticDatum("NAD27 Caribbean", CLARKE_1866, -7, 152, 178));
        put("NAD27 Central", createGeodeticDatum("NAD27 Central", CLARKE_1866, 0, 125, 194));
        put("NAD27 CONUS", createGeodeticDatum("NAD27 CONUS", CLARKE_1866, -8, 160, 176));
        put("NAD27 Cuba", createGeodeticDatum("NAD27 Cuba", CLARKE_1866, -9, 152, 178));
        put("NAD27 Greenland", createGeodeticDatum("NAD27 Greenland", CLARKE_1866, 11, 114, 195));
        put("NAD27 Mexico", createGeodeticDatum("NAD27 Mexico", CLARKE_1866, -12, 130, 190));
        put("NAD27 San Salvador", createGeodeticDatum("NAD27 San Salvador", CLARKE_1866, 1, 140, 165));
        put("NAD83", createGeodeticDatum("NAD83", GRS_80, 0, 0, 0));
        put("Nahrwn Masirah Ilnd", createGeodeticDatum("Nahrwn Masirah Ilnd", CLARKE_1880, -247, -148, 369));
        put("Nahrwn Saudi Arbia", createGeodeticDatum("Nahrwn Saudi Arbia", CLARKE_1880, -231, -196, 482));
        put("Nahrwn United Arab", createGeodeticDatum("Nahrwn United Arab", CLARKE_1880, -249, -156, 381));
        put("Naparima BWI", createGeodeticDatum("Naparima BWI", INTERNATIONAL_1924, -2, 374, 172));
        put("NGO1948", createGeodeticDatum("NGO1948", BESSEL_1841_NORWAY, 315, -217, 528));
        put("NTF France", createGeodeticDatum("NTF France", CLARKE_1880_IGN, -168, -60, 320));
        put("Norsk", createGeodeticDatum("Norsk", BESSEL_1841_NORWAY, 278, 93, 474));
        put("NZGD1949", createGeodeticDatum("NZGD1949", INTERNATIONAL_1924, 84, -22, 209));
        put("NZGD2000", createGeodeticDatum("NZGD2000", WGS_84, 0, 0, 0));
        put("Observatorio 1966", createGeodeticDatum("Observatorio 1966", INTERNATIONAL_1924, -425, -169, 81));
        put("Old Egyptian", createGeodeticDatum("Old Egyptian", HELMERT_1906, -130, 110, -13));
        put("Old Hawaiian", createGeodeticDatum("Old Hawaiian", CLARKE_1866, 61, -285, -181));
        put("Oman", createGeodeticDatum("Oman", CLARKE_1880, -346, -1, 224));
        put("Ord Srvy Grt Britn", createGeodeticDatum("Ord Srvy Grt Britn", AIRY_1830, 375, -111, 431));
        put("Pico De Las Nieves", createGeodeticDatum("Pico De Las Nieves", INTERNATIONAL_1924, -307, -92, 127));
        put("Pitcairn Astro 1967", createGeodeticDatum("Pitcairn Astro 1967", INTERNATIONAL_1924, 185, 165, 42));
        put("Potsdam Rauenberg DHDN", createGeodeticDatum("Potsdam Rauenberg DHDN", BESSEL_1841, 606, 23, 413));
        put("Prov So Amrican 1956", createGeodeticDatum("Prov So Amrican 1956", INTERNATIONAL_1924, -288, 175, -376));
        put("Prov So Chilean 1963", createGeodeticDatum("Prov So Chilean 1963", INTERNATIONAL_1924, 16, 196, 93));
        put("Puerto Rico", createGeodeticDatum("Puerto Rico", CLARKE_1866, 11, 72, -101));
        put("Pulkovo 1942 (1)", createGeodeticDatum("Pulkovo 1942 (1)", KRASSOVSKY_1940, 28, -130, -95));
        put("Pulkovo 1942 (2)", createGeodeticDatum("Pulkovo 1942 (2)", KRASSOVSKY_1940, 28, -130, -95));
        put("Qatar National", createGeodeticDatum("Qatar National", INTERNATIONAL_1924, -128, -283, 22));
        put("Qornoq", createGeodeticDatum("Qornoq", INTERNATIONAL_1924, 164, 138, -189));
        put("Reunion", createGeodeticDatum("Reunion", INTERNATIONAL_1924, 94, -948, -1262));
        put("Rijksdriehoeksmeting", createGeodeticDatum("Rijksdriehoeksmeting", BESSEL_1841, 593, 26, 478));
        put("Rome 1940", createGeodeticDatum("Rome 1940", INTERNATIONAL_1924, -225, -65, 9));
        put("RT 90", createGeodeticDatum("RT 90", BESSEL_1841, 498, -36, 568));
        put("S42", createGeodeticDatum("S42", KRASSOVSKY_1940, 28, -121, -77));
        put("Santo (DOS)", createGeodeticDatum("Santo (DOS)", INTERNATIONAL_1924, 170, 42, 84));
        put("Sao Braz", createGeodeticDatum("Sao Braz", INTERNATIONAL_1924, -203, 141, 53));
        put("Sapper Hill 1943", createGeodeticDatum("Sapper Hill 1943", INTERNATIONAL_1924, -355, 16, 74));
        put("Schwarzeck", createGeodeticDatum("Schwarzeck", BESSEL_1841_NAMIBIA, 616, 97, -251));
        put("South American 1969", createGeodeticDatum("South American 1969", SOUTH_AMERICAN_1969, -57, 1, -41));
        put("South Asia", createGeodeticDatum("South Asia", MODIFIED_FISCHER_1960, 7, -10, -26));
        put("Southeast Base", createGeodeticDatum("Southeast Base", INTERNATIONAL_1924, -499, -249, 314));
        put("Southwest Base", createGeodeticDatum("Southwest Base", INTERNATIONAL_1924, -104, 167, -38));
        put("Timbalai 1948", createGeodeticDatum("Timbalai 1948", EVEREST_INDIA_1830, -689, 691, -46));
        put("Tokyo", createGeodeticDatum("Tokyo", BESSEL_1841, -128, 481, 664));
        put("Tristan Astro 1968", createGeodeticDatum("Tristan Astro 1968", INTERNATIONAL_1924, -632, 438, -609));
        put("Viti Levu 1916", createGeodeticDatum("Viti Levu 1916", CLARKE_1880, 51, 391, -36));
        put("Wake-Eniwetok 1960", createGeodeticDatum("Wake-Eniwetok 1960", HOUGH_1960, 101, 52, -39));
        put("WGS 72", createGeodeticDatum("WGS 72", WGS_72, 0, 0, 5));
        put("WGS 84", DefaultGeodeticDatum.WGS84);
        put("Yacare", createGeodeticDatum("Yacare", INTERNATIONAL_1924, -155, 171, 37));
        put("Zanderij", createGeodeticDatum("Zanderij", INTERNATIONAL_1924, -265, 120, -358));
    }};

    private CoordinateReferenceSystem crs;
    private GeographicCRS geoCrs;
    private String projectionName;
    private MathTransform world2Crs;
    private MathTransform grid2Crs;
    private File imageFile;

    public OziMapFileReader(File file) throws IOException, FactoryException, TransformException {
        List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("windows-1251"));

        if (lines.size() < 40) {
            throw new IOException("too few lines!");
        }

        Collections.swap(lines, 9, 39);

        List<CalibrationPoint> calibrationPoints = new ArrayList<>();

        for (int lineIndex = 0; lineIndex < lines.size(); ++lineIndex) {
            String line = lines.get(lineIndex);

            if (line.isEmpty()) {
                continue;
            }

            String[] values = Arrays.stream(line.split(",", -1)).map(String::trim).toArray(String[]::new);

            String v0 = values[0];
            String v1 = values.length > 1 ? values[1] : "";
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

            switch (lineIndex) {
                case 2:
                    imageFile = new File(v0);

                    // TODO: check windows path on linux
                    // E:\Gpsmap\B3release\Maps\Demo1.bmp

                    if (!imageFile.exists()) {
                        imageFile = new File(file.getParent(), imageFile.getName());

                        if (!imageFile.exists()) {
                            throw new IOException("File not found: " + imageFile);
                        }
                    }
                    break;
                case 4:
                    if (values.length < 5) {
                        return;
                    }

                    GeodeticDatum datum = DATUMS.get(v0);

                    if (datum == null) {
                        return;
                    }

                    CRSFactory crsFactory = ReferencingFactoryFinder.getCRSFactory(null);
                    geoCrs = crsFactory.createGeographicCRS(ImmutableMap.of("name", v0), datum, DefaultEllipsoidalCS.GEODETIC_2D);
                    break;
                default: {
                    if (v0.startsWith("Map Projection")) {
                        if (StringUtils.isEmpty(v1)) {
                            return;
                        }

                        projectionName = v1;
                    } else if (v0.startsWith("Projection Setup")) {
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

                                    if (NumberUtils.isCreatable(v1)) {
                                        parameters.parameter("latitude_of_origin").setValue(NumberUtils.toDouble(v1));
                                    }

                                    if (NumberUtils.isCreatable(v2)) {
                                        parameters.parameter("central_meridian").setValue(NumberUtils.toDouble(v2));
                                    }

                                    if (NumberUtils.isCreatable(v3)) {
                                        parameters.parameter("scale_factor").setValue(NumberUtils.toDouble(v3));
                                    }

                                    if (NumberUtils.isCreatable(values[4])) {
                                        parameters.parameter("false_easting").setValue(NumberUtils.toDouble(values[4]));
                                    }

                                    if (NumberUtils.isCreatable(values[5])) {
                                        parameters.parameter("false_northing").setValue(NumberUtils.toDouble(values[5]));
                                    }

                                    Conversion conversion = new DefiningConversion("Mercator_1SP", parameters);

                                    crsFactory = ReferencingFactoryFinder.getCRSFactory(null);

                                    Map<String, ?> properties = Collections.singletonMap("name", "unnamed");

                                    this.crs = crsFactory.createProjectedCRS(properties, geoCrs, conversion, cartCS);
                                    break;
                                }
                                default:
                                    break;
                            }

                            world2Crs = CRS.findMathTransform(DefaultGeographicCRS.WGS84, this.crs, true);
                        } catch (FactoryException e) {
                            return;
                        }
                    } else if (v0.startsWith("Point")) {
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

                            if (world2Crs.transform(latLon, p) != null) {
                                xy = p;
                            }
                        } else if (NumberUtils.isCreatable(v14) && NumberUtils.isCreatable(v15)) {
                            xy = new DirectPosition2D(crs, NumberUtils.toDouble(v15), NumberUtils.toDouble(v14));
                        }

                        if (pixelLine != null && xy != null) {
                            calibrationPoints.add(new CalibrationPoint(pixelLine, xy));
                        }
                    }
                }
            }
        }

        if (calibrationPoints.size() < 2) {
            throw new IOException("too few calibration points!");
        }

        double xPixelSize;
        double yPixelSize;
        double xULC;
        double yULC;

        if (calibrationPoints.size() == 1) {
            CalibrationPoint cp1 = calibrationPoints.get(calibrationPoints.size() - 1);
            CalibrationPoint cp0 = calibrationPoints.get(0);

            xPixelSize = (cp1.getXy().x - cp0.getXy().x) / (double) (cp1.getPixelLine().x - cp0.getPixelLine().x);
            yPixelSize = (cp1.getXy().y - cp0.getXy().y) / (double) (cp1.getPixelLine().y - cp0.getPixelLine().y);
            xULC = cp0.getXy().x - cp0.getPixelLine().x * xPixelSize;
            yULC = cp0.getXy().y - cp0.getPixelLine().y * yPixelSize;

            grid2Crs = new AffineTransform2D(xPixelSize, 0, 0, yPixelSize, xULC, yULC);
        } else if (calibrationPoints.size() >= 2) {
            int nGCPCount = calibrationPoints.size();
            //throw new IOException("Too much calibration points (TEMP)");
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
                throw new IOException("Degenerate in at least one dimension");
            }

            double pl_normalize[] = new double[6], geo_normalize[] = new double[6];

            pl_normalize[0] = -min_pixel / (max_pixel - min_pixel);
            pl_normalize[1] = 1.0 / (max_pixel - min_pixel);
            pl_normalize[2] = 0.0;
            pl_normalize[3] = -min_line / (max_line - min_line);
            pl_normalize[4] = 0.0;
            pl_normalize[5] = 1.0 / (max_line - min_line);

            geo_normalize[0] = -min_geox / (max_geox - min_geox);
            geo_normalize[1] = 1.0 / (max_geox - min_geox);
            geo_normalize[2] = 0.0;
            geo_normalize[3] = -min_geoy / (max_geoy - min_geoy);
            geo_normalize[4] = 0.0;
            geo_normalize[5] = 1.0 / (max_geoy - min_geoy);

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

            for (int i = 0; i < calibrationPoints.size(); ++i) {
                CalibrationPoint cp = calibrationPoints.get(i);
//
////                GDALApplyGeoTransform(pl_normalize,
////                        pasGCPs[i].dfGCPPixel,
////                        pasGCPs[i].dfGCPLine,
////                        &pixel, &line);

                double pixel = geo_normalize[0] + cp.pixelLine.x * geo_normalize[1]
                        + cp.pixelLine.y * geo_normalize[2];
                double line = geo_normalize[3] + cp.pixelLine.x * geo_normalize[4]
                        + cp.pixelLine.y * geo_normalize[5];

////                GDALApplyGeoTransform(geo_normalize,
////                        pasGCPs[i].dfGCPX,
////                        pasGCPs[i].dfGCPY,
////                        &geox, &geoy);

                double geox = geo_normalize[0] + cp.xy.x * geo_normalize[1]
                        + cp.xy.y * geo_normalize[2];
                double geoy = geo_normalize[3] + cp.xy.x * geo_normalize[4]
                        + cp.xy.y * geo_normalize[5];

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
                    calibrationPoints.size() * (sum_xx * sum_yy - sum_xy * sum_xy)
                            + 2.0 * sum_x * sum_y * sum_xy - sum_y * sum_y * sum_xx
                            - sum_x * sum_x * sum_yy;

            /* -------------------------------------------------------------------- */
            /*      If the divisor is zero, there is no valid solution.             */
            /* -------------------------------------------------------------------- */
            if (divisor == 0.0) {
                throw new IOException("Divisor is zero, there is no valid solution");
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

            /* -------------------------------------------------------------------- */
            /*      Compose the resulting transformation with the normalization     */
            /*      geotransformations.                                             */
            /* -------------------------------------------------------------------- */
            double gt1p2[] = new double[]{0, 0, 0, 0, 0, 0};
            double inv_geo_normalize[] = new double[]{0, 0, 0, 0, 0, 0};

            if (!GDALInvGeoTransform(geo_normalize, inv_geo_normalize)) {
                throw new IOException("HZ");
            }

            double padfGeoTransform[] = new double[] {0, 0, 0, 0, 0, 0};

            GDALComposeGeoTransforms(pl_normalize, gt_normalized, gt1p2);
            GDALComposeGeoTransforms(gt1p2, inv_geo_normalize, padfGeoTransform);

            xPixelSize = padfGeoTransform[0];
            yPixelSize = padfGeoTransform[3];
            xULC = padfGeoTransform[4];
            yULC = padfGeoTransform[5];

            grid2Crs = new AffineTransform2D(xPixelSize, 0, 0, yPixelSize, xULC, yULC);

            int asd = 0;
            int asdf = asd;
        } else {
            throw new IOException("Too few calibration points");
        }
    }

    private static boolean GDALInvGeoTransform(double[] gt_in, double[] gt_out) {
        // Special case - no rotation - to avoid computing determinate
        // and potential precision issues.
        if (gt_in[2] == 0.0 && gt_in[4] == 0.0 &&
                gt_in[1] != 0.0 && gt_in[5] != 0.0) {
        /*X = gt_in[0] + x * gt_in[1]
          Y = gt_in[3] + y * gt_in[5]
          -->
          x = -gt_in[0] / gt_in[1] + (1 / gt_in[1]) * X
          y = -gt_in[3] / gt_in[5] + (1 / gt_in[5]) * Y
        */
            gt_out[0] = -gt_in[0] / gt_in[1];
            gt_out[1] = 1.0 / gt_in[1];
            gt_out[2] = 0.0;
            gt_out[3] = -gt_in[3] / gt_in[5];
            gt_out[4] = 0.0;
            gt_out[5] = 1.0 / gt_in[5];
            return false;
        }

        // Assume a 3rd row that is [1 0 0].

        // Compute determinate.

        double det = gt_in[1] * gt_in[5] - gt_in[2] * gt_in[4];

        if (Math.abs(det) < 0.000000000000001)
            return false;

        double inv_det = 1.0 / det;

        // Compute adjoint, and divide by determinate.

        gt_out[1] = gt_in[5] * inv_det;
        gt_out[4] = -gt_in[4] * inv_det;

        gt_out[2] = -gt_in[2] * inv_det;
        gt_out[5] = gt_in[1] * inv_det;

        gt_out[0] = (gt_in[2] * gt_in[3] - gt_in[0] * gt_in[5]) * inv_det;
        gt_out[3] = (-gt_in[1] * gt_in[3] + gt_in[0] * gt_in[4]) * inv_det;

        return true;
    }

    private static void GDALComposeGeoTransforms(double[] padfGT1, double[] padfGT2,
                                                 double[] padfGTOut) {
        double gtwrk[] = new double[]{0, 0, 0, 0, 0, 0};
        // We need to think of the geotransform in a more normal form to do
        // the matrix multiple:
        //
        //  __                     __
        //  | gt[1]   gt[2]   gt[0] |
        //  | gt[4]   gt[5]   gt[3] |
        //  |  0.0     0.0     1.0  |
        //  --                     --
        //
        // Then we can use normal matrix multiplication to produce the
        // composed transformation.  I don't actually reform the matrix
        // explicitly which is why the following may seem kind of spagettish.

        gtwrk[1] =
                padfGT2[1] * padfGT1[1]
                        + padfGT2[2] * padfGT1[4];
        gtwrk[2] =
                padfGT2[1] * padfGT1[2]
                        + padfGT2[2] * padfGT1[5];
        gtwrk[0] =
                padfGT2[1] * padfGT1[0]
                        + padfGT2[2] * padfGT1[3]
                        + padfGT2[0] * 1.0;

        gtwrk[4] =
                padfGT2[4] * padfGT1[1]
                        + padfGT2[5] * padfGT1[4];
        gtwrk[5] =
                padfGT2[4] * padfGT1[2]
                        + padfGT2[5] * padfGT1[5];
        gtwrk[3] =
                padfGT2[4] * padfGT1[0]
                        + padfGT2[5] * padfGT1[3]
                        + padfGT2[3] * 1.0;

        System.arraycopy(gtwrk, 0, padfGTOut, 0, gtwrk.length);
        //memcpy(padfGTOut, gtwrk, sizeof(gtwrk));
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

    private static GeodeticDatum createGeodeticDatum(String name, Ellipsoid ellipsoid, double dx, double dy, double dz) {
        Map<String, Object> map = new HashMap<>();

        map.put("name", name);

        final BursaWolfParameters bursaWolfParameters = new BursaWolfParameters(DefaultGeodeticDatum.WGS84);

        bursaWolfParameters.dx = dx;
        bursaWolfParameters.dy = dy;
        bursaWolfParameters.dz = dz;

        if (!bursaWolfParameters.isIdentity()) {
            map.put(DefaultGeodeticDatum.BURSA_WOLF_KEY, bursaWolfParameters);
        }

        DatumFactory datumFactory = ReferencingFactoryFinder.getDatumFactory(null);

        try {
            return datumFactory.createGeodeticDatum(map, ellipsoid, DefaultPrimeMeridian.GREENWICH);
        } catch (FactoryException e) {
            // TODO: implement
        }

        return null;
    }

    private static final class CalibrationPoint {
        private final Point pixelLine;
        private final DirectPosition2D xy;

        public CalibrationPoint(Point pixelLine, DirectPosition2D xy) {
            this.pixelLine = pixelLine;
            this.xy = xy;
        }

        public Point getPixelLine() {
            return pixelLine;
        }

        public DirectPosition2D getXy() {
            return xy;
        }
    }
}
