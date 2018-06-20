package com.github.nikolaybespalov.gtoziexplorermap;

import java.util.Optional;

public class OziProjectionSetup {
    public static final OziProjectionSetup EMPTY = new OziProjectionSetup();
    private Optional<Double> latitudeOrigin = Optional.empty();
    private Optional<Double> longitudeOrigin = Optional.empty();
    private Optional<Double> kFactor = Optional.empty();
    private Optional<Double> falseEasting = Optional.empty();
    private Optional<Double> falseNorthing = Optional.empty();
    private Optional<Double> latitude1 = Optional.empty();
    private Optional<Double> latitude2 = Optional.empty();
    private Optional<Double> height = Optional.empty();
    private Optional<String> sat = Optional.empty();
    private Optional<String> path = Optional.empty();

    public Optional<Double> getLatitudeOrigin() {
        return latitudeOrigin;
    }

    public void setLatitudeOrigin(Double latitudeOrigin) {
        this.latitudeOrigin = Optional.of(latitudeOrigin);
    }

    public Optional<Double> getLongitudeOrigin() {
        return longitudeOrigin;
    }

    public void setLongitudeOrigin(Double longitudeOrigin) {
        this.longitudeOrigin = Optional.of(longitudeOrigin);
    }

    public Optional<Double> getkFactor() {
        return kFactor;
    }

    public void setkFactor(Double kFactor) {
        this.kFactor = Optional.of(kFactor);
    }

    public Optional<Double> getFalseEasting() {
        return falseEasting;
    }

    public void setFalseEasting(Double falseEasting) {
        this.falseEasting = Optional.of(falseEasting);
    }

    public Optional<Double> getFalseNorthing() {
        return falseNorthing;
    }

    public void setFalseNorthing(Optional<Double> falseNorthing) {
        this.falseNorthing = falseNorthing;
    }

    public Optional<Double> getLatitude1() {
        return latitude1;
    }

    public void setLatitude1(Optional<Double> latitude1) {
        this.latitude1 = latitude1;
    }

    public Optional<Double> getLatitude2() {
        return latitude2;
    }

    public void setLatitude2(Optional<Double> latitude2) {
        this.latitude2 = latitude2;
    }

    public Optional<Double> getHeight() {
        return height;
    }

    public void setHeight(Optional<Double> height) {
        this.height = height;
    }

    public Optional<String> getSat() {
        return sat;
    }

    public void setSat(Optional<String> sat) {
        this.sat = sat;
    }

    public Optional<String> getPath() {
        return path;
    }

    public void setPath(Optional<String> path) {
        this.path = path;
    }
}
