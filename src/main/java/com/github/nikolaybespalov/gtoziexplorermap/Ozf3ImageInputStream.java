package com.github.nikolaybespalov.gtoziexplorermap;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageInputStreamImpl;
import java.io.IOException;

public class Ozf3ImageInputStream extends ImageInputStreamImpl {
    ImageInputStream stream;

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
        return 0;
    }
}
