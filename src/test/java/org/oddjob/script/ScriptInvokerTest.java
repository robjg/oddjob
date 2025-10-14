package org.oddjob.script;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.state.JobState;

import javax.script.ScriptException;

public class ScriptInvokerTest extends OjTestCase {

   @Test
	public void testInvokeScript() throws ScriptException, NoSuchMethodException {

       ArooaSession session = new StandardArooaSession();

		ScriptJob scriptJob = new ScriptJob();
        scriptJob.setArooaSession(session);
		scriptJob.setLanguage("JavaScript");
		scriptJob.setScript("function snack(colour) {" +
				" return colour + ' apple'}");
		scriptJob.setBind("dummy", "foo");
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
