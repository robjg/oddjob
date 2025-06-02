/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.framework.adapt;

import org.apache.commons.beanutils.DynaBean;
import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.framework.adapt.beanutil.WrapDynaBean;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;
import org.oddjob.state.JobStateHandler;
import org.oddjob.state.StateHandler;

import java.util.Map;

/**
 * Tests on BaseWrapper.
 */
public class BaseWrapperTest extends OjTestCase {

    /**
     * Bean fixture
     */
    public static class Result {
        public int getResult() {
            return 42;
        }
    }

    /**
     * Test base wrapper by extending it.
     */
    private class MockWrapper extends BaseWrapper {

        final JobStateHandler stateHandler = new JobStateHandler(this);

        final IconHelper iconHelper = new IconHelper(this,
                StateIcons.iconFor(stateHandler.getState()));

        final Object wrapped;

        final DynaBean dynaBean;

        MockWrapper(Object wrapped, ArooaSession session) {
            this.wrapped = wrapped;
            this.dynaBean = new WrapDynaBean(wrapped, session.getTools().getPropertyAccessor());
        }

        @Override
        protected StateHandler<?> stateHandler() {
            return stateHandler;
        }

        @Override
        protected IconHelper iconHelper() {
            return iconHelper;
        }

        public Object getWrapped() {
            return wrapped;
        }

        protected Object getProxy() {
            return null;
        }

        protected DynaBean getDynaBean() {
            return dynaBean;
        }

        public void run() {
        }

        @Override
        public boolean softReset() {
            throw new RuntimeException("Unexpected.");
        }

        @Override
        public boolean hardReset() {
            throw new RuntimeException("Unexpected.");
        }

        @Override
        protected void fireDestroyedState() {
            throw new RuntimeException("Unexpected");
        }
    }

    /**
     * Test getting a result.
     *
     */
    @Test
    public void testWithResult() throws ArooaPropertyException, ArooaConversionException {
        ArooaSession session = new StandardArooaSession();
        MockWrapper test = new MockWrapper(new Result(), session);
        test.setArooaSession(session);

        assertEquals(42, test.getResult(null));
    }

    /**
     * Test getting a result with no result.
     *
     */
    @Test
    public void testNoResult() throws ArooaPropertyException, ArooaConversionException {
        ArooaSession session = new StandardArooaSession();
        MockWrapper test = new MockWrapper(new Object(), session);

        assertEquals(0, test.getResult(null));
    }

    public static class MockBean {
        public String getReadable() {
            return "a";
        }

        public void setWritable(String writable) {

        }

        public String getBoth() {
            return "b";
        }

        public void setBoth(String both) {

        }
    }

    @Test
    public void testDescribe() {
        ArooaSession session = new StandardArooaSession();

        MockWrapper test = new MockWrapper(new MockBean(), session);
        test.setArooaSession(session);

        Map<String, String> properties = new UniversalDescriber(
                session).describe(test);

        assertEquals("readable", "a", properties.get("readable"));
        assertEquals("both", "b", properties.get("both"));
        assertEquals("writable", null, properties.get("writable"));
    }
}
