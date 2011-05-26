package org.oddjob.values.types;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.scheduling.ManualClock;

public class NowTypeTest extends TestCase {

   
    public void testInOddjob() throws Exception {
    	        
        Oddjob oddjob = new Oddjob();
        oddjob.setExport("clock", new ArooaObject(
        		new ManualClock("2009-07-25 12:15")));
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/values/types/NowExample.xml", 
				getClass().getClassLoader()));
        
        oddjob.run();
        
        OddjobLookup lookup = new OddjobLookup(oddjob);
        
        assertEquals("Now: " + lookup.lookup("time.now", Object.class), 
        		lookup.lookup("time.now").toString());
        
        assertEquals("12:15 PM", lookup.lookup("time.formatted", String.class));
        
        oddjob.destroy();
    }

}
