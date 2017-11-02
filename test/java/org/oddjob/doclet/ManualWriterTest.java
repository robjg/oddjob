package org.oddjob.doclet;

import org.junit.Test;

import org.oddjob.OjTestCase;

public class ManualWriterTest extends OjTestCase {

   @Test
	public void testIndexFileWithPackage() {
		
		String result = ManualWriter.getIndexFile("com.foo.ba.HelloWorld");
		
		assertEquals("../../../index.html", result);
	}

   @Test
	public void testIndexFileWithSmallNames() {
		
		String result = ManualWriter.getIndexFile("a.b.c.X");
		
		assertEquals("../../../index.html", result);
	}
	
   @Test
	public void testIndexFileNoPackage() {
		
		String result = ManualWriter.getIndexFile("HelloWorld");
		
		assertEquals("index.html", result);
	}
	
   @Test
	public void testIndexFileNoClass() {
		
		String result = ManualWriter.getIndexFile("");
		
		assertEquals("index.html", result);
	}
}
