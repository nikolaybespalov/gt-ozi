package com.github.nikolaybespalov.gtoziexplorermap;

import com.github.davidcarboni.ResourceUtils;
import org.junit.Test;

import java.io.IOException;

public class OziMapFileTest {

    @Test
    public void testDemo1() throws IOException {
        new OziMapFile(ResourceUtils.getFile("/Maps/Demo1.map").toPath());
    }
}