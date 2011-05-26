/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.ConverterHelper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.arooa.ConfiguredHow;
import org.oddjob.arooa.MockArooaBeanDescriptor;
import org.oddjob.arooa.ParsingInterceptor;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.xml.XMLConfiguration;

public class FileTypeTest extends TestCase {
	
	File ourFile;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		OurDirs dirs = new OurDirs();
		ourFile = dirs.relative("work/io/FileTypeTest.dat");
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
		public void run() {}
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
	}
	
	public void testFileManefestation() throws Exception {
		
		FileType test = new FileType();
		test.setFile(ourFile);

		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		Object v = converter.convert(test, File.class);
		assertTrue(v instanceof File);
	}
	
	public void testInputStream() throws Exception {
		
		ourFile.createNewFile();
		
		FileType test = new FileType();
		test.setFile(ourFile);

		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		Object result = converter.convert(
				test, InputStream.class);
		
		assertTrue(result instanceof InputStream);		
	}

	public void testOutputStream() throws Exception {
		
		FileType test = new FileType();
		test.setFile(ourFile);

		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		Object result = converter.convert(
				test, OutputStream.class);
		
		assertTrue(result instanceof OutputStream);		
	}

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
		
		DynaBean bean = (DynaBean) new OddjobLookup(oj).lookup("b");

		// check file
		assertEquals(ourFile, bean.get("file"));
		
		// check files
		File[] files = (File[]) bean.get("files");
		assertEquals(1, files.length);
		assertEquals(ourFile, files[0]);
		
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
}
