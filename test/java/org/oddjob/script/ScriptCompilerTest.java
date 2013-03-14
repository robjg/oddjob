package org.oddjob.script;

import java.io.StringReader;

import javax.script.Invocable;
import javax.script.ScriptException;

import junit.framework.TestCase;

public class ScriptCompilerTest extends TestCase {

	public void testCompile() throws ScriptException {
		
		ScriptCompiler test = new ScriptCompiler(null, null);
		
		Evaluatable evaluatable = test.compileScript(
				new StringReader("result = 'hello';"));

		assertEquals(PreCompiled.class, evaluatable.getClass());
		
		evaluatable.eval();
				
		assertEquals("hello", evaluatable.get("result"));
	}

	public void testNoVarAtComile() throws ScriptException {
		
		ScriptCompiler test = new ScriptCompiler(null, null);
		
		Evaluatable evaluatable = test.compileScript(
				new StringReader("result = fruit;"));

		assertEquals(PreCompiled.class, evaluatable.getClass());

		evaluatable.put("fruit", "apple");
		
		evaluatable.eval();
				
		assertEquals("apple", evaluatable.get("result"));
	}

	
	public void testInvocable() throws ScriptException, NoSuchMethodException {
		
		ScriptCompiler test = new ScriptCompiler(null, null);
		

		Evaluatable evaluatable = 
			test.compileScript(
					new StringReader(
							"function hello() { return 'hello'; };"));
		
		assertNotNull(test.getInvocable());
		
		Invocable invocable = test.getInvocable();
		
		try {
			invocable.invokeFunction("hello");
			fail("Expected to fail with no such method.");
		} catch (NoSuchMethodException e) {
			// expected.
		}
		
		evaluatable.eval();
		
		Object result = invocable.invokeFunction("hello");
		
		assertEquals("hello", result);
	}
}
