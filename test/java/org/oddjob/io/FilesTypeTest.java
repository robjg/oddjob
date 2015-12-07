/*
 * Copyright (c) 2005, Rob Gordon.
 */
package org.oddjob.io;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConverterHelper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.ElementMappings;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.ConversionPath;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.design.DesignElementProperty;
import org.oddjob.arooa.design.InstanceSupport;
import org.oddjob.arooa.design.model.MockDesignElementProperty;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.parsing.QTag;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.runtime.MockRuntimeConfiguration;
import org.oddjob.arooa.runtime.RuntimeConfiguration;
import org.oddjob.arooa.types.ListType;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.FragmentHelper;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;

/**
 *
 * @author Rob Gordon.
 */
public class FilesTypeTest extends TestCase {
	private static final Logger logger = Logger.getLogger(FilesTypeTest.class);
	
    public void testPattern() throws Exception {
    	OurDirs dirs = new OurDirs();
    	
        FilesType test = new FilesType();
        test.setFiles(dirs.base() + "/lib/*.jar");
        
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
        File[] fs = converter.convert(test, File[].class);

        assertTrue(fs.length > 1);
        
        for (int i = 0; i < fs.length; ++i) {
            System.out.println(fs[i]);
        }
        
        ConversionPath<FilesType, String[]> path = converter.findConversion(
        		FilesType.class, String[].class);
        assertEquals("FilesType-File[]-Object-String[]", path.toString());
        
        String[] strings = converter.convert(test, String[].class);
        
        assertTrue(strings.length > 1);
    }
        
    public void testNestedFileList() throws Exception {
    	OurDirs dirs = new OurDirs();
    	
        FilesType f = new FilesType();
        f.setFiles(dirs.base() + "/lib/*.jar");

		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
        File[] fs = (File[]) converter.convert(f, File[].class);
        assertTrue(fs.length > 1);
                
        for (int i = 0; i < fs.length; ++i) {
            System.out.println(fs[i]);
        }        
    }
    
    public void testXMLCreate() throws Exception {
    	String xml = "<files files='*.txt'/>";
    	
    	FilesType ft = (FilesType) OddjobTestHelper.createValueFromXml(xml);
    	
    	assertEquals("*.txt", ft.getFiles());
    }
    
    public void testXMLCreate2() throws Exception {
    	OurDirs dirs = new OurDirs();
    	
    	String xml = 
    		"<list merge='true'>" +
    		" <values>" +
    		"  <files files='" + dirs.base() + "/test/io/reference/test2.txt'/>" +
    		"  <files files='" + dirs.base() + "/test/io/reference/test*.txt'/>" +
    		" </values>" +
    		"</list>";
    	
    	ListType listType= (ListType) OddjobTestHelper.createValueFromXml(xml);
    	
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
    	File[] files = converter.convert(listType, File[].class);
    	
    	for (int i = 0; i < files.length; ++i) {
        	logger.debug(files[i]);
    	}
    	
    	assertEquals(4, files.length);
    	
    	Set<File> set = new HashSet<File>(Arrays.asList(files));
    	assertTrue(set.contains(
    			new File(dirs.base(), "test/io/reference/test1.txt")));
    }
    
    public void testXMLCreate3() throws Exception {
    	OurDirs dirs = new OurDirs();
    	
    	String xml = 
    		"<files files='" + dirs.base() + "/test/io/reference/test*.txt'/>";
    	
    	FilesType ft = (FilesType) OddjobTestHelper.createValueFromXml(xml);
    
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
    	File[] files = converter.convert(
    			ft, File[].class);
    	
    	assertEquals(3, files.length);
    	
    	logger.debug(files[0]);
    	logger.debug(files[1]);
    	
    	Set<File> set = new HashSet<File>(Arrays.asList(files));
    	assertTrue(set.contains(new File(dirs.base(), "test/io/reference/test1.txt")));
    }
    
    public static class MyFiles extends SimpleJob {
        File[] files;
    	public void setFiles(File[] files) throws IOException {
    		if (files == null) {
    			this.files = null;
    		}
    		else {
    			this.files = Files.expand(files);
    		}
    	}
    	public int execute() {
    		return 0;
    	}
    }
    
    public void testInOddjob() {
    	
    	OurDirs dirs = new OurDirs();
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <bean id='mine' class='" + MyFiles.class.getName() + "'>" +
    		"   <files>" +
    		"    <files files='" + dirs.base() + "/lib/*.jar'/>" +
    		"   </files>" +
    		"  </bean>" +
			" </job>" +
    		"</oddjob>";
				
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		MyFiles mine = (MyFiles) new OddjobLookup(oddjob).lookup("mine");
		
		assertTrue(mine.files.length > 1);
		
		oddjob.destroy();
    }
    
    public void testInOddjob2() throws Exception {
    	OurDirs dirs = new OurDirs();
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <variables id='v'>" +
    		"   <myfiles>" +
    		"      <list merge='true' unique='true'>" +
    		"       <values>" +
    		"        <files files='" + dirs.base() + "/test/io/reference/test*.txt'/>" +
    		"        <files files='" + dirs.base() + "/test/io/reference/test2.txt'/>" +
    		"       </values>" +
    		"      </list>" +
    		"   </myfiles>" +
    		"  </variables>" +
    		" </job>" +
    		"</oddjob>";
				
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("TEST", xml));
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		File[] files = lookup.lookup("v.myfiles", File[].class);
		
		assertEquals(3, files.length);
		
		oddjob.destroy();
    }

    public void testSupports() throws ArooaParseException {
    
    	ArooaSession session = new OddjobSessionFactory().createSession();

    	ArooaConverter converter = session.getTools().getArooaConverter();
    	
    	ElementMappings mappings = 
    		session.getArooaDescriptor().getElementMappings();
    	
    	assertTrue(checkElements(mappings.elementsFor(
    			new InstantiationContext(ArooaType.VALUE, 
    					new SimpleArooaClass(Object.class),
    					converter))));
    	
    	assertTrue(checkElements(mappings.elementsFor(
    			new InstantiationContext(ArooaType.VALUE, 
    					new SimpleArooaClass(ArooaValue.class),
    					converter))));

    	assertTrue(checkElements(mappings.elementsFor(
    			new InstantiationContext(ArooaType.VALUE, 
    					new SimpleArooaClass(File.class),
    					converter))));
    	
    	assertTrue(checkElements(mappings.elementsFor(
    			new InstantiationContext(ArooaType.VALUE, 
    					new SimpleArooaClass(File[].class),
    					converter))));
    	
    }
    
    /**
     * Why doesn't 'files' appear in the designer selection for File[]???
     * @throws ArooaParseException 
     */
    public void testSupports2() throws ArooaParseException {
    	
    	final ArooaSession session = 
    		new OddjobSessionFactory().createSession();

    	final ArooaContext context = new MockArooaContext() {
    		@Override
    		public ArooaSession getSession() {
    			return session;
    		}
    		@Override
    		public ArooaType getArooaType() {
    			return ArooaType.VALUE;
    		}
    		@Override
    		public RuntimeConfiguration getRuntime() {
    			return new MockRuntimeConfiguration() {
    				@Override
    				public ArooaClass getClassIdentifier() {
    					return new SimpleArooaClass(File[].class);
    				}
    			};
    		}
    	};

    	DesignElementProperty property = new MockDesignElementProperty() {
    		@Override
    		public ArooaContext getArooaContext() {
    			return context;
    		}
    	};
    	
    	InstanceSupport support = new InstanceSupport(property);
    	
    	QTag tags[] = support.getTags();
    	
    	Set<QTag> results = new HashSet<QTag>(Arrays.asList(tags));
    	
    	assertTrue(results.contains(new QTag("files")));
    }
    
    private boolean checkElements(ArooaElement elements[]) {
    	return new HashSet<ArooaElement>(
    			Arrays.asList(elements)).contains(
    					new ArooaElement("file"));
    }
    
    public void testFileListToString() throws NoConversionAvailableException, ConversionFailedException, ArooaParseException {

    	OurDirs dirs = new OurDirs();
    	
    	FilesType files1 = new FilesType();
    	files1.setFiles(dirs.base() + "/test/io/reference/test2.txt");
    	
    	FilesType files2 = new FilesType();
    	files2.setFiles(dirs.base() + "/test/io/reference/test*.txt");

    	ArooaSession session = new OddjobSessionFactory().createSession();;
    	ArooaConverter converter = session.getTools().getArooaConverter();
    	
    	File[] set1 = converter.convert(files1, File[].class);
    	File[] set2 = converter.convert(files2, File[].class);
    		
    	FilesType list = new FilesType();
    	list.setList(0, set1);
    	list.setList(1, set2);
    	
    	String result = converter.convert(list, String.class);
    	
    	assertEquals(new File(dirs.base(), "test/io/reference/test2.txt").getPath() + 
    			File.pathSeparator +
    			new File(dirs.base(), "test/io/reference/test1.txt").getPath() +
    			File.pathSeparator +
    			new File(dirs.base(), "test/io/reference/test3.txt").getPath(),
    			result);
    }
    
    public void testMixedTypesExample() throws IOException {
    	
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/io/FilesTypeMixedList.xml",
				getClass().getClassLoader()));

		oddjob.setArgs(new String[] { "d.jar", "e.jar" });
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {
			
			oddjob.run();
		}
				
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(5, lines.length);
		
		assertEquals(new File("a.jar").getCanonicalPath(), lines[0].trim());
		assertEquals("b.jar", lines[1].trim());
		assertEquals("c.jar", lines[2].trim());
		assertEquals("d.jar", lines[3].trim());
		assertEquals("e.jar", lines[4].trim());
		
		oddjob.destroy();
    }
    
    public void testSimpleExamples() throws ArooaParseException {
    	
    	// Just test the xml for now.
    	
    	FragmentHelper helper = new FragmentHelper();
    	
    	FilesType test = (FilesType) helper.createValueFromResource(
    			"org/oddjob/io/FilesTypeSimple1.xml");

    	assertEquals("Files, onefile.txt", test.toString());
    	
    	test = (FilesType) helper.createValueFromResource(
    			"org/oddjob/io/FilesTypeSimple2.xml");

    	assertEquals("Files, reports/*.txt", test.toString());
    	
    	test = (FilesType) helper.createValueFromResource(
    			"org/oddjob/io/FilesTypeSimple3.xml");
    	
    	assertEquals("Files, list of size 2", test.toString());
    }
}
