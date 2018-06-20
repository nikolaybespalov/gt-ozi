package com.github.nikolaybespalov.gtoziexplorermap;

import java.util.Optional;

public class OziProjection {
    public static final String LatitudeLongitude = "Latitude/Longitude";
    public static final String Mercator = "Mercator";

    private final String name;
    private Optional<Boolean> polyCal = Optional.empty();
    private Optional<Boolean> autoCalOnly = Optional.empty();
    private Optional<Boolean> bsbUseWpx = Optional.empty();

    public OziProjection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Optional<Boolean> getPolyCal() {
        return polyCal;
    }

    public void setPolyCal(Boolean polyCal) {
        this.polyCal = Optional.of(polyCal);
    }

    public Optional<Boolean> getAutoCalOnly() {
        return autoCalOnly;
    }

    public void setAutoCalOnly(Boolean autoCalOnly) {
        this.autoCalOnly = Optional.of(autoCalOnly);
    }

    public Optional<Boolean> getBsbUseWpx() {
        return bsbUseWpx;
    }

    public void setBsbUseWpx(Boolean bsbUseWpx) {
        this.bsbUseWpx = Optional.of(bsbUseWpx);
    }
}
