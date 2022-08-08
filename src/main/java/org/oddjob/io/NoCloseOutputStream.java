package org.oddjob.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Wrap an Output Stream to create an Output Stream that doesn't close.
 *
 * @see StdoutType
 * @see StderrType
 *
 */
public class NoCloseOutputStream extends FilterOutputStream {

    private final String name;

    public NoCloseOutputStream(OutputStream out, String name) {
        super(out);
        this.name = name;
    }

    @Override
    public void close() throws IOException {
        super.flush();
    }

    /*
    FilterOutputStream writes per byte, with Stdout/err this causes interleaving, so we pass this through to
    the original.
     */
    @Override
    public void write(byte[] b, int off, int len) {
        System.out.write(b, off, len);
    }

    @Override
    public String toString() {
        return name;
    }
}
