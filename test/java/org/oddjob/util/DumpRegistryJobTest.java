package org.oddjob.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;

public class DumpRegistryJobTest extends TestCase {

	private static final Logger logger = 
			Logger.getLogger(DumpRegistryJobTest.class);
	
	public void testExample() throws ArooaPropertyException, ArooaConversionException {
		
		File file = new File(getClass().getResource(
				"DumpRegistryJobExample.xml").getFile());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setFile(file);

		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		String out = lookup.lookup("vars.results", String.class);

		logger.info(out);
		
		String[] lines = lookup.lookup("vars.results", String[].class);
		
		String[] expected = OddjobTestHelper.streamToLines(getClass(
				).getResourceAsStream("DumpRegistryJobExample.txt"));
		
		for (int i = 0; i < expected.length; ++i) {
			String expreg = expected[i];
			String line = lines[i];
			Pattern pattern = Pattern.compile(expreg);
			Matcher matcher = pattern.matcher(line);
			boolean match = matcher.lookingAt();
			assertEquals("Line " + i + ": " + expreg + "<>" + line, 
					true, match);
		}
		
		assertEquals(17, lines.length);

		oddjob.destroy();
	}
}
