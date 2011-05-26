package org.oddjob;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

public class OddjobNestedTest extends TestCase {
	private static final Logger logger = Logger.getLogger(OddjobNestedTest.class);
	
    /**
     * Test a nested Oddjob.
     * @throws ArooaParseException 
     *
     */
    public void testNestedOddjob() throws ArooaParseException {
    	
    	URL url = getClass().getClassLoader().getResource("org/oddjob/NestedOddjob.xml");
    	
    	File file = new File(url.getFile());
    	
    	Oddjob oj = new Oddjob();
		oj.setFile(file);
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
						
        oj.run();

        assertEquals(JobState.COMPLETE, 
        		oj.lastJobStateEvent().getJobState());
        
        Oddjob test = (Oddjob) new OddjobLookup(oj).lookup("nested");
        assertNotNull("Nested oddjob", test);
        
        assertEquals(JobState.COMPLETE, 
        		test.lastJobStateEvent().getJobState());
        
		console.close();
		console.dump(logger);
		
		String[] lines = console.getLines();
		
		assertEquals(2, lines.length);
		
		assertEquals("Hello World", lines[0].trim());
		assertEquals("Nested job said: Hello World", lines[1].trim());
		
        test.hardReset();
        
        assertEquals(JobState.READY, 
        		test.lastJobStateEvent().getJobState());
        
        test.run();
        
        assertEquals(JobState.COMPLETE, 
        		test.lastJobStateEvent().getJobState());
        
        oj.destroy();
    }

    public void testSetNestedWithArg() {

    	URL url = getClass().getClassLoader().getResource("org/oddjob/NestedOddjobWithArg.xml");
    	
    	File file = new File(url.getFile());
    	        
        Oddjob oj = new Oddjob();
		oj.setFile(file);

        oj.run();
        
        assertEquals("Hello World", new OddjobLookup(oj).lookup("nested.args[0]"));
        assertEquals("Hello World", new OddjobLookup(oj).lookup("nested/echo.text"));
        
        oj.destroy();
    }

    public void testNestedPassingProperties() throws Exception {
    	    	    	
    	URL url = getClass().getClassLoader().getResource("org/oddjob/NestedOddjobWithProperty.xml");
    	
    	File file = new File(url.getFile());
    	        
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
    	
    	assertEquals(JobState.COMPLETE, 
    			oddjob.lastJobStateEvent().getJobState());
    	
    	OddjobLookup lookup = new OddjobLookup(oddjob);
    	
    	Stateful stateful = lookup.lookup("inner/secret", 
    			Stateful.class);
    	
    	assertEquals(JobState.COMPLETE, 
    			stateful.lastJobStateEvent().getJobState());
    	
    	try {
    		lookup.lookup("inner/secret.text", String.class);
    		
    		fail("Exception expected because exported thing is an ValueType.");
    	}
    	catch (ArooaPropertyException e) {
    		// expected
    	}
    	try {
    		lookup.lookup("inner/secret.value.text", String.class);
    		
    		fail("Exception expected because exported thing is an ValueType.");
    	}
    	catch (ArooaPropertyException e) {
    		// expected
    	}
    	
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
    
    public void testSharedInheritance() throws ArooaPropertyException, ArooaConversionException {
    	
        Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration(
				"org/oddjob/SharedInheritance.xml", 
				getClass().getClassLoader()));
        oj.run();
        
        assertEquals(JobState.COMPLETE,
        		oj.lastJobStateEvent().getJobState());
        
        
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
}
