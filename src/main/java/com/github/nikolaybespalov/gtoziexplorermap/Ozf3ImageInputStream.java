package com.github.nikolaybespalov.gtoziexplorermap;

import com.google.common.primitives.Ints;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;
import java.io.IOException;
import java.nio.ByteOrder;

public class Ozf3ImageInputStream extends ImageInputStreamImpl {
    protected static final byte abyKey[] = {
            (byte) 0x2D, (byte) 0x4A, (byte) 0x43, (byte) 0xF1, (byte) 0x27, (byte) 0x9B, (byte) 0x69, (byte) 0x4F,
            (byte) 0x36, (byte) 0x52, (byte) 0x87, (byte) 0xEC, (byte) 0x5F, (byte) 0x42, (byte) 0x53, (byte) 0x22,
            (byte) 0x9E, (byte) 0x8B, (byte) 0x2D, (byte) 0x83, (byte) 0x3D, (byte) 0xD2, (byte) 0x84, (byte) 0xBA,
            (byte) 0xD8, (byte) 0x5B
    };
    protected ImageInputStream stream;
    protected byte key;

    public Ozf3ImageInputStream(ImageInputStream stream, byte key) {
        this.stream = stream;
        this.key = key;

        stream.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public ByteOrder getByteOrder() {
        return stream.getByteOrder();
    }

    @Override
    public int read() throws IOException {
        byte[] bytes = new byte[1];

        if (read(bytes) != 1) {
            return -1;
        }

        return bytes[0];
    }

    @Override
    public int read(byte[] bytes, int offset, int length) throws IOException {
        int n = stream.read(bytes, offset, length);

        for (int i = offset; i < length; ++i) {
            bytes[i] = (byte) (((int) bytes[i] ^ (int) (abyKey[i % abyKey.length] + key)) & 0xFF);
        }

        return n;
    }

    @Override
    public int readInt() throws IOException {
        byte[] bytes = new byte[4];
        read(bytes, 0, 4);

        return Ints.fromBytes(bytes[3], bytes[2], bytes[1], bytes[0]);
    }
}
