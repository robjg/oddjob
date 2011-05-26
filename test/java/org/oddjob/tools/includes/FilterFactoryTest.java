package org.oddjob.tools.includes;

import junit.framework.TestCase;

public class FilterFactoryTest extends TestCase {

	public void testForSnippet() {
		
		FilterFactory test = new FilterFactory("x/y/z/abc.txt#snippet");
		
		assertEquals("x/y/z/abc.txt", test.getResourcePath());
		assertEquals(SnippetFilter.class, test.getTextLoader().getClass());
		
	}
	
	public void testForNoneSnippet() {
		
		FilterFactory test = new FilterFactory("x/y/z/abc.txt");
		
		assertEquals("x/y/z/abc.txt", test.getResourcePath());
		assertEquals(PlainStreamToText.class, test.getTextLoader().getClass());
		
	}
}
