package org.oddjob.sql;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.beanbus.BasicBeanBus;
import org.oddjob.beanbus.BusCrashException;

import java.util.ArrayList;
import java.util.List;

public class SQLResultBeansTest extends OjTestCase {

    @Test
    public void testRows() {

        List<String> list = new ArrayList<>();
        list.add("apple");
        list.add("orange");

        SQLResultsBean test = new SQLResultsBean();

        test.addBeans(list);

        Object[] row = test.getRows();

        assertEquals("apple", row[0]);
        assertEquals("orange", row[1]);
    }

    @Test
    public void testEmptyRows() {

        List<String> list = new ArrayList<>();

        SQLResultsBean test = new SQLResultsBean();

        test.addBeans(list);

        Object[] row = test.getRows();

        assertEquals(0, row.length);
    }

    @Test
    public void testFullLifeCycle() throws BusCrashException {

        SQLResultsBean test = new SQLResultsBean();

        BasicBeanBus<String> beanBus = new BasicBeanBus<>();

        test.setBusConductor(beanBus.getBusConductor());

        beanBus.startBus();

        // Start the trip.
        beanBus.accept("ignored");

        test.filter("Apple");

        beanBus.getBusConductor().flush();

        assertEquals("Apple", test.getRow());
        assertEquals(1, test.getRowCount());

        beanBus.accept("ignored");

        test.filter("Pear");
        test.filter("Banana");

        beanBus.stopBus();

        assertEquals(2, test.getRowSetCount());

        assertEquals("Pear", test.getRowSets()[1][0]);
        assertEquals("Banana", test.getRowSets()[1][1]);

        assertEquals(3, test.getRowCount());

        // test restarts.

        beanBus.startBus();

        beanBus.accept("ignored");

        test.filter("Apple");

        beanBus.stopBus();

        assertEquals("Apple", test.getRow());
        assertEquals(1, test.getRowCount());

    }

    @Test
    public void testSetBeanBusTwice() throws BusCrashException {

        SQLResultsBean test = new SQLResultsBean();

        BasicBeanBus<String> beanBus = new BasicBeanBus<>();

        test.setBusConductor(beanBus.getBusConductor());
        test.setBusConductor(beanBus.getBusConductor());

        beanBus.startBus();

        // Start the trip.
        beanBus.accept("ignored");

        test.filter("Apple");

        beanBus.stopBus();

        assertEquals("Apple", test.getRow());
        assertEquals(1, test.getRowCount());
    }
}
