package org.oddjob.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * An output stream that splits output into an existing
 * output stream if supplied, and a console archive. 
 * <p>
 * The internal buffer is synchronised so this class is
 * thread safe, however no synchronisation occurs between writing
 * to the underlying stream and the log so output could be in
 * a different order.
 *
 */
abstract public class AbstractLoggingOutput extends OutputStream {
	
	private final ByteArrayOutputStream buffer;
	
	/** The existing OutputStream to also write to. */
	private final OutputStream existing;
	
	/**
	 * Constructor.
	 * 
	 * @param existing The output stream to also write to. May be null.
	 */
	public AbstractLoggingOutput(OutputStream existing) {
		this.buffer = new ByteArrayOutputStream();
		this.existing = existing;
	}
	
	public void write(int c) throws IOException {
		add(new byte[] { (byte) c }, 0, 1);
		if (existing != null) existing.write(c);
	}
	
	public void write(byte[] b) throws IOException {
		add(b, 0, b.length);
		if (existing != null) existing.write(b);
	}
	
	public void write(byte[] buf, int off, int len) throws IOException {
		add(buf, off, len);
		if (existing != null) existing.write(buf, off, len);
	}
	
	@Override
	public void flush() throws IOException {
		if (existing != null) {
			existing.flush();
		}
	}
	
	public void close() throws IOException {
		next();
		if (existing != null) existing.close();
	}
	
	/**
	 * Add bytes to the internal buffer.
	 * 
	 * @param buf
	 * @param off
	 * @param length
	 */
	void add(byte[] buf, int off , int length) {
		synchronized (buffer) {
			for (int i = off; i < off + length; ++i) {
				if (buf[i] == '\n') {
					buffer.write(buf, off, i - off + 1);							
					next();
					add(buf, i+1, length - (i - off +1));
					return;
				}
			}
			buffer.write(buf, off, length);
		}
	}

	/**
	 * Called when a line is ready to be written to the {@link LogArchive}.
	 *
	 */
	void next() {
		String message;
		synchronized (buffer) {
			if (buffer.size() == 0) {
				return;
			}
			message = buffer.toString();
			buffer.reset();
		}
		dispatch(message);
	}
	
	abstract protected void dispatch(String message);
}