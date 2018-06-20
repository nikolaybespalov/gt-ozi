package com.github.nikolaybespalov.gtoziexplorermap;

import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class OziProjectionSetupTest {
    OziProjectionSetup projectionSetup = new OziProjectionSetup();

    @Test
    public void getLatitudeOrigin() {
        assertFalse(projectionSetup.getLatitudeOrigin().isPresent());

        assertEquals(Optional.empty(), projectionSetup.getLatitudeOrigin());
    }

    @Test
    public void setLatitudeOrigin() {
        assertFalse(projectionSetup.getLatitudeOrigin().isPresent());

        projectionSetup.setLatitudeOrigin(123.0);

        assertTrue(projectionSetup.getLatitudeOrigin().isPresent());
        assertEquals(123.0, projectionSetup.getLatitudeOrigin().get(), 0.1);
    }

    @Test
    public void getLongitudeOrigin() {
    }

    @Test
    public void setLongitudeOrigin() {
    }

    @Test
    public void getKFactor() {
    }

    @Test
    public void setKFactor() {
    }

    @Test
    public void getFalseEasting() {
    }

    @Test
    public void setFalseEasting() {
    }

    @Test
    public void getFalseNorthing() {
    }

    @Test
    public void setFalseNorthing() {
    }

    @Test
    public void getLatitude1() {
    }

    @Test
    public void setLatitude1() {
    }

    @Test
    public void getLatitude2() {
    }

    @Test
    public void setLatitude2() {
    }

    @Test
    public void getHeight() {
    }

    @Test
    public void setHeight() {
    }

    @Test
    public void getSat() {
    }

    @Test
    public void setSat() {
    }

    @Test
    public void getPath() {
    }

    @Test
    public void setPath() {
    }
}