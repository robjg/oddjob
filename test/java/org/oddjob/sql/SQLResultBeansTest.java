package org.oddjob.sql;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.oddjob.beanbus.BasicBeanBus;
import org.oddjob.beanbus.BusCrashException;

public class SQLResultBeansTest extends TestCase {

	public void testRows() {
		
		List<String> list = new ArrayList<String>();
		list.add("apple");
		list.add("orange");
		
		SQLResultsBean test = new SQLResultsBean();
		
		test.addBeans(list);
		
		Object[] row = test.getRows();
		
		assertEquals("apple", row[0]);
		assertEquals("orange", row[1]);
	}
	
	public void testEmptyRows() {
		
		List<String> list = new ArrayList<String>();
		
		SQLResultsBean test = new SQLResultsBean();
		
		test.addBeans(list);
		
		Object[] row = test.getRows();
		
		assertEquals(0, row.length);
	}
	
	public void testFullLifeCycle() throws BusCrashException {
		
		SQLResultsBean test = new SQLResultsBean();
		
		BasicBeanBus<String> beanBus = new BasicBeanBus<String>();
		
		test.setBusConductor(beanBus.getBusConductor());
		
		beanBus.startBus();
		
		// Start the trip.
		beanBus.add("ignored");
		
		test.filter("Apple");
		
		beanBus.getBusConductor().cleanBus();
		
		assertEquals("Apple", test.getRow());
		assertEquals(1, test.getRowCount());
		
		beanBus.add("ignored");
		
		test.filter("Pear");
		test.filter("Banana");
		
		beanBus.stopBus();
		
		assertEquals(2, test.getRowSetCount());
		
		assertEquals("Pear", test.getRowSets()[1][0]);
		assertEquals("Banana", test.getRowSets()[1][1]);
		
		assertEquals(3, test.getRowCount());
		
		// test restarts.
		
		beanBus.startBus();

		beanBus.add("ignored");
		
		test.filter("Apple");
		
		beanBus.stopBus();
		
		assertEquals("Apple", test.getRow());
		assertEquals(1, test.getRowCount());
		
	}
	
	public void testSetBeanBusTwice() throws BusCrashException {
		
		SQLResultsBean test = new SQLResultsBean();
		
		BasicBeanBus<String> beanBus = new BasicBeanBus<String>();
		
		test.setBusConductor(beanBus.getBusConductor());
		test.setBusConductor(beanBus.getBusConductor());
		
		beanBus.startBus();
		
		// Start the trip.
		beanBus.add("ignored");
		
		test.filter("Apple");
		
		beanBus.stopBus();
		
		assertEquals("Apple", test.getRow());
		assertEquals(1, test.getRowCount());
	}		
}
