package org.oddjob.sql;

import java.util.ArrayList;
import java.util.List;

import org.oddjob.beanbus.BadBeanException;
import org.oddjob.beanbus.CrashBusException;

import junit.framework.TestCase;

public class SQLResultBeansTest extends TestCase {

	public void testRows() throws BadBeanException, CrashBusException {
		
		List<String> list = new ArrayList<String>();
		list.add("apple");
		list.add("orange");
		
		SQLResultsBean test = new SQLResultsBean();
		
		test.accept(list);
		
		Object[] row = test.getRows();
		
		assertEquals("apple", row[0]);
		assertEquals("orange", row[1]);
	}
	
	public void testEmptyRows() throws BadBeanException, CrashBusException {
		
		List<String> list = new ArrayList<String>();
		
		SQLResultsBean test = new SQLResultsBean();
		
		test.accept(list);
		
		Object[] row = test.getRows();
		
		assertEquals(0, row.length);
	}
}
