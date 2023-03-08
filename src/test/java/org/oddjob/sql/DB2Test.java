package org.oddjob.sql;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class DB2Test extends OjTestCase {

    @Test
    public void testCallable() throws ArooaPropertyException, ArooaConversionException {

        if (System.getProperty("db2.home") == null) {
            return;
        }

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/sql/DB2Callable.xml",
                getClass().getClassLoader()));
        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        assertEquals(Integer.valueOf(0), lookup.lookup(
                "sp-result1", Integer.class));

        assertEquals(Integer.valueOf(0), lookup.lookup(
                "sp-result2", Integer.class));

        oddjob.destroy();

    }
}
