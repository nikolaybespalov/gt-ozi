package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import static org.junit.jupiter.api.Assertions.fail;

class TestUtils {
    static void assertCrsEquals(CoordinateReferenceSystem crs1, CoordinateReferenceSystem crs2) {
        if (!CRS.equalsIgnoreMetadata(crs1, crs2)) {
            fail("CRS differ: " + System.getProperty("line.separator") +
                    crs1 + System.getProperty("line.separator") +
                    crs2);
        }
    }

    static void assertReaderEquals(AbstractGridCoverage2DReader r1, AbstractGridCoverage2DReader r2) {
        if (!CRS.equalsIgnoreMetadata(r1.getCoordinateReferenceSystem(), r2.getCoordinateReferenceSystem())) {
            fail("CRS differ: " + System.getProperty("line.separator") +
                    r1.getCoordinateReferenceSystem() + System.getProperty("line.separator") +
                    r2.getCoordinateReferenceSystem());
        }

        if (!r1.getOriginalEnvelope().equals(r2.getOriginalEnvelope(), 0.1, true)) {
            fail("Original envelope differ: " + System.getProperty("line.separator") +
                    r1.getOriginalEnvelope() + System.getProperty("line.separator") +
                    r2.getOriginalEnvelope());
        }
    }
}
