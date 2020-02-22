/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.io;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.OurDirs;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;

public class DeleteJobTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(DeleteJobTest.class);

    private File workDir;

    @Before
    public void setUp() throws Exception {
        logger.debug("----------------" + getName() + "------------------");

        workDir = OurDirs.workPathDir(getClass().getSimpleName(), true)
                .toFile();
    }

    @Test
    public void testDeleteFile() throws Exception {

        File testFile = new File(workDir, "a");
        FileUtils.touch(testFile);

        WildcardSpec wild = new WildcardSpec(new File(workDir, "a"));
        File[] found = wild.findFiles();
        assertEquals(1, found.length);

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <delete>" +
                        "   <files>" +
                        " 	 <file file='" + workDir.getPath() + "/a'/>" +
                        "   </files>" +
                        "  </delete>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("TEST", xml));
        oj.run();

        assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

        assertFalse(found[0].exists());
    }

    @Test
    public void testDeleteFiles() throws Exception {

        FileUtils.touch(new File(workDir, "a"));
        FileUtils.touch(new File(workDir, "b"));
        FileUtils.touch(new File(workDir, "c"));

        WildcardSpec wild = new WildcardSpec(new File(workDir, "*"));
        File[] found = wild.findFiles();
        assertEquals(3, found.length);


        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/io/DeleteFilesExample.xml",
                getClass().getClassLoader()));

        oddjob.setArgs(new String[]{workDir.getPath()});
        oddjob.run();

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

        found = wild.findFiles();
        assertEquals(0, found.length);

        OddjobLookup lookup = new OddjobLookup(oddjob);

        assertEquals(3, (int) lookup.lookup("delete.fileCount", int.class));
        assertEquals(0, (int) lookup.lookup("delete.dirCount", int.class));
        assertEquals(0, (int) lookup.lookup("delete.errorCount", int.class));

        oddjob.destroy();
    }

    @Test
    public void testDeleteDir() throws Exception {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <delete id='delete'>" +
                        "   <files>" +
                        "    <file file='" + workDir.getPath() + "'/>" +
                        "   </files>" +
                        "  </delete>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("TEST", xml));

        oddjob.run();

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

        assertFalse(workDir.exists());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        assertEquals(0, (int) lookup.lookup("delete.fileCount", int.class));
        assertEquals(1, (int) lookup.lookup("delete.dirCount", int.class));
        assertEquals(0, (int) lookup.lookup("delete.errorCount", int.class));

        oddjob.destroy();
    }

    @Test
    public void testDeleteFullDir() throws Exception {

        FileUtils.touch(new File(workDir, "a"));
        FileUtils.touch(new File(workDir, "b"));
        FileUtils.touch(new File(workDir, "c"));


        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <delete>" +
                        "   <files>" +
                        "    <file file='" + workDir.getPath() + "'/>" +
                        "   </files>" +
                        "  </delete>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("XML", xml));

        oj.run();
        assertEquals(ParentState.EXCEPTION, oj.lastStateEvent().getState());

        assertTrue(workDir.exists());

        xml = "<oddjob>" +
                "<job>" +
                " <delete force='true'>" +
                "  <files>" +
                "   <file file='" + workDir.getPath() + "'/>" +
                "  </files>" +
                " </delete>" +
                "</job>" +
                "</oddjob>";

        oj.hardReset();
        oj.setConfiguration(new XMLConfiguration("TEST", xml));

        oj.run();
        assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

        assertFalse(workDir.exists());

        oj.destroy();
    }

    @Test
    public void testDeleteFileThatDoesntExist() throws IOException, InterruptedException {

        DeleteJob test = new DeleteJob();
        test.setFiles(new File[]{
                new File("doesntexist/reallydoesntexist")});

        Integer result = test.call();

        assertEquals(new Integer(0), result);

        assertEquals(0, test.getFileCount());
        assertEquals(0, test.getDirCount());
        assertEquals(0, test.getErrorCount());
    }

    @Test
    public void testSerialize() throws Exception {

        assertTrue(workDir.exists());
        DeleteJob test = new DeleteJob();
        test.setFiles(new File[]{workDir});

        Callable<Integer> copy =
                OddjobTestHelper.copy(test);
        copy.call();

        assertFalse(workDir.exists());
    }

    @Test
    public void testReallyRoot() throws IOException, InterruptedException {

        final File file = new File("/");

        // This is a really dangerous test so lets make sure wherever it's
        // being run File behaves as expected.
        assertTrue(file.getCanonicalFile().isAbsolute());
        assertNull(file.getCanonicalFile().getParentFile());

        DeleteJob test = new DeleteJob();
        test.setFiles(new File[]{file});

        try {
            test.call();
            fail("Should fail.");
        } catch (RuntimeException e) {
            assertEquals("You can not delete root (/*) files without setting the reallyRoot property to true.",
                    e.getMessage());
        }

        assertEquals(1, test.getErrorCount());

        test.reset();

        File[] rootFiles = FilesUtil.expand(new File("/*"));

        final File aRootFile = rootFiles[0];
        test.setFiles(new File[]{aRootFile});

        assertNull(aRootFile.getParentFile().getParentFile());

        try {
            test.call();
            fail("Should fail.");
        } catch (RuntimeException e) {
            assertEquals("You can not delete root (/*) files without setting the reallyRoot property to true.",
                    e.getMessage());
        }

        assertEquals(1, test.getErrorCount());

        test.reset();

        File aRootPath = new File("/a/../" + aRootFile.getName());
        test.setFiles(new File[] { aRootPath });

        assertThat(aRootPath.getCanonicalFile(), is(aRootFile));
        assertThat(aRootPath.getCanonicalFile().getParentFile().getParentFile(),
                nullValue());

        try {
            test.call();
            fail("Should fail.");
        } catch (RuntimeException e) {
            assertEquals("You can not delete root (/*) files without setting the reallyRoot property to true.",
                    e.getMessage());
        }

        assertEquals(1, test.getErrorCount());
    }
}
