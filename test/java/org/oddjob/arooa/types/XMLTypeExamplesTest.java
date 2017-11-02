package org.oddjob.arooa.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class XMLTypeExamplesTest {

   @Test
	public void testExample() throws ArooaPropertyException, ArooaConversionException, SAXException, IOException {
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/arooa/types/XMLTypeExample.xml",
				getClass().getClassLoader()));

		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		String result = lookup.lookup("vars.myXML", String.class);

		Diff diff = DiffBuilder.compare("<some-xml><![CDATA[Some Text]]></some-xml>")
				.withTest(result).ignoreWhitespace()
				.build();
		
		assertFalse(diff.toString(), diff.hasDifferences());
		
		oddjob.destroy();
	}
}
