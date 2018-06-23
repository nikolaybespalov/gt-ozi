package com.github.nikolaybespalov.gtoziexplorermap;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Iterator;

// https://trac.osgeo.org/gdal/browser/sandbox/klokan/ozf/ozf-binary-format-description.txt
// http://www.oziexplorer3.com/eng/help/map_file_format.html
public final class OzfImageReader extends ImageReader {
    ImageInputStream stream = null;
    byte k2;
    boolean isOzf3 = false;
    int width;
    int height;
    int bpp;
    int depth;

    static final byte abyKey[] = {
            (byte) 0x2D, (byte) 0x4A, (byte) 0x43, (byte) 0xF1, (byte) 0x27, (byte) 0x9B, (byte) 0x69, (byte) 0x4F,
            (byte) 0x36, (byte) 0x52, (byte) 0x87, (byte) 0xEC, (byte) 0x5F, (byte) 0x42, (byte) 0x53, (byte) 0x22,
            (byte) 0x9E, (byte) 0x8B, (byte) 0x2D, (byte) 0x83, (byte) 0x3D, (byte) 0xD2, (byte) 0x84, (byte) 0xBA,
            (byte) 0xD8, (byte) 0x5B
    };

    public OzfImageReader(ImageReaderSpi imageReaderSpi) {
        super(imageReaderSpi);
    }

    @Override
    public int getNumImages(boolean b) throws IOException {
        return 1;
    }

    @Override
    public int getWidth(int i) throws IOException {
        return 0;
    }

    @Override
    public int getHeight(int i) throws IOException {
        return 0;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int i) throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public IIOMetadata getImageMetadata(int i) throws IOException {
        return null;
    }

    @Override
    public BufferedImage read(int i, ImageReadParam imageReadParam) throws IOException {
        return null;
    }

    @Override
    public void setInput(Object input,
                         boolean seekForwardOnly,
                         boolean ignoreMetadata) {
        super.setInput(input, seekForwardOnly, ignoreMetadata);

        if (!(input instanceof ImageInputStream)) {
            throw new IllegalArgumentException
                    ("input not an ImageInputStream!");
        }

        stream = (ImageInputStream) input;
        stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);

        try {
            byte[] header = new byte[14];

            stream.readFully(header);

            isOzf3 = (header[0] == (byte) 0x80) && (header[1] == (byte) 0x77);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

        if (isOzf3) {
            try {
                int n = stream.readByte() & 0xFF;

                if (n < 0x94) {
                    throw new IllegalArgumentException("");
                }

                byte[] b = new byte[n];

                stream.readFully(b);

                byte k = b[0x93];

                k2 = (byte) ((k + 0x8A) & 0xFF);

                stream.skipBytes(4);
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        try {
            byte[] header2 = new byte[40];

            if (stream.read(header2) != 40) {
                throw new IllegalArgumentException();
            }

            if (isOzf3) {
                decrypt(header2, 0, 40, k2);
            }

            int headerSize = readInt(header2, 0);

            if (headerSize != 40) {
                throw new IllegalArgumentException();
            }

            width = readInt(header2, 4);
            height = readInt(header2, 8);
            depth = readShort(header2, 12);
            bpp = readShort(header2, 14);

            int asd = 0;
            int asdf = asd;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    protected static int readInt(byte[] bytes, int offset) {
        return Ints.fromBytes(bytes[3 + offset], bytes[2 + offset], bytes[1 + offset], bytes[offset]);
    }

    protected static short readShort(byte[] bytes, int offset) {
        return Shorts.fromBytes(bytes[1 + offset], bytes[offset]);
    }

    protected static void decrypt(byte[] bytes, int offset, int length, byte key) {
        for (int i = offset; i < length; ++i) {
            bytes[i] = (byte) (((int) bytes[i] ^ (int) (abyKey[i % abyKey.length] + key)) & 0xFF);
        }
    }
}
