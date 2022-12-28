/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import org.apache.commons.io.FileUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.OurDirs;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.Matchers.contains;

public class CopyJobTest extends OjTestCase {

    private File baseDir;
    private File workDir;

    @Before
    public void setUp() throws Exception {

        baseDir = OurDirs.basePath()
                .toFile();
        workDir = OurDirs.workPathDir(getClass().getSimpleName(), false)
                .toFile();

        if (workDir.exists()) {
            FileUtils.forceDelete(workDir);
        }
    }

    @Test
    public void testCopyFile() throws Exception {
        FileUtils.forceMkdir(workDir);

        Properties props = new Properties();
        props.setProperty("base.dir", baseDir.toString());
        props.setProperty("work.dir", workDir.toString());

        Oddjob oj = new Oddjob();
        oj.setProperties(props);
        oj.setConfiguration(new XMLConfiguration(
                "org/oddjob/io/CopyFileExample.xml",
                getClass().getClassLoader()));
        oj.run();

        assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

        assertTrue(new File(workDir, "test1.txt").exists());
    }

    @Test
    public void testCopyFiles() throws Exception {
        FileUtils.forceMkdir(workDir);

        String xml =
                "<oddjob id='this'>" +
                        " <job>" +
                        "  <copy to='${work.dir}'>" +
                        "   <from>" +
                        "    <files files='${base.dir}/test/io/reference/*.txt'/>" +
                        "   </from>" +
                        "  </copy>" +
                        " </job>" +
                        "</oddjob>";

        Properties props = new Properties();
        props.setProperty("base.dir", baseDir.toString());
        props.setProperty("work.dir", workDir.toString());

        Oddjob oj = new Oddjob();
        oj.setProperties(props);
        oj.setConfiguration(new XMLConfiguration("TEST", xml));
        oj.run();

        assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

        assertEquals(3, new WildcardSpec(
                new File(workDir, "*.txt")).findFiles().length);
    }

    @Test
    public void testCopyDirectory() throws Exception {
        FileUtils.forceMkdir(workDir);

        Properties props = new Properties();
        props.setProperty("base.dir", baseDir.toString());
        props.setProperty("work.dir", workDir.toString());

        Oddjob oj = new Oddjob();
        oj.setProperties(props);
        oj.setConfiguration(new XMLConfiguration(
                "org/oddjob/io/CopyDirectory.xml",
                getClass().getClassLoader()));

        oj.run();

        assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

        assertTrue(new File(workDir, "a/x/test3.txt").exists());
    }

    @Test
    public void testCopyDirectory2() {
        // directory doesn't exist this time.
        // dir.mkdir();

        Properties props = new Properties();
        props.setProperty("base.dir", baseDir.toString());
        props.setProperty("work.dir", workDir.toString());

        Oddjob oj = new Oddjob();
        oj.setProperties(props);
        oj.setConfiguration(new XMLConfiguration(
                "org/oddjob/io/CopyDirectory.xml",
                getClass().getClassLoader()));
        oj.run();

        assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

        assertTrue(new File(workDir, "x/test3.txt").exists());
    }

    @Test
    public void testSerialize() throws Exception {
        workDir.mkdir();

        CopyJob test = new CopyJob();
        test.setFrom(new File[]{
                new File(baseDir, "test/io/reference/test1.txt")});
        test.setTo(workDir);

        Runnable copy = OddjobTestHelper.copy(test);
        copy.run();

        assertTrue(new File(workDir, "test1.txt").exists());
    }

    @Test
    public void testCopyBuffer() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/io/CopyFileToBuffer.xml",
                getClass().getClassLoader()));

        oddjob.setArgs(new String[]{baseDir.toString()});

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        String result = new OddjobLookup(oddjob).lookup("e.text", String.class);

        assertEquals("Test 1", result.trim());

        oddjob.destroy();
    }

    @Test
    public void testCopyToConsumer() {

        AtomicBoolean closed = new AtomicBoolean();

        InputStream in = new ByteArrayInputStream("A\nB\nC\n".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public void close() throws IOException {
                super.close();
                closed.set(true);
            }

            @Override
            public String toString() {
                return "Our-Input";
            }
        };

        CopyJob copyJob = new CopyJob();
        copyJob.setInput(in);

        List<String> results = new ArrayList<>();

        copyJob.setConsumer(results::add);

        copyJob.run();

        assertThat(results, contains("A", "B", "C"));
        assertThat(closed.get(), Matchers.is(true));
    }

    @Test
    public void copyFileLinesInBeanBusExample() throws ArooaPropertyException, IOException {

        Path workDir = OurDirs.workPathDir(CopyJobTest.class, true);

        Properties properties = new Properties();
        properties.setProperty("work.dir", workDir.toString());

        Oddjob oddjob = new Oddjob();

        File config = new File(Objects.requireNonNull(getClass().getResource(
                "CopyFileByLines.xml")).getFile());

        oddjob.setFile(config);
        oddjob.setProperties(properties);

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        List<String> results = Files.readAllLines(workDir.resolve("LinesFoo.txt"));

        assertThat(results, contains(
                "orangesFoo",
                "applesFoo",
                "pearsFoo",
                "bananasFoo",
                "kiwisFoo"
        ));

        oddjob.destroy();
    }

}
