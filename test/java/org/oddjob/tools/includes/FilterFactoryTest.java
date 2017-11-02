package org.oddjob.tools.includes;

import org.junit.Test;

import org.oddjob.OjTestCase;

public class FilterFactoryTest extends OjTestCase {

   @Test
	public void testForSnippet() {
		
		FilterFactory test = new FilterFactory("x/y/z/abc.txt#snippet");
		
		assertEquals("x/y/z/abc.txt", test.getResourcePath());
		assertEquals(SnippetFilter.class, test.getTextLoader().getClass());
		
	}
	
   @Test
	public void testForNoneSnippet() {
		
		FilterFactory test = new FilterFactory("x/y/z/abc.txt");
		
		assertEquals("x/y/z/abc.txt", test.getResourcePath());
		assertEquals(PlainStreamToText.class, test.getTextLoader().getClass());
		
	}
}
