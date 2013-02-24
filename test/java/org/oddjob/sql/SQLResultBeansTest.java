package org.oddjob.sql;

import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.beanbus.BasicBeanBus;
import org.oddjob.beanbus.BusCrashException;

import junit.framework.TestCase;

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
		
		test.setArooaSession(new StandardArooaSession());
		test.setBusConductor(beanBus);
		
		beanBus.startBus();
		
		beanBus.accept("ignored");
		
		test.accept("Apple");
		
		beanBus.cleanBus();
		
		assertEquals("Apple", test.getRow());
		assertEquals(1, test.getRowCount());
		
		beanBus.accept("ignored");
		
		test.accept("Pear");
		test.accept("Banana");
		
		beanBus.stopBus();
		
		assertEquals(2, test.getRowSetCount());
		
		assertEquals("Pear", test.getRowSets()[1][0]);
		assertEquals("Banana", test.getRowSets()[1][1]);
		
		assertEquals(3, test.getRowCount());
	}
}
