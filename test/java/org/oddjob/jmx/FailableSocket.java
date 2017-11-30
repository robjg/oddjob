package org.oddjob.jmx;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FailableSocket extends Socket {
	
	private static final Logger logger = LoggerFactory.getLogger(FailableSocket.class);
	
	volatile boolean fail;

	public FailableSocket(String host, int port) throws UnknownHostException, IOException {
		super(host, port);
	}

	public FailableSocket(SocketImpl impl) throws SocketException {
		super(impl);
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return new FailableInputStream(super.getInputStream());
	}
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		return new FailableOutputStream(super.getOutputStream());
	}
	
	
	class FailableInputStream extends FilterInputStream {
	
		public FailableInputStream(InputStream in) {
			super(in);
		}
		
		@Override
		public int read() throws IOException {
			assertOK();
			return super.read();
		}
		
		@Override
		public int read(byte[] b) throws IOException {
			assertOK();
			return super.read(b);
		}
		
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			assertOK();
			return super.read(b, off, len);
		}
		
	}

	class FailableOutputStream extends FilterOutputStream {
		
		public FailableOutputStream(OutputStream out) {
			super(out);
		}
		
		@Override
		public void write(int b) throws IOException {
			assertOK();
			super.write(b);
		}
		
		@Override
		public void write(byte[] b) throws IOException {
			assertOK();
			super.write(b);
		}
		
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			assertOK();
			super.write(b, off, len);
		}
	}
	
	public void setFail(boolean fail) {
		this.fail = fail;
	}
	
	void assertOK() throws IOException {
		if (fail) {
			IOException e = 
				new IOException("Emergancy, Emergancy! Network failure! Can you recover?");
			logger.error("Failed!", e);
			throw e;
		}
	}
}
