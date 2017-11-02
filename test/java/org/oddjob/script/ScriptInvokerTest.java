package org.oddjob.script;

import org.junit.Test;

import java.io.IOException;

import javax.script.ScriptException;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.io.BufferType;
import org.oddjob.state.JobState;

public class ScriptInvokerTest extends OjTestCase {

   @Test
	public void testInvokeScript() throws IOException, ScriptException, NoSuchMethodException {

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
