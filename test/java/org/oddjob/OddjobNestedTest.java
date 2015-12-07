package org.oddjob;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.ConversionPath;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.types.ValueType;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.io.FileType;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.values.properties.PropertiesJob;

public class OddjobNestedTest extends TestCase {
	private static final Logger logger = Logger.getLogger(OddjobNestedTest.class);
	
    /**
     * Test a nested Oddjob.
     * @throws ArooaParseException 
     * @throws URISyntaxException 
     *
     */
    public void testNestedOddjob() throws ArooaParseException, URISyntaxException {
    	
    	URL url = getClass().getClassLoader().getResource("org/oddjob/NestedOddjob.xml");
    	
    	File file = new File(url.toURI().getPath());
    	
    	Oddjob oj = new Oddjob();
		oj.setFile(file);
		
		ConsoleCapture console = new ConsoleCapture();
		try (ConsoleCapture.Close close = console.captureConsole()) {
			
	        oj.run();
		}
						
        assertEquals(ParentState.COMPLETE, 
        		oj.lastStateEvent().getState());
        
        Oddjob test = (Oddjob) new OddjobLookup(oj).lookup("nested");
        assertNotNull("Nested oddjob", test);
        
        assertEquals(ParentState.COMPLETE, 
        		test.lastStateEvent().getState());
        
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(2, lines.length);
		
		assertEquals("Hello World", lines[0].trim());
		assertEquals("Nested job said: Hello World", lines[1].trim());
		
        test.hardReset();
        
        assertEquals(ParentState.READY, 
        		test.lastStateEvent().getState());
        
        test.run();
        
        assertEquals(ParentState.COMPLETE, 
        		test.lastStateEvent().getState());
        
        oj.destroy();
    }

    public void testSetNestedWithArg() throws URISyntaxException {

    	URL url = getClass().getClassLoader().getResource("org/oddjob/NestedOddjobWithArg.xml");
    	
    	File file = new File(url.toURI().getPath());
    	        
        Oddjob oj = new Oddjob();
		oj.setFile(file);

        oj.run();
        
        assertEquals("Hello World", new OddjobLookup(oj).lookup("nested.args[0]"));
        assertEquals("Hello World", new OddjobLookup(oj).lookup("nested/echo.text"));
        
        oj.destroy();
    }

    public void testNestedPassingProperties() throws Exception {
    	    	    	
    	URL url = getClass().getClassLoader().getResource("org/oddjob/NestedOddjobWithProperty.xml");
    	
    	File file = new File(url.toURI().getPath());
    	        
        Oddjob oj = new Oddjob();
		oj.setFile(file);
        oj.run();
        
        assertEquals("Hello World", new OddjobLookup(oj).lookup("nested/echo.text"));
        
        oj.destroy();
    }    
    
    public void testExportJob() throws ArooaPropertyException, ArooaConversionException {

    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/OddjobExportJobTest.xml", 
    			getClass().getClassLoader()));
    	
    	oddjob.run();
    	
    	assertEquals(ParentState.COMPLETE, 
    			oddjob.lastStateEvent().getState());
    	
    	OddjobLookup lookup = new OddjobLookup(oddjob);
    	
    	Stateful stateful = lookup.lookup("inner/secret", 
    			Stateful.class);
    	
    	assertEquals(JobState.COMPLETE, 
    			stateful.lastStateEvent().getState());
    	
    	assertEquals("I'm a secret job",
    		lookup.lookup("inner/secret.text", String.class));
    	
    	oddjob.destroy();
    }
    
    public void testExportInOddjob() throws Exception {
    	
    	String config = 
    		"<oddjob>" +
    		" <job>" +
    		"  <oddjob id='nested'>" +
    		"   <export>" +
    		"      <value key='fruit' value='apple'/>" +
    		"   </export>" +
    		"   <configuration>" +
    		"    <arooa:configuration xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
    		"     <xml>" +
    		"      <xml>" +
    		"<oddjob>" +
    		" <job>" +
    		"  <variables id='fruits'>" +
    		"	<fruit>" +
    		"    <value value='${fruit}'/>" +
    		"	</fruit>" +
    		"  </variables>" +
    		" </job>" +
    		"</oddjob>" + 
    		"      </xml>" +
    		"     </xml>" +
    		"    </arooa:configuration>" +
    		"   </configuration>" +
    		"  </oddjob>" +
    		" </job>" +
    		"</oddjob>";
        
    	    	
        Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", config));
        oj.run();
        
        String fruit = new OddjobLookup(oj).lookup(
        		"nested/fruits.fruit", String.class);
        
        assertEquals("apple", fruit);
        
        oj.destroy();
    }    
    
    public void testArooaValueConversionAssumptionTest() throws NoConversionAvailableException, ConversionFailedException {
    	
    	ArooaConverter converter = new DefaultConverter();
    	
    	ConversionPath<ValueType, ArooaObject> path = 
    			converter.findConversion(
    					ValueType.class, ArooaObject.class);
    	assertEquals("ValueType-ArooaObject", path.toString());
    	
    	ValueType value1 = new ValueType();
    	value1.setValue(new ArooaObject("TEST"));
    	
    	ArooaObject result1 = converter.convert(value1, ArooaObject.class);
    	
    	assertEquals("TEST", result1.getValue());
    	
    	ValueType value2 = new ValueType();
    	FileType fileType = new FileType();
    	fileType.setFile(new File("foo.txt"));
    	value2.setValue(fileType);
    	
    	try {
    		converter.convert(value2, ArooaObject.class);
    		fail("Should fail.");
    	}
    	catch (ConversionFailedException e) {
    		// expected
    	}
    	
    	ValueType value3 = new ValueType();
    	value3.setValue(null);
    	
		ArooaObject result = converter.convert(value3, ArooaObject.class);
		
		assertNull(result);
    }
    
    public void testExportBean() throws ArooaPropertyException, ArooaConversionException {

    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration(
    			"org/oddjob/OddjobExportBeanTest.xml", 
    			getClass().getClassLoader()));
    	
    	oddjob.run();
    	
    	assertEquals(ParentState.COMPLETE, 
    			oddjob.lastStateEvent().getState());
    	
    	OddjobLookup lookup = new OddjobLookup(oddjob);
    	
    	String text1 = lookup.lookup("inner/echo1.text", 
    			String.class);
    	
    	assertEquals("class java.lang.Object", text1);
    	
    	String text2 = lookup.lookup("inner/echo2.text", 
    			String.class);
    	
    	assertEquals("class java.lang.String", text2);
    	
    	String text3 = lookup.lookup("inner/echo3.text", 
    			String.class);
    	
    	assertEquals("class org.oddjob.io.FileType", text3);
    	
    	String text4 = lookup.lookup("inner/echo4.text", 
    			String.class);
    	
    	// Note that a DynaBean such as variables doesn't have a class
    	// property.
    	assertEquals("Variables: vars", text4);
    	
    	oddjob.destroy();
    }
    
    public void testSharedInheritance() throws ArooaPropertyException, ArooaConversionException {
    	
        Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/SharedInheritance.xml", 
				getClass().getClassLoader()));
        oj.run();
        
        assertEquals(ParentState.COMPLETE,
        		oj.lastStateEvent().getState());
        
        
        OddjobLookup lookup = new OddjobLookup(oj);
        
        String snackText = lookup.lookup(
        		"snack-text.text", String.class);
        String connectionText = lookup.lookup(
        		"connection-text.text", String.class);
        
        assertEquals("Favourite snack: apples", snackText);
        assertEquals("Connection is: shared", connectionText);
        
        Oddjob inner = lookup.lookup("inner", Oddjob.class);
        
        inner.unload();
        
        Resetable snackEcho = lookup.lookup(
        		"snack-text", Resetable.class);
        Resetable connectionEcho = lookup.lookup(
        		"connection-text", Resetable.class);
        snackEcho.hardReset();
        connectionEcho.hardReset();
        ((Runnable) snackEcho).run();
        ((Runnable) connectionEcho).run();
        
        snackText = lookup.lookup(
        		"snack-text.text", String.class);
        connectionText = lookup.lookup(
        		"connection-text.text", String.class);
        
        assertEquals("Favourite snack: pizza", snackText);
        assertEquals("Connection is: ", connectionText);
        
        oj.destroy();
        
    }
    
    public void testExportedProperty() 
    throws ArooaPropertyException, ArooaConversionException {
    	
    	String xml =
    			"<oddjob>" +
    			" <job>" +
    			"  <oddjob id='nested'>" +
    			"   <configuration>" +
    		    "    <xml>" +
    		    "     <oddjob>" +
    		    "      <job>" +
    		    "       <sequential>" +
    		    "        <jobs>" +
    		    "         <properties id='props1'>" +
    		    "          <values>" +
    		    "           <value key='fruit.favourite' value='apple'/>" +
    		    "          </values>" +
    		    "         </properties>" +
    		    "         <properties id='props2' override='true'>" +
    		    "          <values>" +
    		    "           <value key='snack.favourite' value='pizza'/>" +
    		    "          </values>" +
    		    "         </properties>" +
    		    "        </jobs>" +
    		    "       </sequential>" +
    		    "      </job>" +
    		    "     </oddjob>" +
    		    "    </xml>" +
    		    "   </configuration>" +
    		    "   <properties>" +
    		    "    <properties>" +
    		    "     <values>" +
    		    "      <value key='fruit.favourite' value='banana'/>" +
    		    "      <value key='snack.favourite' value='chips'/>" +
    		    "     </values>" +
    		    "    </properties>" +
    		    "   </properties>" +
    		    "  </oddjob>" +
    		    " </job>" +
    		    "</oddjob>";
    		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("TEST", xml));

		oddjob.run();
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		PropertiesJob props1 = lookup.lookup("nested/props1", 
				PropertiesJob.class);
		PropertiesJob props2 = lookup.lookup("nested/props2", 
				PropertiesJob.class);
		
		Map<String, String> description1 = props1.describe();
		Map<String, String> description2 = props2.describe();
		
		
		assertEquals(1, description1.size());
		assertEquals(1, description2.size());
		
		assertEquals("apple *(banana) [Oddjob]", 
				description1.get("fruit.favourite"));
		assertEquals("pizza", 
				description2.get("snack.favourite"));
		
		oddjob.destroy();
    }
}
