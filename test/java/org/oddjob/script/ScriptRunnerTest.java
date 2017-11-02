package org.oddjob.script;

import org.junit.Test;

import java.io.StringReader;

import javax.script.ScriptException;

import org.oddjob.OjTestCase;

public class ScriptRunnerTest extends OjTestCase {

   @Test
	public void testSimpleEval() throws ScriptException, NoSuchMethodException {

		ScriptCompiler compiler = new ScriptCompiler(null, null);
		
		Evaluatable evaluatable = compiler.compileScript(
				new StringReader("var result = 'hello';"));

		ScriptRunner test = new ScriptRunner("result");
		
		Object result = test.executeScript(evaluatable);
		
		assertEquals("hello", result);
		
		assertEquals("hello", evaluatable.get("result"));
	}
}
