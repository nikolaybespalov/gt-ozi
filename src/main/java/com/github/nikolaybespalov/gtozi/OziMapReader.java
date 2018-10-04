package com.github.nikolaybespalov.gtozi;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataSourceException;
import org.geotools.data.FileGroupProvider;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.image.io.ImageIOExt;
import org.geotools.metadata.iso.spatial.PixelTranslation;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.coverage.grid.Format;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.TransformException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.geotools.util.logging.Logging.getLogger;

@SuppressWarnings("WeakerAccess")
public final class OziMapReader extends AbstractGridCoverage2DReader {
    private static final Logger LOGGER = getLogger(OziMapReader.class);

    static {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    private final OziMapFileReader oziMapFileReader;
    private final ImageReaderSpi imageReaderSpi;

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

            coverageName = oziMapFileReader.getTitle();

            crs = oziMapFileReader.getCoordinateReferenceSystem();
            raster2Model = PixelTranslation.translate(oziMapFileReader.getGrid2Crs(), PixelInCell.CELL_CORNER, PixelInCell.CELL_CENTER);

            File imageFile = oziMapFileReader.getImageFile();

            inStreamSPI = ImageIOExt.getImageInputStreamSPI(imageFile);

            try (ImageInputStream inStream = inStreamSPI.createInputStreamInstance(
                    imageFile, ImageIO.getUseCache(), ImageIO.getCacheDirectory())) {
                ImageReader imageReader = ImageIOExt.getImageioReader(inStream);
                imageReader.setInput(inStream);

                imageReaderSpi = imageReader.getOriginatingProvider();

                int hrWidth = imageReader.getWidth(0);
                int hrHeight = imageReader.getHeight(0);
                final Rectangle actualDim = new Rectangle(0, 0, hrWidth, hrHeight);

                originalGridRange = new GridEnvelope2D(actualDim);

                final AffineTransform tempTransform =
                        new AffineTransform((AffineTransform) raster2Model);
                tempTransform.translate(-0.5, -0.5);

                originalEnvelope =
                        CRS.transform(
                                ProjectiveTransform.create(tempTransform),
                                new GeneralEnvelope(actualDim));
                originalEnvelope.setCoordinateReferenceSystem(crs);
            }
        } catch (DataSourceException e) {
            throw e;
        } catch (IOException | TransformException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public Format getFormat() {
        return new OziMapFormat();
    }

    @Override
    public GridCoverage2D read(GeneralParameterValue[] params) throws IllegalArgumentException, IOException {
        Hints readHints = new Hints();

        Integer imageChoice = 0;
        final ImageReadParam readP = new ImageReadParam();

        if (params != null) {
            for (GeneralParameterValue param1 : params) {
                final ParameterValue param = (ParameterValue) param1;
                final String name = param.getDescriptor().getName().getCode();

                if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString())) {
                    final GridGeometry2D gg = (GridGeometry2D) param.getValue();

                    try {
                        readP.setSourceRegion(gg.worldToGrid(gg.getEnvelope2D()));
                    } catch (TransformException e) {
                        LOGGER.warning("Filed to setup source region: " + e.getLocalizedMessage());
                    }
                } else if (name.equals(AbstractGridFormat.SUGGESTED_TILE_SIZE.getName().toString())) {
                    String suggestedTileSize = (String) param.getValue();

                    String[] tileSizeValues = suggestedTileSize.split(",");

                    int tileWidth;
                    int tileHeight;

                    if (tileSizeValues.length == 1) {
                        tileWidth = tileHeight = Integer.parseInt(tileSizeValues[0]);
                    } else if (tileSizeValues.length == 2) {
                        tileWidth = Integer.parseInt(tileSizeValues[0]);
                        tileHeight = Integer.parseInt(tileSizeValues[1]);
                    } else {
                        LOGGER.warning("Failed to parse tile size: " + suggestedTileSize);
                        continue;
                    }

                    final ImageLayout layout = new ImageLayout();
                    layout.setTileGridXOffset(0);
                    layout.setTileGridYOffset(0);
                    layout.setTileWidth(tileWidth);
                    layout.setTileHeight(tileHeight);
                    readHints.add(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));
                }
            }
        }

        final ParameterBlock pbjRead = new ParameterBlock();

        pbjRead.add(
                inStreamSPI.createInputStreamInstance(
                        oziMapFileReader.getImageFile(), ImageIO.getUseCache(), ImageIO.getCacheDirectory()));

        pbjRead.add(imageChoice);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add(null);
        pbjRead.add(null);
        pbjRead.add(readP);
        pbjRead.add(imageReaderSpi.createReaderInstance());
        final RenderedOp coverageRaster = JAI.create("ImageRead", pbjRead, readHints);

        AffineTransform rescaledRaster2Model = getRescaledRasterToModel(coverageRaster);

        return createImageCoverage(coverageRaster, ProjectiveTransform.create(rescaledRaster2Model));
    }

    @Override
    protected List<FileGroupProvider.FileGroup> getFiles() throws IOException {
        List<FileGroupProvider.FileGroup> files = super.getFiles();

        if (files.size() > 0) {
            files.get(0).setSupportFiles(Collections.singletonList(oziMapFileReader.getImageFile()));
        }

        return files;
    }

    // TODO: определить остальные методы
}
