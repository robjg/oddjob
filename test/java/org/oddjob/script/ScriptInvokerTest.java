package org.oddjob.script;

import java.io.IOException;

import junit.framework.TestCase;

import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.io.BufferType;
import org.oddjob.state.JobState;

public class ScriptInvokerTest extends TestCase {

	public void testInvokeScript() throws IOException {

		BufferType buffer = new BufferType();
		buffer.setText("function snack(colour) {" +
			" return colour + ' apple'}");
		buffer.configured();
		
		ScriptJob scriptJob = new ScriptJob();
		scriptJob.setLanguage("JavaScript");
		scriptJob.setInput(buffer.toInputStream());
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
