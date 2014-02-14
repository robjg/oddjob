package org.oddjob.input;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class StdInInputHandlerTest extends TestCase {

	public void testLineReader() throws IOException {
		
		String s = "hello\r\n" +
				"\n" +
				"world\n" +
				"\r\n";
				
		StdInInputHandler.LineReader test = new StdInInputHandler.LineReader(
				new ByteArrayInputStream(s.getBytes()));
		
		assertEquals("hello", test.readLine());
		assertEquals("", test.readLine());
		assertEquals("world", test.readLine());
		assertEquals("", test.readLine());
		assertEquals(null, test.readLine());
		
		test.close();
	}
}
