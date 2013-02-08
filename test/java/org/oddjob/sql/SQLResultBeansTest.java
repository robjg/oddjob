package org.oddjob.sql;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class SQLResultBeansTest extends TestCase {

	public void testRows() {
		
		List<String> list = new ArrayList<String>();
		list.add("apple");
		list.add("orange");
		
		SQLResultsBean test = new SQLResultsBean();
		
		test.add(list);
		
		Object[] row = test.getRows();
		
		assertEquals("apple", row[0]);
		assertEquals("orange", row[1]);
	}
	
	public void testEmptyRows() {
		
		List<String> list = new ArrayList<String>();
		
		SQLResultsBean test = new SQLResultsBean();
		
		test.add(list);
		
		Object[] row = test.getRows();
		
		assertEquals(0, row.length);
	}
}
