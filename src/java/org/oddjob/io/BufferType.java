/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.deploy.annotations.ArooaText;
import org.oddjob.arooa.life.ArooaLifeAware;

/**
 * @oddjob.description A buffer can be used to accumulate output from 
 * one or more jobs which can then be used as input to another job.
 * <p>
 * A buffer can be used wherever input or output can be specified. A job
 * <p>
 * A buffer can be initialised with text, or lines of text and will can
 * also provide it's contents as text.
 * <p>
 * 
 * @oddjob.example
 * 
 * Capturing the contents of a file in a buffer.
 * 
 * {@oddjob.xml.resource org/oddjob/io/BufferFileCaptureExample.xml}
 * 
 * @oddjob.example
 * 
 * Accumulate output in a buffer.
 * 
 * {@oddjob.xml.resource org/oddjob/io/BufferAppendExample.xml}
 * 
 * @oddjob.example
 * 
 * Write the contents of a buffer to file. This example also shows 
 * initialising the buffer with a list.
 * 
 * {@oddjob.xml.resource org/oddjob/io/BufferToFileExample.xml}
 * 
 * @oddjob.example
 * 
 * Using the contents of a buffer as lines. This also shows how a buffer
 * can be initialised with text.
 * 
 * {@oddjob.xml.resource org/oddjob/io/BufferAsLinesExample.xml}
 * 
 */
public class BufferType implements ArooaValue, ArooaLifeAware {

	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {
			registry.register(BufferType.class, String.class, 
					new Convertlet<BufferType, String>() {
				public String convert(BufferType from) {
					return from.getText();				
				}
			});
			
			registry.register(BufferType.class, InputStream.class, 
					new Convertlet<BufferType, InputStream>() {
				public InputStream convert(BufferType from) {
					return from.toInputStream();				
				}
			});
			
			registry.register(BufferType.class, OutputStream.class, 
					new Convertlet<BufferType, OutputStream>() {
				public OutputStream convert(BufferType from) {
					return from.toOutputStream();
				}
			});
			registry.register(BufferType.class, String[].class, 
					new Convertlet<BufferType, String[]>() {
				public String[] convert(BufferType from) {
					return from.getLines();
				}
			});
		}
	}
	
	private volatile ByteArrayOutputStream buffer;
	
	private volatile String text;
	
	private volatile String[] lines;
	
	public InputStream toInputStream() {
		if (buffer == null) {
			return null;
		}
		else {
			return new ByteArrayInputStream(buffer.toByteArray());
		}
	}
	
	public OutputStream toOutputStream() {
		return buffer;
	}

	/**
	 * @oddjob.property text
	 * @oddjob.description The buffer as a text property. Either set the
	 * buffer contents from text or get the buffer contents as text.
	 * @oddjob.required No.
	 * 
	 * @param text
	 * 
	 * @throws IOException
	 */
	@ArooaText
	public void setText(String text) throws IOException {
		this.text = text;
	}
	
	public String getText() {
		if (buffer == null) {
			return null;
		}
		else {
			return new String(buffer.toByteArray());
		}
	}
	
	public String[] getLines() {
		try {
			InputStream inputStream = toInputStream();
			if (inputStream == null) {
				return null;
			}
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(inputStream));
			List<String> lines = new ArrayList<String>();
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				lines.add(line);
			}
			return lines.toArray(new String[lines.size()]);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}	
	
	public void setLines(String[] lines) {
		this.lines = lines;
	}
	
	@Override
	public void initialised() {
	}
	
	@Override
	public void configured() {
		buffer = new ByteArrayOutputStream();
		
		if (text != null) {
			try {
				buffer.write(text.getBytes());
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		if (lines != null) {
			PrintStream print = new PrintStream(buffer);
			for (String line: lines) {
				print.println(line);
			}
			print.flush();
		}
	}
	
	@Override
	public void destroy() {
		buffer = null;
	}
	
	public String toString() {
		return "Buffer (" + (buffer == null ? "unconfigured" :
			buffer.size() + " bytes") + ")";
	}
	
}
