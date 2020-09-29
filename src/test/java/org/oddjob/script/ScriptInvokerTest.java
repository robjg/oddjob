package org.oddjob.script;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.state.JobState;

import javax.script.ScriptException;
import java.io.IOException;

public class ScriptInvokerTest extends OjTestCase {

   @Test
	public void testInvokeScript() throws IOException, ScriptException, NoSuchMethodException {

		ScriptJob scriptJob = new ScriptJob();
		scriptJob.setLanguage("JavaScript");
		scriptJob.setScript("function snack(colour) {" +
				" return colour + ' apple'}");
		scriptJob.setBeans("dummy", "foo");
		scriptJob.run();
		
		assertEquals(JobState.COMPLETE, 
				scriptJob.lastStateEvent().getState());
		
		ScriptInvoker test = new ScriptInvoker(scriptJob.getInvocable());
		
		ConvertableArguments arguments = new ConvertableArguments(
				new DefaultConverter(), "red");

		String result = (String) test.invoke("snack", arguments);
		
		assertEquals("red apple", result);
	}	
}
