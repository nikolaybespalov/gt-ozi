package com.github.nikolaybespalov.gtoziexplorermap;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

public class OziMapFileTest {

    @Test
    public void testDemo1() throws IOException {
        OziMapFile oziMapFile = new OziMapFile(Paths.get("c:\\Users\\Nikolay Bespalov\\Documents\\githab.com\\nikolaybespalov\\gt-oziexplorermap\\src\\test\\resources\\Maps\\Demo1.map"));

        assertNotNull(oziMapFile.getProjection());
        assertNotNull(oziMapFile.getProjection().getName());
        assertNotNull(oziMapFile.getProjectionSetup());
    }
}