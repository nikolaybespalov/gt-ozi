package com.github.nikolaybespalov.gtoziexplorermap;

@SuppressWarnings("unused")
public enum OziEllipsoid {
    WGS_84(20, "WGS 84", 6378137.0, 298.257223563);
    private int code;
    private String name;
    private double a;
    private double invf;

    OziEllipsoid(int code, String name, double a, double invf) {
        this.code = code;
        this.name = name;
        this.a = a;
        this.invf = invf;
    }
}
