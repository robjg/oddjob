package org.oddjob.util;

import java.io.IOException;
import java.io.OutputStream;

public class StreamPrinter {

	private static final byte[] EOL = System.getProperty("line.separator").getBytes();
	
	private final OutputStream out;
	
	public StreamPrinter(OutputStream out) {
		if (out == null) {
			throw new NullPointerException("No OutputStream.");
		}
		this.out = out;
	}
	
	public void println() {
		
		try {
			out.write(EOL);
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void println(String s) {
		
		try {
			out.write(s.getBytes());
			out.write(EOL);
			out.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
