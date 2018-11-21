/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.io;

import org.apache.commons.beanutils.DynaBean;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaAnnotations;
import org.oddjob.arooa.ConfiguredHow;
import org.oddjob.arooa.MockArooaBeanDescriptor;
import org.oddjob.arooa.ParsingInterceptor;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.deploy.NoAnnotations;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class FileTypeTest extends OjTestCase {

    private File ourFile;

    @Before
    public void setUp() throws Exception {

        ourFile = OurDirs.workPathDir(getClass().getSimpleName(), true)
                .resolve("FileTypeTest.dat")
                .toFile();
    }


    // note: implement runnable solely that it get's
    // proxied so it's configured in order in sequential job
    // in testInOddjob().
    public static class Bean implements Runnable {
        File file;
        File[] files;
        InputStream is;
        OutputStream os;

        public void setFile(File file) {
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        public void setFiles(File[] files) {
            this.files = files;
        }

        public File[] getFiles() {
            return files;
        }

        public void setIs(InputStream is) {
            this.is = is;
        }

        public InputStream getIs() {
            return is;
        }

        public void setOs(OutputStream os) {
            this.os = os;
        }

        public OutputStream getOs() {
            return os;
        }

        public void run() {
        }
    }

    public static class BeanArooa extends MockArooaBeanDescriptor {
        @Override
        public ParsingInterceptor getParsingInterceptor() {
            return null;
        }

        @Override
        public ConfiguredHow getConfiguredHow(String property) {
            return ConfiguredHow.ATTRIBUTE;
        }

        @Override
        public String getComponentProperty() {
            return null;
        }

        @Override
        public boolean isAuto(String property) {
            return false;
        }

        @Override
        public ArooaAnnotations getAnnotations() {
            return new NoAnnotations();
        }
    }

    @Test
    public void testFileManefestation() throws Exception {

        FileType test = new FileType();
        test.setFile(ourFile);

        ArooaConverter converter =
                new ConverterHelper().getConverter();

        Object v = converter.convert(test, File.class);
        assertTrue(v instanceof File);
    }

    @Test
    public void testInputStream() throws Exception {

        ourFile.createNewFile();

        FileType test = new FileType();
        test.setFile(ourFile);

        ArooaConverter converter =
                new ConverterHelper().getConverter();

        InputStream result = converter.convert(
                test, InputStream.class);

        result.close();
    }

    @Test
    public void testOutputStream() throws Exception {

        FileType test = new FileType();
        test.setFile(ourFile);

        ArooaConverter converter =
                new ConverterHelper().getConverter();

        OutputStream result = converter.convert(
                test, OutputStream.class);

        result.close();
    }

    @Test
    public void testInOddjob() throws IOException {

        ourFile.delete();
        ourFile.createNewFile();

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <variables	id='v'>" +
                        "     <file>" +
                        "      <file file='" + ourFile.getPath() + "'/>" +
                        "     </file>" +
                        "    </variables>" +
                        "    <bean class='" + Bean.class.getName() + "' id='b'" +
                        " file='${v.file}' files='${v.file}' " +
                        "os='${v.file}' is='${v.file}'/>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("TEST", xml));

        oj.run();

        assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());

        DynaBean bean = (DynaBean) new OddjobLookup(oj).lookup("b");

        // check file
        assertEquals(ourFile.getCanonicalFile(), bean.get("file"));

        // check files
        File[] files = (File[]) bean.get("files");
        assertEquals(1, files.length);
        assertEquals(ourFile.getCanonicalFile(), files[0]);

        // check input output
        OutputStream os = (OutputStream) bean.get("os");
        os.write('A');
        os.flush();
        os.close();

        InputStream is = (InputStream) bean.get("is");
        char c = (char) is.read();

        assertEquals('A', c);

        is.close();
    }

    @Test
    public void testNullFile() throws Exception {

        FileType test = new FileType();

        ArooaConverter converter =
                new ConverterHelper().getConverter();

        File result = converter.convert(
                test, File.class);

        assertNull(result);
    }

    /**
     * Property will be blank.
     */
    @Test
    public void testNullInOddjob() throws Exception {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "    <properties id='props'>" +
                        "     <values>" +
                        "      <file key='no.file'/>" +
                        "     </values>" +
                        "    </properties>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("TEST", xml));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Properties props =
                lookup.lookup("props.properties", Properties.class);

        assertEquals("", props.getProperty("no.file"));

        oddjob.destroy();
    }

    @Test
    public void testInvalidFileName() {

        FileType test = new FileType();

        test.setFile(new File("*"));

        try {
            test.toCanonicalFile();
            fail("Should fail.");
        } catch (IOException e) {
            // expected.
        }
    }

    @Test
    public void testSerialisation()
            throws IOException, ClassNotFoundException {

        FileType test = new FileType();
        test.setFile(new File("."));

        FileType copy = OddjobTestHelper.copy(test);

        assertEquals(copy.toCanonicalFile(), test.toCanonicalFile());
    }
}
