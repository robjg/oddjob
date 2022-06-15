package org.oddjob.framework.adapt.job;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.OurDirs;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.util.URLClassLoaderType;
import org.oddjob.util.URLClassLoaderTypeTest;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

public class CallableWrapperTest extends OjTestCase {

    public static class OurCallable implements Callable<Integer> {

        boolean ran;

        int status;

        public Integer call() {
            ran = true;
            return status;
        }

        public boolean isRan() {
            return ran;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String toString() {
            return "OurCallable";
        }
    }

    @Test
    public void testInOddjob() throws Exception {
        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bean class='" + OurCallable.class.getName() + "' id='r' />" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Object runnable = lookup.lookup("r");
        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(runnable));

        Boolean ran = lookup.lookup("r.ran", Boolean.class);

        assertEquals(Boolean.TRUE, ran);

        oddjob.destroy();
    }

    @Test
    public void testIncompleteInOddjob() throws Exception {
        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bean class='" + OurCallable.class.getName() +
                        "' id='r' status='10'/>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Object runnable = lookup.lookup("r");
        assertEquals(JobState.INCOMPLETE, OddjobTestHelper.getJobState(runnable));

        Boolean ran = lookup.lookup("r.ran", Boolean.class);

        assertEquals(Boolean.TRUE, ran);

        oddjob.destroy();
    }

    public static class OurCallable2 implements Callable<Void> {

        boolean ran;

        public Void call() {
            ran = true;
            return null;
        }

        public boolean isRan() {
            return ran;
        }

        public String toString() {
            return "OurCallable";
        }
    }

    @Test
    public void testVoidCallableInOddjob() throws Exception {
        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bean class='" + OurCallable2.class.getName() +
                        "' id='r' />" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Object runnable = lookup.lookup("r");
        assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(runnable));

        Boolean ran = lookup.lookup("r.ran", Boolean.class);

        assertEquals(Boolean.TRUE, ran);

        oddjob.destroy();
    }

    public static class BadCallable implements Callable<Void> {

        public Void call() throws Exception {
            throw new Exception("Deliberate fail!");
        }

        public String toString() {
            return "OurCallable";
        }
    }

    @Test
    public void testExceptionInOddjob() {
        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bean class='" + BadCallable.class.getName() +
                        "' id='r'/>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Object runnable = lookup.lookup("r");
        assertEquals(JobState.EXCEPTION, OddjobTestHelper.getJobState(runnable));

        oddjob.destroy();
    }

    /**
     * Not sure why this test is here as it doesn't appear to have anything
     * to do with Callables.
     *
     * @throws IOException
     */
    @Test
    public void testContextClassLoaderWhenRunningFromOddjob() throws ArooaPropertyException, ArooaConversionException, IOException {

        OurDirs dirs = new OurDirs();

        ClassLoader existing = Thread.currentThread().getContextClassLoader();

        File check = dirs.relative("test/classloader/AJob.class");
        if (!check.exists()) {
            URLClassLoaderTypeTest.compileSample(dirs.base());
        }

        URLClassLoaderType classLoaderType = new URLClassLoaderType();
        classLoaderType.setFiles(new File[]{
                dirs.relative("test/classloader")});
        classLoaderType.setParent(getClass().getClassLoader());
        classLoaderType.configured();

        ClassLoader classLoader = classLoaderType.toValue();

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <bean class='AJob' id='c'/>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));
        oddjob.setClassLoader(classLoader);

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        ClassLoader threadClassLoader = lookup.lookup("c.classLoader",
                ClassLoader.class);

        assertEquals(classLoader, threadClassLoader);

        oddjob.destroy();

        assertEquals(existing, Thread.currentThread().getContextClassLoader());
    }
}
