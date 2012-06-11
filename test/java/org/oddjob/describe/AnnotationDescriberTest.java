package org.oddjob.describe;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;

public class AnnotationDescriberTest extends TestCase {

	@DescribeWith
	public Map<String, String> myDescription() {
		Map<String, String> description = new HashMap<String, String>();
		description.put("fruit", "apple");
		return description;
	}
	

	public void testAnnotedDescriberMethod() {
		
		ArooaSession session = new StandardArooaSession();
		
		Describer test = new AnnotationDescriber(session);
		
		Map<String, String> result = test.describe(this);
		
		assertEquals(1, result.size());
		assertEquals("apple", result.get("fruit"));
	}
}
