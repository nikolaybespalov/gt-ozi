package com.github.nikolaybespalov.gtoziexplorermap;

import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Test;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import javax.imageio.spi.IIORegistry;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class OziExplorerMapReaderTest {

    @Test
    public void testDemo1() throws IOException {
        OziExplorerMapReader reader = new OziExplorerMapReader(Paths.get("c:\\Users\\Nikolay Bespalov\\Documents\\github.com\\nikolaybespalov\\gt-oziexplorermap\\src\\test\\resources\\Maps\\Demo1.map"));

        CoordinateReferenceSystem crs = reader.getCoordinateReferenceSystem();

        assertNotNull(crs);
        assertTrue(CRS.equalsIgnoreMetadata(DefaultGeographicCRS.WGS84, crs));

        GridEnvelope originalGridRange = reader.getOriginalGridRange();

        assertNotNull(originalGridRange);
        assertEquals(new GridEnvelope2D(new Rectangle(0, 0, 1202, 778)), originalGridRange);

        GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();

        assertNotNull(originalEnvelope);

        GeneralEnvelope expectedOriginalEnvelope = new GeneralEnvelope(DefaultGeographicCRS.WGS84);
        expectedOriginalEnvelope.add(new DirectPosition2D(152.288056, -26.449320));
        expectedOriginalEnvelope.add(new DirectPosition2D(152.991764, -26.449320));
        expectedOriginalEnvelope.add(new DirectPosition2D(152.991764, -26.858472));
        expectedOriginalEnvelope.add(new DirectPosition2D(152.288056, -26.858472));

        assertTrue(expectedOriginalEnvelope.equals(originalEnvelope, 0.001, false));
        //assertEquals(expectedOriginalEnvelope, originalEnvelope);
    }

    @Test
    public void testWorld() throws IOException, FactoryException {
        IIORegistry.getDefaultInstance().registerServiceProvider(new OzfImageReaderSpi());

        OziExplorerMapReader reader = new OziExplorerMapReader(Paths.get("c:\\Users\\Nikolay Bespalov\\Documents\\github.com\\nikolaybespalov\\gt-oziexplorermap\\src\\test\\resources\\Maps\\World.map"));

        CoordinateReferenceSystem crs = reader.getCoordinateReferenceSystem();

        assertNotNull(crs);
        CoordinateReferenceSystem expectedCrs = CRS.parseWKT("PROJCS[\"unnamed\",\n" +
                "    GEOGCS[\"WGS 84\",\n" +
                "        DATUM[\"WGS_1984\",\n" +
                "            SPHEROID[\"WGS 84\",6378137,298.257223563,\n" +
                "                AUTHORITY[\"EPSG\",\"7030\"]],\n" +
                "            AUTHORITY[\"EPSG\",\"6326\"]],\n" +
                "        PRIMEM[\"Greenwich\",0,\n" +
                "            AUTHORITY[\"EPSG\",\"8901\"]],\n" +
                "        UNIT[\"degree\",0.0174532925199433,\n" +
                "            AUTHORITY[\"EPSG\",\"9122\"]],\n" +
                "        AUTHORITY[\"EPSG\",\"4326\"]],\n" +
                "    PROJECTION[\"Mercator_1SP\"],\n" +
                "    PARAMETER[\"central_meridian\",0],\n" +
                "    PARAMETER[\"scale_factor\",1],\n" +
                "    PARAMETER[\"false_easting\",0],\n" +
                "    PARAMETER[\"false_northing\",0],\n" +
                "    UNIT[\"Meter\",1]]");
        assertTrue(CRS.equalsIgnoreMetadata(expectedCrs, crs));

        GridEnvelope originalGridRange = reader.getOriginalGridRange();

        assertNotNull(originalGridRange);
        assertEquals(new GridEnvelope2D(new Rectangle(0, 0, 2108, 2048)), originalGridRange);

        GeneralEnvelope originalEnvelope = reader.getOriginalEnvelope();

        assertNotNull(originalEnvelope);

        GeneralEnvelope expectedOriginalEnvelope = new GeneralEnvelope(expectedCrs);
        //expectedOriginalEnvelope.add(new DirectPosition2D(-1.992618885199597E7, -1.790989307498489E7));
        //expectedOriginalEnvelope.add(new DirectPosition2D(1.992618885199597E7, 1.832794874451037E7));

//        expectedOriginalEnvelope.add(new DirectPosition2D(-20046458.654, 18365954.163));
//        expectedOriginalEnvelope.add(new DirectPosition2D(-20046458.654,-20551246.174));
//        expectedOriginalEnvelope.add(new DirectPosition2D(20046458.654,18365954.163));
//        expectedOriginalEnvelope.add(new DirectPosition2D(20046458.654,-20551246.174));

        expectedOriginalEnvelope.add(new DirectPosition2D(20046458.654, -20551246.174));
        expectedOriginalEnvelope.add(new DirectPosition2D(-20046458.654, -20551246.174));
        expectedOriginalEnvelope.add(new DirectPosition2D(20046458.654, 18365954.163));
        expectedOriginalEnvelope.add(new DirectPosition2D(20046458.654, -20551246.174));


        assertTrue(expectedOriginalEnvelope.equals(originalEnvelope, 0.001, false));
        //assertEquals(expectedOriginalEnvelope, originalEnvelope);
    }

    @Test
    public void test123() {
        String s = "Adindan,4201,5,-162,-12,206\t\t\t# Africa - Eritrea, Ethiopia and Sudan \n" +
                "Afgooye,4205,15,-43,-163,45\t\t\t# Somalia\n" +
                "Ain el Abd 1970,4204,14,-150,-251,-2\t\t# Asia - Middle East - Bahrain, Kuwait and Saudi Arabia\n" +
                "Anna 1 Astro 1965,4708,2,-491,-22,435\t\t# Cocos (Keeling) Islands\n" +
                "Arc 1950,4209,5,-143,-90,-294\t\t\t# Africa - Botswana, Malawi, Zambia, Zimbabwe\n" +
                "Arc 1960,4210,5,-160,-8,-300\t\t\t# Africa - Kenya, Tanzania and Uganda\n" +
                "Ascension Island 1958,4712,14,-207,107,52\t# St Helena - Ascension Island\n" +
                "Astro B4 Sorol Atoll,4707,14,114,-116,-333\t# USA - Hawaii - Tern Island and Sorel Atoll\n" +
                "Astro Beacon 1945,4709,14,145,75,-272\t\t# Japan - Iwo Jima\n" +
                "Astro DOS 71/4,4710,14,-320,550,-494\t\t# St Helena - St Helena Island\n" +
                "Astronomic Stn 1952,4711,14,124,-234,-25\t# Japan - Minamitori-shima (Marcus Island)\n" +
                "Australian Geodetic 1966,4202,2,-133,-48,148\t# Australasia - Australia and PNG - AGD66\n" +
                "Australian Geodetic 1984,4203,2,-134,-48,149\t# Australia - AGD84\n" +
                "Australian Geocentric 1994 (GDA94),4283,11,0,0,0 # Australia - GDA94\n" +
                "Austrian,4312,3,594,84,471\t\t\t# MGI - Europe, Austria and former Yugoslavia\n" +
                "Bellevue (IGN),4714,14,-127,-769,472\t\t# Vanuatu - southern islands\n" +
                "Bermuda 1957,4216,4,-73,213,296\t\t\t# Bermuda\n" +
                "Bogota Observatory,4218,14,307,304,-318\t\t# Colombia\n" +
                "Campo Inchauspe,4221,14,-148,136,90\t\t# Argentina\n" +
                "Canton Astro 1966,4716,14,298,-304,-375\t\t# Kiribati - Phoenix Islands\n" +
                "Cape,4222,5,-136,-108,-292\t\t\t# Africa - Botswana and South Africa\n" +
                "Cape Canaveral,4717,4,-2,150,181\t\t# North America - Bahamas and USA - Florida\n" +
                "Carthage,4223,5,-263,6,431\t\t\t# Tunisia\n" +
                "CH-1903,4149,3,674,15,405\t\t\t# Europe - Liechtenstein and Switzerland\n" +
                "Chatham 1971,4672,14,175,-38,113\t\t# New Zealand - Chatham Islands\n" +
                "Chua Astro,4224,14,-134,229,-29\t\t\t# South America - Brazil ; N Paraguay\n" +
                "Corrego Alegre,4225,14,-206,172,-6\t\t# Brazil - Corrego Alegre\n" +
                "Djakarta (Batavia),4211,3,-377,681,-50\t\t# Indonesia - Java\n" +
                "DOS 1968,,14,230,-199,-752\t\t\t# Solomon Islands - Gizo Island : EPSG:4718 + EPSG:15805 (gcs.csv uses EPSG:15807)\n" +
                "Easter Island 1967,4719,14,211,147,111\t\t# Chile - Easter Island\n" +
                "Egypt,,14,-130,-117,-151\t\t\t# Egypt - EPSG code is 4199, but transformation parameters are missing in gcs.csv\n" +
                "European 1950,4230,14,-87,-98,-121\t\t# Europe\n" +
                "European 1950 (Mean France),,14,-87,-96,-120\t# Europe -France\n" +
                "European 1950 (Spain and Portugal),,14,-84,-107,-120 # Europe - Spain and Portugal\n" +
                "European 1979,4668,14,-86,-98,-119\t\t# Europe - west\n" +
                "Finland Hayford,4123,14,-78,-231,-97\t\t# Finland (KKJ)\n" +
                "Gandajika Base,4233,14,-133,-321,50\t\t# Maldives\n" +
                "Geodetic Datum 1949,4272,14,84,-22,209\t\t# New Zealand (NZGD49)\n" +
                "GGRS 87,4121,11,-199.87,74.79,246.62\t\t# Greece\n" +
                "Guam 1963,4675,4,-100,-248,259\t\t\t# Guam\n" +
                "GUX 1 Astro,4718,14,252,-209,-751\t\t# Solomon Islands - Guadalcanal Island\n" +
                "Hartebeeshoek94,4148,20,0,0,0\t\t\t# South Africa\n" +
                "Hermannskogel,3906,3,653,-212,449\t\t# Boznia and Herzegovina; Croatia; FYR Macedonia; Montenegro; Serbia; Slovenia (MGI 1901)\n" +
                "Hjorsey 1955,4658,14,-73,46,-86\t\t\t# Iceland\n" +
                "Hong Kong 1963,4739,14,-156,-271,-189\t\t# China - Hong Kong\n" +
                "Hu-Tzu-Shan,4236,14,-634,-549,-201\t\t# Taiwan\n" +
                "Indian Bangladesh,4682,6,289,734,257\t\t# Bangladesh (Gulshan 303)\n" +
                "Indian Thailand,4240,6,214,836,303\t\t# Thailand\n" +
                "Israeli,4281,23,-235,-85,264\t\t\t# Asia - Middle East - Israel, Jordan and Palestine Territory (Palestine 1923)\n" +
                "Ireland 1965,4299,1,506,-122,611\t\t# Europe - Ireland (Republic and Ulster)\n" +
                "ISTS 073 Astro 1969,4724,14,208,-435,-229\t# British Indian Ocean Territory - Diego Garcia\n" +
                "Johnston Island,4725,14,191,-77,-204\t\t# Johnston Island\n" +
                "Kandawala,4244,6,-97,787,86\t\t\t# Sri Lanka\n" +
                "Kerguelen Island,4698,14,145,-187,103\t\t# French Southern Territories - Kerguelen\n" +
                "Kertau 1948,4245,7,-11,851,5\t\t\t# Asia - Malaysia (west) and Singapore\n" +
                "L.C. 5 Astro,4726,4,42,124,147\t\t\t# Cayman Islands - Little Cayman and Cayman Brac\n" +
                "Liberia 1964,4251,5,-90,40,88\t\t\t# Liberia\n" +
                "Luzon Mindanao,,4,-133,-79,-72\t\t\t# Philippines - Mindanao (EPSG:4253 + EPSG:1162 Coordinate Transformation)\n" +
                "Luzon Philippines,4253,4,-133,-77,-51\t\t# Philippines - excluding Mindanao\n" +
                "Mahe 1971,4256,5,41,-220,-134\t\t\t# Seychelles\n" +
                "Marco Astro,4616,14,-289,-124,60\t\t# Portugal - Selvagens islands (Madeira)\n" +
                "Massawa,4262,3,639,405,60\t\t\t# Eritrea\n" +
                "Merchich,4261,5,31,146,47\t\t\t# Morocco\n" +
                "Midway Astro 1961,4727,14,912,-58,1227\t\t# Midway Islands - Sand and Eastern Islands\n" +
                "Minna,4263,5,-92,-93,122\t\t\t# Nigeria\n" +
                "NAD27 Alaska,,4,-5,135,172\t\t\t# Alaska (EPSG:4269 + EPSG:1176 Coordinate Transformation)\n" +
                "NAD27 Bahamas,,4,-4,154,178\t\t\t# Bahamas (EPSG:4269 + EPSG:1177 Coordinate Transformation)\n" +
                "NAD27 Canada,,4,-10,158,187\t\t\t# Canada (EPSG:4269 + EPSG:1172 Coordinate Transformation)\n" +
                "NAD27 Canal Zone,,4,0,125,201\t\t\t# Panama (EPSG:4269 + EPSG:1184 Coordinate Transformation)\n" +
                "NAD27 Caribbean,,4,-7,152,178\t\t\t# Caribbean\n" +
                "NAD27 Central,,4,0,125,194\t\t\t# Central America (EPSG:4269 + EPSG:1171 Coordinate Transformation)\n" +
                "NAD27 CONUS,,4,-8,160,176\t\t\t# Continental US (EPSG:4269 + EPSG:1173 Coordinate Transformation)\n" +
                "NAD27 Cuba,,4,-9,152,178\t\t\t# Cuba (EPSG:4269 + EPSG:1185 Coordinate Transformation)\n" +
                "NAD27 Greenland,,4,11,114,195\t\t\t# Greenland - Hayes Peninsula (EPSG:4269 + EPSG:1186 Coordinate Transformation)\n" +
                "NAD27 Mexico,,4,-12,130,190\t\t\t# Mexico (EPSG:4269 + EPSG:1187 Coordinate Transformation)\n" +
                "NAD27 San Salvador,,4,1,140,165\t\t\t# San Salvador (EPSG:4269 + EPSG:1178 Coordinate Transformation)\n" +
                "NAD83,4269,11,0,0,0\t\t\t\t# North America\n" +
                "Nahrwn Masirah Ilnd,,5,-247,-148,369\t\t# Oman - Masirah Island (EPSG:4270 + EPSG:1189)\n" +
                "Nahrwn Saudi Arbia,,5,-231,-196,482\t\t# Saudi Arabia (EPSG:4270 + EPSG:1190)\n" +
                "Nahrwn United Arab,,5,-249,-156,381\t\t# United Arab Emirates (UAE) (EPSG:4270 + EPSG:1191)\n" +
                "Naparima BWI,4271,14,-2,374,172\t\t\t# Trinidad and Tobago - Tobago\n" +
                "NGO1948,4273,27,315,-217,528\t\t\t# Norway\n" +
                "NTF France,4275,24,-168,-60,320\t\t\t# France\n" +
                "Norsk,4817,27,278,93,474\t\t\t# Norway (NGO 1948)\n" +
                "NZGD1949,4272,14,84,-22,209\t\t\t# New Zealand\n" +
                "NZGD2000,4167,20,0,0,0\t\t\t\t# New Zealand\n" +
                "Observatorio 1966,4182,14,-425,-169,81\t\t# Portugal - western Azores\n" +
                "Old Egyptian,4229,12,-130,110,-13\t\t# Egypt (1907)\n" +
                "Old Hawaiian,4135,4,61,-285,-181\t\t# USA - Hawaii\n" +
                "Oman,4232,5,-346,-1,224\t\t\t\t# Oman\n" +
                "Ord Srvy Grt Britn,4277,0,375,-111,431\t\t# UK - Great Britain; Isle of Man\n" +
                "Pico De Las Nieves,4728,14,-307,-92,127\t\t# Spain - Canary Islands\n" +
                "Pitcairn Astro 1967,4729,14,185,165,42\t\t# Pitcairn Island\n" +
                "Potsdam Rauenberg DHDN,4314,3,606,23,413\t# Germany\n" +
                "Prov So Amrican 1956,4248,14,-288,175,-376\t# South America - PSAD56\n" +
                "Prov So Chilean 1963,4254,14,16,196,93\t\t# South America - Tierra del Fuego\n" +
                "Puerto Rico,4139,4,11,72,-101\t\t\t# Caribbean - Puerto Rico and the Virgin Islands\n" +
                "Pulkovo 1942 (1),4284,15,28,-130,-95\t\t# Europe - FSU\n" +
                "Pulkovo 1942 (2),4284,15,28,-130,-95\t\t# Europe - FSU\n" +
                "Qatar National,4285,14,-128,-283,22\t\t# Qatar\n" +
                "Qornoq,4287,14,164,138,-189\t\t\t# Greenland\n" +
                "Reunion,4626,14,94,-948,-1262\t\t\t# France - Reunion Island\n" +
                "Rijksdriehoeksmeting,4289,3,593,26,478\t\t# Netherlands\n" +
                "Rome 1940,4806,14,-225,-65,9\t\t\t# Italy - including San Marino and Vatican\n" +
                "RT 90,4124,3,498,-36,568\t\t\t# Sweden\n" +
                "S42,4179,15,28,-121,-77\t\t\t\t# Europe - eastern - S-42\n" +
                "Santo (DOS),4730,14,170,42,84\t\t\t# Vanuatu - northern islands\n" +
                "Sao Braz,4184,14,-203,141,53\t\t\t# Portugal - eastern Azores\n" +
                "Sapper Hill 1943,4292,14,-355,16,74\t\t# Falkland Islands\n" +
                "Schwarzeck,4293,21,616,97,-251\t\t\t# Namibia\n" +
                "South American 1969,4291,16,-57,1,-41\t\t# South America - SAD69\n" +
                "South Asia,,8,7,-10,-26\t\t\t\t# Singapore (unknown EPSG code)\n" +
                "Southeast Base,4615,14,-499,-249,314\t\t# Porto Santo and Madeira Islands\n" +
                "Southwest Base,4183,14,-104,167,-38\t\t# Faial, Graciosa, Pico, Sao Jorge and Terceira\n" +
                "Timbalai 1948,4298,6,-689,691,-46\t\t# Asia - Brunei and East Malaysia\n" +
                "Tokyo,4301,3,-128,481,664\t\t\t# Asia - Japan and Korea\n" +
                "Tristan Astro 1968,4734,14,-632,438,-609\t# St Helena - Tristan da Cunha\n" +
                "Viti Levu 1916,4731,5,51,391,-36\t\t# Fiji - Viti Levu\n" +
                "Wake-Eniwetok 1960,4732,13,101,52,-39\t\t# Marshall Islands - Eniwetok, Kwajalein and Wake islands\n" +
                "WGS 72,4322,19,0,0,5\t\t\t\t# World\n" +
                "WGS 84,4326,20,0,0,0\t\t\t\t# World\n" +
                "Yacare,4309,14,-155,171,37\t\t\t# Uruguay\n" +
                "Zanderij,4311,14,-265,120,-358\t\t\t# Suriname";

        String[] lines = s.split("\n");

        for (int i = 0; i < lines.length; ++i) {
            String line = lines[i];

            String epsg = "";

            if (!line.split(",", -1)[1].isEmpty()) {
                epsg = ", EPSG:" + line.split(",", -1)[1];
            }

            System.out.println("- " + line.split(",")[0] + " (" + line.split("#")[1].trim() + epsg + ")");
        }
    }

    @Test
    public void test124() {
        String s = "0,Airy 1830,6377563.396,299.3249646\n" +
                "1,Modified Airy,6377340.189,299.3249646\n" +
                "2,Australian National,6378160.0,298.25 \n" +
                "3,Bessel 1841,6377397.155,299.1528128\n" +
                "4,Clarke 1866,6378206.4,294.9786982\n" +
                "5,Clarke 1880,6378249.145,293.465\n" +
                "6,Everest (India 1830),6377276.345,300.8017\n" +
                "7,Everest (1948),6377304.063,300.8017\n" +
                "8,Modified Fischer 1960,6378155.0,298.3\n" +
                "9,Everest (Pakistan),6377309.613,300.8017\n" +
                "10,Indonesian 1974,6378160.0,298.247\n" +
                "11,GRS 80,6378137.0,298.257222101\n" +
                "12,Helmert 1906,6378200.0,298.3\n" +
                "13,Hough 1960,6378270.0,297.0\n" +
                "14,International 1924,6378388.0,297.0\n" +
                "15,Krassovsky 1940,6378245.0,298.3\n" +
                "16,South American 1969,6378160.0,298.25\n" +
                "17,Everest (Malaysia 1969),6377295.664,300.8017\n" +
                "18,Everest (Sabah Sarawak),6377298.556,300.8017\n" +
                "19,WGS 72,6378135.0,298.26\n" +
                "20,WGS 84,6378137.0,298.257223563\n" +
                "21,Bessel 1841 (Namibia),6377483.865,299.1528128\n" +
                "22,Everest (India 1956),6377301.243,300.8017\n" +
                "23,Clarke 1880 Palestine,6378300.789,293.466 \n" +
                "24,Clarke 1880 IGN,6378249.2,293.466021\n" +
                "25,Hayford 1909,6378388.0,296.959263\n" +
                "26,Clarke 1858,6378350.87,294.26\n" +
                "27,Bessel 1841 (Norway),6377492.0176,299.1528\n" +
                "28,Plessis 1817 (France),6376523.0,308.6409971\n" +
                "29,Hayford 1924,6378388.0,297.0";

        String[] lines = s.split("\n", -1);

        // private static final Ellipsoid AIRY_1830 = DefaultEllipsoid.createFlattenedSphere("Airy 1830", 6377563.396, 299.3249646, SI.METER);

        for (int i = 0; i < lines.length; ++i) {
            String line = lines[i];

            String[] values = line.split(",", -1);

            String s1 = values[1];
            String s2 = values[2];
            String s3 = values[3];

            System.out.println("private static final Ellipsoid " + s1.toUpperCase().replace(' ', '_').replace("(", "").replace(")", "") + " = DefaultEllipsoid.createFlattenedSphere(\"" + s1 + "\", " + s2 + ", " + s3 + ", SI.METER);");
        }
    }

    @Test
    public void test125() {
        String s = "Adindan,4201,5,-162,-12,206\t\t\t# Africa - Eritrea, Ethiopia and Sudan \n" +
                "Afgooye,4205,15,-43,-163,45\t\t\t# Somalia\n" +
                "Ain el Abd 1970,4204,14,-150,-251,-2\t\t# Asia - Middle East - Bahrain, Kuwait and Saudi Arabia\n" +
                "Anna 1 Astro 1965,4708,2,-491,-22,435\t\t# Cocos (Keeling) Islands\n" +
                "Arc 1950,4209,5,-143,-90,-294\t\t\t# Africa - Botswana, Malawi, Zambia, Zimbabwe\n" +
                "Arc 1960,4210,5,-160,-8,-300\t\t\t# Africa - Kenya, Tanzania and Uganda\n" +
                "Ascension Island 1958,4712,14,-207,107,52\t# St Helena - Ascension Island\n" +
                "Astro B4 Sorol Atoll,4707,14,114,-116,-333\t# USA - Hawaii - Tern Island and Sorel Atoll\n" +
                "Astro Beacon 1945,4709,14,145,75,-272\t\t# Japan - Iwo Jima\n" +
                "Astro DOS 71/4,4710,14,-320,550,-494\t\t# St Helena - St Helena Island\n" +
                "Astronomic Stn 1952,4711,14,124,-234,-25\t# Japan - Minamitori-shima (Marcus Island)\n" +
                "Australian Geodetic 1966,4202,2,-133,-48,148\t# Australasia - Australia and PNG - AGD66\n" +
                "Australian Geodetic 1984,4203,2,-134,-48,149\t# Australia - AGD84\n" +
                "Australian Geocentric 1994 (GDA94),4283,11,0,0,0 # Australia - GDA94\n" +
                "Austrian,4312,3,594,84,471\t\t\t# MGI - Europe, Austria and former Yugoslavia\n" +
                "Bellevue (IGN),4714,14,-127,-769,472\t\t# Vanuatu - southern islands\n" +
                "Bermuda 1957,4216,4,-73,213,296\t\t\t# Bermuda\n" +
                "Bogota Observatory,4218,14,307,304,-318\t\t# Colombia\n" +
                "Campo Inchauspe,4221,14,-148,136,90\t\t# Argentina\n" +
                "Canton Astro 1966,4716,14,298,-304,-375\t\t# Kiribati - Phoenix Islands\n" +
                "Cape,4222,5,-136,-108,-292\t\t\t# Africa - Botswana and South Africa\n" +
                "Cape Canaveral,4717,4,-2,150,181\t\t# North America - Bahamas and USA - Florida\n" +
                "Carthage,4223,5,-263,6,431\t\t\t# Tunisia\n" +
                "CH-1903,4149,3,674,15,405\t\t\t# Europe - Liechtenstein and Switzerland\n" +
                "Chatham 1971,4672,14,175,-38,113\t\t# New Zealand - Chatham Islands\n" +
                "Chua Astro,4224,14,-134,229,-29\t\t\t# South America - Brazil ; N Paraguay\n" +
                "Corrego Alegre,4225,14,-206,172,-6\t\t# Brazil - Corrego Alegre\n" +
                "Djakarta (Batavia),4211,3,-377,681,-50\t\t# Indonesia - Java\n" +
                "DOS 1968,,14,230,-199,-752\t\t\t# Solomon Islands - Gizo Island : EPSG:4718 + EPSG:15805 (gcs.csv uses EPSG:15807)\n" +
                "Easter Island 1967,4719,14,211,147,111\t\t# Chile - Easter Island\n" +
                "Egypt,,14,-130,-117,-151\t\t\t# Egypt - EPSG code is 4199, but transformation parameters are missing in gcs.csv\n" +
                "European 1950,4230,14,-87,-98,-121\t\t# Europe\n" +
                "European 1950 (Mean France),,14,-87,-96,-120\t# Europe -France\n" +
                "European 1950 (Spain and Portugal),,14,-84,-107,-120 # Europe - Spain and Portugal\n" +
                "European 1979,4668,14,-86,-98,-119\t\t# Europe - west\n" +
                "Finland Hayford,4123,14,-78,-231,-97\t\t# Finland (KKJ)\n" +
                "Gandajika Base,4233,14,-133,-321,50\t\t# Maldives\n" +
                "Geodetic Datum 1949,4272,14,84,-22,209\t\t# New Zealand (NZGD49)\n" +
                "GGRS 87,4121,11,-199.87,74.79,246.62\t\t# Greece\n" +
                "Guam 1963,4675,4,-100,-248,259\t\t\t# Guam\n" +
                "GUX 1 Astro,4718,14,252,-209,-751\t\t# Solomon Islands - Guadalcanal Island\n" +
                "Hartebeeshoek94,4148,20,0,0,0\t\t\t# South Africa\n" +
                "Hermannskogel,3906,3,653,-212,449\t\t# Boznia and Herzegovina; Croatia; FYR Macedonia; Montenegro; Serbia; Slovenia (MGI 1901)\n" +
                "Hjorsey 1955,4658,14,-73,46,-86\t\t\t# Iceland\n" +
                "Hong Kong 1963,4739,14,-156,-271,-189\t\t# China - Hong Kong\n" +
                "Hu-Tzu-Shan,4236,14,-634,-549,-201\t\t# Taiwan\n" +
                "Indian Bangladesh,4682,6,289,734,257\t\t# Bangladesh (Gulshan 303)\n" +
                "Indian Thailand,4240,6,214,836,303\t\t# Thailand\n" +
                "Israeli,4281,23,-235,-85,264\t\t\t# Asia - Middle East - Israel, Jordan and Palestine Territory (Palestine 1923)\n" +
                "Ireland 1965,4299,1,506,-122,611\t\t# Europe - Ireland (Republic and Ulster)\n" +
                "ISTS 073 Astro 1969,4724,14,208,-435,-229\t# British Indian Ocean Territory - Diego Garcia\n" +
                "Johnston Island,4725,14,191,-77,-204\t\t# Johnston Island\n" +
                "Kandawala,4244,6,-97,787,86\t\t\t# Sri Lanka\n" +
                "Kerguelen Island,4698,14,145,-187,103\t\t# French Southern Territories - Kerguelen\n" +
                "Kertau 1948,4245,7,-11,851,5\t\t\t# Asia - Malaysia (west) and Singapore\n" +
                "L.C. 5 Astro,4726,4,42,124,147\t\t\t# Cayman Islands - Little Cayman and Cayman Brac\n" +
                "Liberia 1964,4251,5,-90,40,88\t\t\t# Liberia\n" +
                "Luzon Mindanao,,4,-133,-79,-72\t\t\t# Philippines - Mindanao (EPSG:4253 + EPSG:1162 Coordinate Transformation)\n" +
                "Luzon Philippines,4253,4,-133,-77,-51\t\t# Philippines - excluding Mindanao\n" +
                "Mahe 1971,4256,5,41,-220,-134\t\t\t# Seychelles\n" +
                "Marco Astro,4616,14,-289,-124,60\t\t# Portugal - Selvagens islands (Madeira)\n" +
                "Massawa,4262,3,639,405,60\t\t\t# Eritrea\n" +
                "Merchich,4261,5,31,146,47\t\t\t# Morocco\n" +
                "Midway Astro 1961,4727,14,912,-58,1227\t\t# Midway Islands - Sand and Eastern Islands\n" +
                "Minna,4263,5,-92,-93,122\t\t\t# Nigeria\n" +
                "NAD27 Alaska,,4,-5,135,172\t\t\t# Alaska (EPSG:4269 + EPSG:1176 Coordinate Transformation)\n" +
                "NAD27 Bahamas,,4,-4,154,178\t\t\t# Bahamas (EPSG:4269 + EPSG:1177 Coordinate Transformation)\n" +
                "NAD27 Canada,,4,-10,158,187\t\t\t# Canada (EPSG:4269 + EPSG:1172 Coordinate Transformation)\n" +
                "NAD27 Canal Zone,,4,0,125,201\t\t\t# Panama (EPSG:4269 + EPSG:1184 Coordinate Transformation)\n" +
                "NAD27 Caribbean,,4,-7,152,178\t\t\t# Caribbean\n" +
                "NAD27 Central,,4,0,125,194\t\t\t# Central America (EPSG:4269 + EPSG:1171 Coordinate Transformation)\n" +
                "NAD27 CONUS,,4,-8,160,176\t\t\t# Continental US (EPSG:4269 + EPSG:1173 Coordinate Transformation)\n" +
                "NAD27 Cuba,,4,-9,152,178\t\t\t# Cuba (EPSG:4269 + EPSG:1185 Coordinate Transformation)\n" +
                "NAD27 Greenland,,4,11,114,195\t\t\t# Greenland - Hayes Peninsula (EPSG:4269 + EPSG:1186 Coordinate Transformation)\n" +
                "NAD27 Mexico,,4,-12,130,190\t\t\t# Mexico (EPSG:4269 + EPSG:1187 Coordinate Transformation)\n" +
                "NAD27 San Salvador,,4,1,140,165\t\t\t# San Salvador (EPSG:4269 + EPSG:1178 Coordinate Transformation)\n" +
                "NAD83,4269,11,0,0,0\t\t\t\t# North America\n" +
                "Nahrwn Masirah Ilnd,,5,-247,-148,369\t\t# Oman - Masirah Island (EPSG:4270 + EPSG:1189)\n" +
                "Nahrwn Saudi Arbia,,5,-231,-196,482\t\t# Saudi Arabia (EPSG:4270 + EPSG:1190)\n" +
                "Nahrwn United Arab,,5,-249,-156,381\t\t# United Arab Emirates (UAE) (EPSG:4270 + EPSG:1191)\n" +
                "Naparima BWI,4271,14,-2,374,172\t\t\t# Trinidad and Tobago - Tobago\n" +
                "NGO1948,4273,27,315,-217,528\t\t\t# Norway\n" +
                "NTF France,4275,24,-168,-60,320\t\t\t# France\n" +
                "Norsk,4817,27,278,93,474\t\t\t# Norway (NGO 1948)\n" +
                "NZGD1949,4272,14,84,-22,209\t\t\t# New Zealand\n" +
                "NZGD2000,4167,20,0,0,0\t\t\t\t# New Zealand\n" +
                "Observatorio 1966,4182,14,-425,-169,81\t\t# Portugal - western Azores\n" +
                "Old Egyptian,4229,12,-130,110,-13\t\t# Egypt (1907)\n" +
                "Old Hawaiian,4135,4,61,-285,-181\t\t# USA - Hawaii\n" +
                "Oman,4232,5,-346,-1,224\t\t\t\t# Oman\n" +
                "Ord Srvy Grt Britn,4277,0,375,-111,431\t\t# UK - Great Britain; Isle of Man\n" +
                "Pico De Las Nieves,4728,14,-307,-92,127\t\t# Spain - Canary Islands\n" +
                "Pitcairn Astro 1967,4729,14,185,165,42\t\t# Pitcairn Island\n" +
                "Potsdam Rauenberg DHDN,4314,3,606,23,413\t# Germany\n" +
                "Prov So Amrican 1956,4248,14,-288,175,-376\t# South America - PSAD56\n" +
                "Prov So Chilean 1963,4254,14,16,196,93\t\t# South America - Tierra del Fuego\n" +
                "Puerto Rico,4139,4,11,72,-101\t\t\t# Caribbean - Puerto Rico and the Virgin Islands\n" +
                "Pulkovo 1942 (1),4284,15,28,-130,-95\t\t# Europe - FSU\n" +
                "Pulkovo 1942 (2),4284,15,28,-130,-95\t\t# Europe - FSU\n" +
                "Qatar National,4285,14,-128,-283,22\t\t# Qatar\n" +
                "Qornoq,4287,14,164,138,-189\t\t\t# Greenland\n" +
                "Reunion,4626,14,94,-948,-1262\t\t\t# France - Reunion Island\n" +
                "Rijksdriehoeksmeting,4289,3,593,26,478\t\t# Netherlands\n" +
                "Rome 1940,4806,14,-225,-65,9\t\t\t# Italy - including San Marino and Vatican\n" +
                "RT 90,4124,3,498,-36,568\t\t\t# Sweden\n" +
                "S42,4179,15,28,-121,-77\t\t\t\t# Europe - eastern - S-42\n" +
                "Santo (DOS),4730,14,170,42,84\t\t\t# Vanuatu - northern islands\n" +
                "Sao Braz,4184,14,-203,141,53\t\t\t# Portugal - eastern Azores\n" +
                "Sapper Hill 1943,4292,14,-355,16,74\t\t# Falkland Islands\n" +
                "Schwarzeck,4293,21,616,97,-251\t\t\t# Namibia\n" +
                "South American 1969,4291,16,-57,1,-41\t\t# South America - SAD69\n" +
                "South Asia,,8,7,-10,-26\t\t\t\t# Singapore (unknown EPSG code)\n" +
                "Southeast Base,4615,14,-499,-249,314\t\t# Porto Santo and Madeira Islands\n" +
                "Southwest Base,4183,14,-104,167,-38\t\t# Faial, Graciosa, Pico, Sao Jorge and Terceira\n" +
                "Timbalai 1948,4298,6,-689,691,-46\t\t# Asia - Brunei and East Malaysia\n" +
                "Tokyo,4301,3,-128,481,664\t\t\t# Asia - Japan and Korea\n" +
                "Tristan Astro 1968,4734,14,-632,438,-609\t# St Helena - Tristan da Cunha\n" +
                "Viti Levu 1916,4731,5,51,391,-36\t\t# Fiji - Viti Levu\n" +
                "Wake-Eniwetok 1960,4732,13,101,52,-39\t\t# Marshall Islands - Eniwetok, Kwajalein and Wake islands\n" +
                "WGS 72,4322,19,0,0,5\t\t\t\t# World\n" +
                "WGS 84,4326,20,0,0,0\t\t\t\t# World\n" +
                "Yacare,4309,14,-155,171,37\t\t\t# Uruguay\n" +
                "Zanderij,4311,14,-265,120,-358\t\t\t# Suriname";

        String[] lines = s.split("\n");

        //        put("WGS 84", createGeodeticDatum("WGS 84", WGS_84, DefaultGeodeticDatum.WGS84, 0, 0, 0));

        Map<String, String> el = new HashMap<>();
        el.put("0", "AIRY_1830");
        el.put("1", "MODIFIED_AIRY");
        el.put("2", "AUSTRALIAN_NATIONAL");
        el.put("3", "BESSEL_1841");
        el.put("4", "CLARKE_1866");
        el.put("5", "CLARKE_1880");
        el.put("6", "EVEREST_INDIA_1830");
        el.put("7", "EVEREST_1948");
        el.put("8", "MODIFIED_FISCHER_1960");
        el.put("9", "EVEREST_PAKISTAN");
        el.put("10", "INDONESIAN_1974");
        el.put("11", "GRS_80");
        el.put("12", "HELMERT_1906");
        el.put("13", "HOUGH_1960");
        el.put("14", "INTERNATIONAL_1924");
        el.put("15", "KRASSOVSKY_1940");
        el.put("16", "SOUTH_AMERICAN_1969");
        el.put("17", "EVEREST_MALAYSIA_1969");
        el.put("18", "EVEREST_SABAH_SARAWAK");
        el.put("19", "WGS_72");
        el.put("20", "WGS_84");
        el.put("21", "BESSEL_1841_NAMIBIA");
        el.put("22", "EVEREST_INDIA_1956");
        el.put("23", "CLARKE_1880_PALESTINE");
        el.put("24", "CLARKE_1880_IGN");
        el.put("25", "HAYFORD_1909");
        el.put("26", "CLARKE_1858");
        el.put("27", "BESSEL_1841_NORWAY");
        el.put("28", "PLESSIS_1817_FRANCE");
        el.put("29", "HAYFORD_1924");

        for (int i = 0; i < lines.length; ++i) {
            String line = lines[i];

            String values[] = line.substring(0, line.indexOf("#")).split(",", -1);

            String s0 = values[0];
            String s1 = values[1];
            String s2 = values[2];
            String s3 = values[3];
            String s4 = values[4];
            String s5 = values[5].trim();

            System.out.println("put(\"" + s0 + "\", createGeodeticDatum(\"" + s0 + "\", " + el.get(s2) + ", DefaultGeodeticDatum.WGS84, " + s3 + ", " + s4 + ", " + s5 + "));");

            int asd = 0;
            int asdf = asd;
        }
    }
}