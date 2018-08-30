package com.github.nikolaybespalov.gtozi;

import org.apache.commons.io.FilenameUtils;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
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
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class OziMapReader extends AbstractGridCoverage2DReader {
    private WorldImageReader worldImageReader;

    @SuppressWarnings("WeakerAccess")
    public OziMapReader(Object input) throws IOException, FactoryException, TransformException {
        this(input, null);
    }

    @SuppressWarnings("WeakerAccess")
    public OziMapReader(Object input, Hints uHints) throws IOException, FactoryException, TransformException {
        super(input, uHints);

        File inputFile = getSourceAsFile();

        OziMapFileReader oziMapFileReader = new OziMapFileReader(inputFile);

        CoordinateReferenceSystem crs = oziMapFileReader.getCoordinateReferenceSystem();
        MathTransform grid2Crs = oziMapFileReader.getGrid2Crs();

        String baseName = FilenameUtils.removeExtension(((File) input).getAbsolutePath());

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

    // TODO: определить остальные методы
}
