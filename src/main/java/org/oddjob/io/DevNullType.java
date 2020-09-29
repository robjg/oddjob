package org.oddjob.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementations of /dev/null Input and Output Stream.
 */
public class DevNullType {

    public static InputStream IN = new InputStream() {

        private volatile boolean closed;

        @Override
        public int read() throws IOException {
            if (closed) {
                throw new EOFException("Stream closed");
            }
            return -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            if (closed) {
                throw new EOFException("Stream closed");
            }
            return -1;
        }

        @Override
        public long skip(long n) throws IOException {
            if (closed) {
                throw new EOFException("Stream closed");
            }
            return 0L;
        }

        @Override
        public int available() throws IOException {
            if (closed) {
                throw new EOFException("Stream closed");
            }
            return 0;
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public String toString() {
            return "/dev/null" + (closed ? " (closed)" : "");
        }
    };

    public static OutputStream OUT = new OutputStream() {
        private volatile boolean closed;

        @Override
        public void write(int b) throws IOException {
            if (closed) {
                throw new EOFException("Stream closed");
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            if (closed) {
                throw new EOFException("Stream closed");
            }
        }

        @Override
        public void flush() throws IOException {
            if (closed) {
                throw new EOFException("Stream closed");
            }
        }

        @Override
        public void close() {
            this.closed = true;
        }

        @Override
        public String toString() {
            return "/dev/null" + (closed ? " (closed)" : "");
        }
    };
}
