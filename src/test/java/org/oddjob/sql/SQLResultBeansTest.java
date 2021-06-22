package org.oddjob.sql;

import org.junit.Test;
import org.oddjob.OjTestCase;

import java.io.IOException;
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
    public void testFullLifeCycle() throws IOException {

        SQLResultsBean test = new SQLResultsBean();

        test.run();

        test.filter("Apple");

        test.flush();

        assertEquals("Apple", test.getRow());
        assertEquals(1, test.getRowCount());

        test.filter("Pear");
        test.filter("Banana");

        test.flush();

        assertEquals(2, test.getRowSetCount());

        assertEquals("Pear", test.getRowSets()[1][0]);
        assertEquals("Banana", test.getRowSets()[1][1]);

        assertEquals(3, test.getRowCount());

        // test restarts.

        test.run();

        test.filter("Apple");

        test.flush();

        assertEquals("Apple", test.getRow());
        assertEquals(1, test.getRowCount());
    }

}
