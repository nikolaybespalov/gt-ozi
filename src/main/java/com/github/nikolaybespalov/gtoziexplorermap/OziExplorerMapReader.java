package com.github.nikolaybespalov.gtoziexplorermap;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@SuppressWarnings("unused")
public class OziExplorerMapReader {
    private static final int DATUM_INDEX = 4;
    private static final int DATUM_NAME_INDEX = 0;
    private final String header = "ziExplorer Map Data File";
    private String version;
    private OziDatum datum = null;

    public OziExplorerMapReader(Path path) {
        try {
            String[] lines = Files.lines(path).toArray(String[]::new);

            if (lines.length < 10) {
                throw new IllegalArgumentException("asd");
            }

            if (!lines[0].startsWith(header)) {
                throw new IllegalArgumentException("asd");
            }

            String[] datumParameters = lines[DATUM_INDEX].split(",");

            if (datumParameters.length < 5) {
                throw new IllegalArgumentException("asd");
            }

            String datumName = datumParameters[DATUM_NAME_INDEX];

        } catch (IOException e) {
        }
    }
}
