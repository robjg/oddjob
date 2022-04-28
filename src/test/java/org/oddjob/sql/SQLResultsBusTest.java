package org.oddjob.sql;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Flushable;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class SQLResultsBusTest extends OjTestCase {

    private static final Logger logger = LoggerFactory.getLogger(SQLResultsBusTest.class);

    @Before
    public void setUp() throws Exception {


        logger.info("-----------------------------  " + getName() + "  ----------------");
    }

    @Test
    public void testLifecycle() throws Exception {

		ArooaSession session = new StandardArooaSession();

		Consumer<? super Object> to = mock(Consumer.class,
				Mockito.withSettings().extraInterfaces(Runnable.class, Flushable.class, Closeable.class));

    	SQLResultsBus test = new SQLResultsBus(to, session);

    	test.run();

    	verify((Runnable) to, times(1)).run();

    	test.handleUpdate(2, null);

		verify(to, times(1)).accept(new UpdateCount(2));

		test.flush();

		verify((Flushable) to, times(1)).flush();

		test.close();

		verify((Flushable) to, times(1)).flush();
		verify((Closeable) to, times(1)).close();
	}

    @Test
    public void testExample() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/sql/SQLResultsBusExample.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> results = lookup.lookup("select.results", List.class);

        assertEquals(2, results.size());

        oddjob.destroy();

    }

    @Test
    public void testExample2() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/sql/SQLResultsBusExample2.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> results = lookup.lookup("bean-capture.beans", List.class);

        assertEquals(3, results.size());

        oddjob.destroy();

    }
}
