package com.github.nikolaybespalov.gtozi;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.referencing.CRS;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.fail;

class TestUtils {
    static URL getResourceAsUrl(String resourceName) {
        ClassLoader loader = MoreObjects.firstNonNull(Thread.currentThread().getContextClassLoader(), TestUtils.class.getClassLoader());
        URL url = loader.getResource(resourceName);
        Preconditions.checkArgument(url != null, "resource %s not found.", resourceName);
        return url;
    }

    static File getResourceAsFile(String resourceName) {
        ClassLoader loader = MoreObjects.firstNonNull(Thread.currentThread().getContextClassLoader(), TestUtils.class.getClassLoader());
        URL url = loader.getResource(resourceName);
        Preconditions.checkArgument(url != null, "resource %s not found.", resourceName);
        return FileUtils.toFile(url);
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
