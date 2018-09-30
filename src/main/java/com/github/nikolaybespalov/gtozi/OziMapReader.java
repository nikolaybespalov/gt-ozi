package com.github.nikolaybespalov.gtozi;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.FileGroupProvider;
import org.geotools.data.WorldFileWriter;
import org.geotools.factory.Hints;
import org.geotools.gce.image.WorldImageReader;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.metadata.iso.spatial.PixelTranslation;
import org.geotools.referencing.wkt.Formattable;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("WeakerAccess")
public final class OziMapReader extends AbstractGridCoverage2DReader {
    static {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    private OziMapFileReader oziMapFileReader;
    private WorldImageReader worldImageReader;

    public OziMapReader(Object input) throws DataSourceException {
        this(input, null);
    }

    public OziMapReader(Object input, Hints uHints) throws DataSourceException {
        super(input, uHints);

        try {
            File inputFile = getSourceAsFile();

            if (inputFile == null) {
                throw new IllegalArgumentException("No input stream for the provided source: " + source);
            }

            oziMapFileReader = new OziMapFileReader(inputFile);

            CoordinateReferenceSystem crs = oziMapFileReader.getCoordinateReferenceSystem();
            MathTransform grid2Crs = oziMapFileReader.getGrid2Crs();

            String baseName = FilenameUtils.removeExtension(oziMapFileReader.getImageFile().getAbsolutePath());

            Path wldPath = Paths.get(baseName + ".wld");

            if (wldPath.toFile().exists()) {
                Files.delete(Paths.get(baseName + ".wld"));
            }

            Files.createFile(wldPath);

            new WorldFileWriter(wldPath.toFile(), PixelTranslation.translate(grid2Crs, PixelInCell.CELL_CORNER, PixelInCell.CELL_CENTER)); // Нет необходимости закрывать WorldFileWriter

            Path prjPath = Paths.get(baseName + ".prj");

            if (prjPath.toFile().exists()) {
                Files.delete(Paths.get(baseName + ".prj"));
            }

            Files.createFile(prjPath);

            final FileWriter prjWriter = new FileWriter(prjPath.toFile());

            prjWriter.write(((Formattable) crs).toWKT(Citations.OGC, 2));

            prjWriter.close();

            worldImageReader = new WorldImageReader(oziMapFileReader.getImageFile());
        } catch (DataSourceException e) {
            throw e;
        } catch (IOException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public Format getFormat() {
        return new OziMapFormat();
    }

    @Override
    public GridCoverage2D read(GeneralParameterValue[] params) throws IllegalArgumentException, IOException {
        return worldImageReader.read(params);
    }

    @Override
    public GridEnvelope getOriginalGridRange() {
        return worldImageReader.getOriginalGridRange();
    }

    @Override
    public GeneralEnvelope getOriginalEnvelope() {
        return worldImageReader.getOriginalEnvelope();
    }

    @Override
    public GeneralEnvelope getOriginalEnvelope(String coverageName) {
        return worldImageReader.getOriginalEnvelope(coverageName);
    }

    @Override
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return worldImageReader.getCoordinateReferenceSystem();
    }

    @Override
    protected List<FileGroupProvider.FileGroup> getFiles() throws IOException {
        List<FileGroupProvider.FileGroup> files = super.getFiles();

        if (files.size() > 0) {
            files.get(0).setSupportFiles(Collections.singletonList(oziMapFileReader.getImageFile()));
        }

        return files;
    }

    @Override
    public void dispose() {
        worldImageReader.dispose();
    }

    // TODO: определить остальные методы
}
