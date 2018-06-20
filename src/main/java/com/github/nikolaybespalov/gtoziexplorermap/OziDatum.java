package com.github.nikolaybespalov.gtoziexplorermap;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("unused")
public class OziDatum {

    public static final OziDatum WGS_84 = new OziDatum("WGS 84", 4326, OziEllipsoid.WGS_84, 0, 0, 0);

    //private static final Map<String, OziDatum> ENUM_MAP;
    private String name;
    private int epsgCode;
    private OziEllipsoid ellipsoid;
    private int deltaX;
    private int deltaY;
    private int deltaZ;
    public static final List<OziDatum> VALUES = Arrays.asList(
            WGS_84
    );

//    static {
//        Map<String, OziDatum> map = new ConcurrentHashMap<>();
//        for (OziDatum instance : OziDatum.values()) {
//            map.put(instance.getName(), instance);
//        }
//        ENUM_MAP = Collections.unmodifiableMap(map);
//    }

    public OziDatum(String name, int epsgCode, OziEllipsoid ellipsoid, int deltaX, int deltaY, int deltaZ) {
        this.name = name;
        this.epsgCode = epsgCode;
        this.ellipsoid = ellipsoid;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.deltaZ = deltaZ;
    }

    public String getName() {
        return name;
    }

    public int getEpsgCode() {
        return epsgCode;
    }

    public OziEllipsoid getEllipsoid() {
        return ellipsoid;
    }

    public int getDeltaX() {
        return deltaX;
    }

    public int getDeltaY() {
        return deltaY;
    }

    public int getDeltaZ() {
        return deltaZ;
    }
}
