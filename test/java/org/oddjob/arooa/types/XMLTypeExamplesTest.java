package org.oddjob.arooa.types;

import java.io.IOException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.xml.sax.SAXException;

public class XMLTypeExamplesTest extends XMLTestCase {

	public void testExample() throws ArooaPropertyException, ArooaConversionException, SAXException, IOException {
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/XMLTypeExample.xml",
				getClass().getClassLoader()));

		oddjob.run();
		
		assertEquals(JobState.COMPLETE, 
				oddjob.lastJobStateEvent().getJobState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		String result = lookup.lookup("vars.myXML", String.class);

		assertXMLEqual("<some-xml><![CDATA[Some Text]]></some-xml>", result);
		
		oddjob.destroy();
	}
}
