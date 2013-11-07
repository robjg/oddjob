package org.oddjob.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.output.ByteArrayOutputStream;

import junit.framework.TestCase;

public class WireTapInputStreamTest extends TestCase {

	public void testReadingArray() throws IOException {
		
		ByteArrayInputStream input = new ByteArrayInputStream(
				"My favourite fruit is apples.".getBytes());
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		WireTapInputStream test = new WireTapInputStream(input, output);
		
		byte[] buffer = new byte[64];
		
		assertEquals(29, test.read(buffer));
		assertEquals(-1, test.read(buffer));
		
		test.close();
		
		assertEquals("My favourite fruit is apples.", 
				new String(output.toByteArray()));
		
		assertEquals("My favourite fruit is apples.", 
				new String(buffer, 0, 29));
	}
	
	public void testReadingCharByChar() throws IOException {
		
		ByteArrayInputStream input = new ByteArrayInputStream(
				"My favourite fruit is apples.".getBytes());
		
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		
		WireTapInputStream test = new WireTapInputStream(input, output);
		
		while (test.read() != -1);
		
		test.close();
		
		assertEquals("My favourite fruit is apples.", 
				new String(output.toByteArray()));
	}
}
