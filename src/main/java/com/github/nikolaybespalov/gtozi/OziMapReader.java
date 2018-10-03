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
import org.geotools.geometry.jts.JTS;
import org.geotools.image.io.ImageIOExt;
import org.geotools.metadata.iso.spatial.PixelTranslation;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.coverage.grid.Format;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
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

@SuppressWarnings("WeakerAccess")
public final class OziMapReader extends AbstractGridCoverage2DReader {
    static {
        System.setProperty("org.geotools.referencing.forceXY", "true");
    }

    private final OziMapFileReader oziMapFileReader;
    private final ImageReaderSpi imageReaderSpi;
    private final MathTransform model2Raster;

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
            model2Raster = raster2Model.inverse();

            File imageFile = oziMapFileReader.getImageFile();

            inStreamSPI = ImageIOExt.getImageInputStreamSPI(imageFile);

            inStream = inStreamSPI.createInputStreamInstance(imageFile, ImageIO.getUseCache(), ImageIO.getCacheDirectory());

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
        // /////////////////////////////////////////////////////////////////////
        //
        // do we have parameters to use for reading from the specified source
        //
        // /////////////////////////////////////////////////////////////////////
        GeneralEnvelope requestedEnvelope = null;
        //Rectangle dim = null;
        int[] suggestedTileSize = null;
        if (params != null) {
            // /////////////////////////////////////////////////////////////////////
            //
            // Checking params
            //
            // /////////////////////////////////////////////////////////////////////
            for (GeneralParameterValue param1 : params) {
                final ParameterValue param = (ParameterValue) param1;
                final String name = param.getDescriptor().getName().getCode();
                if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName().toString())) {
                    final GridGeometry2D gg = (GridGeometry2D) param.getValue();
                    requestedEnvelope = new GeneralEnvelope((Envelope) gg.getEnvelope2D());
                    //dim = gg.getGridRange2D().getBounds();
                } else if (name.equals(AbstractGridFormat.SUGGESTED_TILE_SIZE.getName().toString())) {
                    String suggestedTileSize_ = (String) param.getValue();
                    if (suggestedTileSize_ != null && suggestedTileSize_.length() > 0) {
                        suggestedTileSize_ = suggestedTileSize_.trim();
                        int commaPosition = suggestedTileSize_.indexOf(",");
                        if (commaPosition < 0) {
                            int tileDim = Integer.parseInt(suggestedTileSize_);
                            suggestedTileSize = new int[]{tileDim, tileDim};
                        } else {
                            int tileW =
                                    Integer.parseInt(
                                            suggestedTileSize_.substring(0, commaPosition));
                            int tileH =
                                    Integer.parseInt(
                                            suggestedTileSize_.substring(commaPosition + 1));
                            suggestedTileSize = new int[]{tileW, tileH};
                        }
                    }
                }
            }
        }

        //
        // set params
        //
        Integer imageChoice = 0;
        final ImageReadParam readP = new ImageReadParam();

        if (requestedEnvelope != null) {
            try {
                com.vividsolutions.jts.geom.Envelope sourceGridRange = JTS.transform(new com.vividsolutions.jts.geom.Envelope(
                        requestedEnvelope.getMinimum(0),
                        requestedEnvelope.getMaximum(0),
                        requestedEnvelope.getMinimum(1),
                        requestedEnvelope.getMaximum(1)), model2Raster);

                readP.setSourceRegion(new Rectangle((int) sourceGridRange.getMinX(), (int) sourceGridRange.getMinY(), (int) sourceGridRange.getMaxX() - (int) sourceGridRange.getMinX(), (int) sourceGridRange.getMaxY() - (int) sourceGridRange.getMinY()));
            } catch (TransformException e) {
                //return null;
            }
        }

        Hints newHints = null;
        if (suggestedTileSize != null) {
            newHints = hints.clone();
            final ImageLayout layout = new ImageLayout();
            layout.setTileGridXOffset(0);
            layout.setTileGridYOffset(0);
            layout.setTileHeight(suggestedTileSize[1]);
            layout.setTileWidth(suggestedTileSize[0]);
            newHints.add(new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout));
        }

        final ParameterBlock pbjRead = new ParameterBlock();
        pbjRead.add(
                inStreamSPI != null
                        ? inStreamSPI.createInputStreamInstance(
                        oziMapFileReader.getImageFile(), ImageIO.getUseCache(), ImageIO.getCacheDirectory())
                        : ImageIO.createImageInputStream(oziMapFileReader.getImageFile()));
        // pbjRead.add(wmsRequest ? ImageIO
        // .createImageInputStream(((URL) source).openStream()) : ImageIO
        // .createImageInputStream(source));
        pbjRead.add(imageChoice);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add(Boolean.FALSE);
        pbjRead.add(null);
        pbjRead.add(null);
        pbjRead.add(readP);
        pbjRead.add(imageReaderSpi.createReaderInstance());
        final RenderedOp coverageRaster = JAI.create("ImageRead", pbjRead, newHints);

        //imageReaderSpi.createReaderInstance().read(imageChoice, readP);

        // /////////////////////////////////////////////////////////////////////
        //
        // BUILDING COVERAGE
        //
        // /////////////////////////////////////////////////////////////////////
        AffineTransform rasterToModel = getRescaledRasterToModel(coverageRaster);
        return createImageCoverage(coverageRaster, ProjectiveTransform.create(rasterToModel));
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
