package org.oddjob.framework;

import java.util.concurrent.Callable;

import junit.framework.TestCase;

import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

public class CallableWrapperTest extends TestCase {

	public static class OurCallable implements Callable<Integer> {
	
		boolean ran;
		
		int status;
		
        public Integer call() {
        	ran = true;
        	return new Integer(status);
        }
        public boolean isRan() {
        	return ran;
        }
        public void setStatus(int status) {
			this.status = status;
		}
        public String toString() {
        	return "OurCallable";
        }
	}
	
    public void testInOddjob() throws Exception {
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <bean class='" + OurCallable.class.getName() + "' id='r' />" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	oddjob.run();
    	
    	OddjobLookup lookup = new OddjobLookup(oddjob);
    	
    	Object runnable = lookup.lookup("r");
    	assertEquals(JobState.COMPLETE, Helper.getJobState(runnable));
    	
    	Boolean ran = lookup.lookup("r.ran", Boolean.class);
    	
    	assertEquals(new Boolean(true), ran);
    	
    	oddjob.destroy();
    }

    public void testIncompleteInOddjob() throws Exception {
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <bean class='" + OurCallable.class.getName() + 
    				"' id='r' status='10'/>" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	oddjob.run();
    	
    	OddjobLookup lookup = new OddjobLookup(oddjob);
    	
    	Object runnable = lookup.lookup("r");
    	assertEquals(JobState.INCOMPLETE, Helper.getJobState(runnable));
    	
    	Boolean ran = lookup.lookup("r.ran", Boolean.class);
    	
    	assertEquals(new Boolean(true), ran);
    	
    	oddjob.destroy();
    }
	
	public static class OurCallable2 implements Callable<Void> {
		
		boolean ran;
		
        public Void call() {
        	ran = true;
        	return null;
        }
        public boolean isRan() {
        	return ran;
        }
        public String toString() {
        	return "OurCallable";
        }
	}
	
    public void testVoidCallableInOddjob() throws Exception {
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <bean class='" + OurCallable2.class.getName() + 
    				"' id='r' />" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob oddjob = new Oddjob();
    	oddjob.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	oddjob.run();
    	
    	OddjobLookup lookup = new OddjobLookup(oddjob);
    	
    	Object runnable = lookup.lookup("r");
    	assertEquals(JobState.COMPLETE, Helper.getJobState(runnable));
    	
    	Boolean ran = lookup.lookup("r.ran", Boolean.class);
    	
    	assertEquals(new Boolean(true), ran);
    	
    	oddjob.destroy();
    }
}
