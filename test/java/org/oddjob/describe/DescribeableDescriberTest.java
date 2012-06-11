package org.oddjob.describe;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.oddjob.Describeable;

public class DescribeableDescriberTest extends TestCase
implements Describeable {

	public Map<String, String> describe() {
		Map<String, String> description = new HashMap<String, String>();
		description.put("fruit", "apple");
		return description;
	}

	public void testDescribeableDescriberMethod() {
				
		Describer test = new DescribeableDescriber();
		
		Map<String, String> result = test.describe(this);
		
		assertEquals(1, result.size());
		assertEquals("apple", result.get("fruit"));
	}
	
}
