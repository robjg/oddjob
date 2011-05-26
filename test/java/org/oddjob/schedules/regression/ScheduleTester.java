package org.oddjob.schedules.regression;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.xml.XMLConfiguration;

/**
 * 
 */
public class ScheduleTester {

	private List<SingleTestSchedule> tests = 
		new ArrayList<SingleTestSchedule>();
	
	private TimeZone tz;

	private String config;
	
	private ArooaSession session;
	
	public void setTest(int index, SingleTestSchedule test )  {
		tests.add(index, test);
	}
		
	public ScheduleTester(String configFile) 
	throws Exception {
		this(configFile, null);
	}
	
	public ScheduleTester(String configFile, TimeZone timeZone) 
	throws Exception {
		this.config = configFile;
		
		// because of eclipse.
		if ("testNone".equals(configFile)) {
			return;
		}
		
		this.tz = timeZone;
				
		InputStream in = this.getClass().getResourceAsStream(configFile);
        
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
    	
        StandardArooaParser parser = new StandardArooaParser(
        		this,
        		descriptor);

        parser.setExpectedDocumentElement(
        		new ArooaElement("schedule-tester"));
        
        parser.parse(
        		new XMLConfiguration(configFile, in));
       
        session = parser.getSession();
        
        session.getComponentPool().configure(this);
	}
	
	public void run() {
		TimeZone.setDefault(tz);

		for (SingleTestSchedule schedule: tests) {
			schedule.run();
		}
		
		ComponentPool pool = 
			session.getComponentPool();
		
		pool.contextFor(this).getRuntime().destroy();		
	}
	
	
	public String toString() {
		return config;
	}
	
}
