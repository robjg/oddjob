package org.oddjob.sql;

import org.junit.Test;

import java.util.Map;

import org.oddjob.OjTestCase;

public class ColumnExtractorTest extends OjTestCase {

   @Test
	public void testTypePopulated() {
		
		Map<Integer, String> types = ColumnExtractor.SQL_TYPE_NAMES;
		
		assertEquals("VARCHAR", types.get(12));
	}
	
}
