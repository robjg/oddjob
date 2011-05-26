package org.oddjob.script;

import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;

import junit.framework.TestCase;

public class ScriptAndInvokeTest extends TestCase {

	public void testVariableFromJava() throws ArooaPropertyException, ArooaConversionException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" + 
			"    <script id='s' language='JavaScript'>" +
			"     <input>" +
			"      <buffer>" +
			"function snack() {" +
			" return 'apple'}" +
			"      </buffer>" +
			"     </input>" +
		    "    </script>" +
		    "    <variables id='v'>" +
		    "     <snack>" +
		    "      <invoke function='snack'>" +
		    "       <source>" +
		    "        <value value='${s.invocable}'/>"+
		    "       </source>" +
		    "      </invoke>" +
		    "     </snack>" +
		    "    </variables>" +
		    "   </jobs>" +
		    "  </sequential>" +
		    " </job>" + 
		    "</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(oj));
		
		String snack = new OddjobLookup(oj).lookup("v.snack", String.class);
		
		assertEquals("apple", snack);
	}

}
