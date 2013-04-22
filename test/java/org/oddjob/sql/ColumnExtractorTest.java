package org.oddjob.sql;

import java.util.Map;

import junit.framework.TestCase;

public class ColumnExtractorTest extends TestCase {

	public void testTypePopulated() {
		
		Map<Integer, String> types = ColumnExtractor.SQL_TYPE_NAMES;
		
		assertEquals("VARCHAR", types.get(12));
	}
	
}
