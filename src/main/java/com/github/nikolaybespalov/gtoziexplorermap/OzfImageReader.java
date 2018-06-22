package com.github.nikolaybespalov.gtoziexplorermap;

import it.geosolutions.imageio.stream.input.compressed.InflaterImageInputStream;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public final class OzfImageReader extends ImageReader {
    ImageInputStream stream = null;

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

                byte k2 = (byte) ((k + 0x8A) & 0xFF);

                stream.skipBytes(4);

                stream = new ImageInputStreamImpl() {
                    @Override
                    public int read() throws IOException {
                        return 0;
                    }

                    @Override
                    public int read(byte[] bytes, int offset, int length) throws IOException {
                        int n = ((ImageInputStream) input).read(bytes, offset, length);

                        decrypt(bytes, offset, length, k2);

                        return n;
                    }
                };

                this.stream = new InflaterImageInputStream((ImageInputStream) input, new Inflater(){
                    byte[] bytes;
                    int offset;
                    int length;

                    @Override
                    public void setInput(byte[] bytes, int offset, int length) {
                        super.setInput(bytes, offset, length);

                        this.bytes = bytes;
                        this.offset = offset;
                        this.length = length;
                    }

                    @Override
                    public int inflate(byte[] bytes, int offset, int length) throws DataFormatException {
                        decrypt(this.bytes, this.offset, this.length, k2);

                        System.arraycopy(this.bytes, this.offset, bytes, offset, length);

                        return length;
                    }
                });

                //CipherInputStream asd = new CipherInputStream(null, null);

                //DataInputStream dataInputStream = new DataInputStream();

                //int i = stream.readInt();
                //int i2 = stream.readInt();

//                byte[] bb = new byte[4];
//
//                stream.readFully(bb);
//
//                byte[] bbb = decrypt(bb, k);
//
//                int i3 = Ints.fromByteArray(new byte[]{bbb[3], bbb[2], bbb[1], bbb[0]});


                int asd = 0;
                int asdf = asd;
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        } else {

        }

        try {
            //stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            int headerSize = stream.readInt();

            if (headerSize != 40) {
                throw new IllegalArgumentException();
            }

            int asd = 0;
            int asdf = asd;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void readHeader() {

    }

    private byte[] readFirst14Bytes() throws IOException {
        byte[] header = new byte[14];

        stream.readFully(header);

        return header;
    }

    private static void decrypt(byte[] bytes, int offset, int length, byte key) {
        for (int i = offset; i < length; ++i) {
            bytes[i] = (byte) (((int) bytes[i] ^ (int) (abyKey[i % abyKey.length] + key)) & 0xFF);
        }
    }
}
