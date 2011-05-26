package org.oddjob.state;

import junit.framework.TestCase;

import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.xml.XMLConfiguration;

public class JobStateConvertletProviderTest extends TestCase {

	public static class OurBean {
		
		JobState jobState;
		
		@ArooaAttribute
		public void setJobState(JobState jobState) {
			this.jobState = jobState;
		}
	}
	
	public void testConversions() throws ArooaParseException {

    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
		
		OurBean bean = new OurBean();
		
		StandardArooaParser parser = new StandardArooaParser(bean, descriptor);
		
		parser.parse(new XMLConfiguration("TEST", "<test jobState='COMPLETE'/>"));
		
		assertEquals(JobState.COMPLETE, bean.jobState);
	}
	
}
