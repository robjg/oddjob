package org.oddjob.doclet;

import junit.framework.TestCase;

public class ManualWriterTest extends TestCase {

	public void testIndexFileWithPackage() {
		
		String result = ManualWriter.getIndexFile("com.foo.ba.HelloWorld");
		
		assertEquals("../../../index.html", result);
	}

	public void testIndexFileWithSmallNames() {
		
		String result = ManualWriter.getIndexFile("a.b.c.X");
		
		assertEquals("../../../index.html", result);
	}
	
	public void testIndexFileNoPackage() {
		
		String result = ManualWriter.getIndexFile("HelloWorld");
		
		assertEquals("index.html", result);
	}
	
	public void testIndexFileNoClass() {
		
		String result = ManualWriter.getIndexFile("");
		
		assertEquals("index.html", result);
	}
}
