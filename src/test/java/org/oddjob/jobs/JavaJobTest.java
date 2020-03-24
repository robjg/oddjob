package org.oddjob.jobs;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.io.BufferType;
import org.oddjob.tools.ConsoleCapture;

import java.io.File;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JavaJobTest {

    @Test
    public void testSimpleHello() {

        String mainClassFile = HelloMain.class.getResource(
                HelloMain.class.getSimpleName() + ".class").getFile();

        String classPath = mainClassFile.substring(0,
                mainClassFile.indexOf(HelloMain.class.getPackage()
                        .getName().replace(".", "/")));

        BufferType bufferType = new BufferType();
        bufferType.configured();

        JavaJob test = new JavaJob();
        test.setClassName(HelloMain.class.getName());
        test.setClassPath(new File[]{new File(classPath)});
        test.setStdout(bufferType.toOutputStream());
        test.setRedirectStderr(true);
        test.setProgramArgs("Alice Bob");
        test.setVmArgs("-Dour.greeting=Hello");
        test.run();

        assertThat(bufferType.getText().trim(), is("Hello Alice and Bob"));

    }

    @Test
    public void testOddjobExample() throws ArooaConversionException {

        String mainClassFile = HelloMain.class.getResource(
                HelloMain.class.getSimpleName() + ".class").getFile();

        String classPath = mainClassFile.substring(0,
                mainClassFile.indexOf(HelloMain.class.getPackage()
                        .getName().replace(".", "/")));

        Properties props = new Properties();
        props.setProperty("classPath", classPath);

        File config = new File(getClass().getResource("JavaExample.xml").getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(config);
        oddjob.setProperties(props);

        oddjob.load();

        JavaJob javaJob = new OddjobLookup(oddjob)
                .lookup("javaExample", JavaJob.class);

        ConsoleCapture consoleCapture = new ConsoleCapture();

        try (ConsoleCapture.Close ignored = consoleCapture.capture(javaJob.consoleLog())) {
            oddjob.run();

            assertThat(oddjob.lastStateEvent().getState().isComplete(), is(true));
        }

        consoleCapture.dump();

        assertThat(consoleCapture.getLines()[0].trim(), is("Hello Alice and Bob"));
    }

}