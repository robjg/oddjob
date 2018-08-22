package org.oddjob.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provide a wire tap on an input stream, copying all data read to an
 * output stream.
 * 
 * @see TeeType.
 * 
 * @author rob
 *
 */
public class WireTapInputStream extends FilterInputStream {

	private final OutputStream outputStream;
	
	public WireTapInputStream(InputStream inputStream,
			OutputStream outputStream) {
		super(inputStream);
		this.outputStream = outputStream;
	}
	
    public int read() throws IOException {
    	int b = super.read();
    	if (b > -1) {
    		outputStream.write(b);
    	}
    	return b;
    }
    
    public int read(byte b[], int off, int len) throws IOException {
    	int n = super.read(b, off, len);
    	if (n > 0) {
    		outputStream.write(b, off, n);
    	}
    	return n;
    }
    
    @Override
    public void close() throws IOException {
    	try {
    		super.close();
    	}
    	finally {
        	outputStream.close();
    	}
    }
}
