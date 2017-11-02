package org.oddjob.describe;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import org.oddjob.OjTestCase;

import org.oddjob.Describeable;

public class DescribeableDescriberTest extends OjTestCase
implements Describeable {

	public Map<String, String> describe() {
		Map<String, String> description = new HashMap<String, String>();
		description.put("fruit", "apple");
		return description;
	}

   @Test
	public void testDescribeableDescriberMethod() {
				
		Describer test = new DescribeableDescriber();
		
		Map<String, String> result = test.describe(this);
		
		assertEquals(1, result.size());
		assertEquals("apple", result.get("fruit"));
	}
	
}
