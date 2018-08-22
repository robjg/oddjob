package org.oddjob.script;

import org.junit.Test;

import java.io.StringReader;

import javax.script.Invocable;
import javax.script.ScriptException;

import org.oddjob.OjTestCase;

public class ScriptCompilerTest extends OjTestCase {

   @Test
	public void testCompile() throws ScriptException {
		
		ScriptCompiler test = new ScriptCompiler(null, null);
		
		Evaluatable evaluatable = test.compileScript(
				new StringReader("result = 'hello';"));

		assertEquals(PreCompiled.class, evaluatable.getClass());
		
		evaluatable.eval();
				
		assertEquals("hello", evaluatable.get("result"));
	}

   @Test
	public void testNoVarAtComile() throws ScriptException {
		
		ScriptCompiler test = new ScriptCompiler(null, null);
		
		Evaluatable evaluatable = test.compileScript(
				new StringReader("result = fruit;"));

		assertEquals(PreCompiled.class, evaluatable.getClass());

		evaluatable.put("fruit", "apple");
		
		evaluatable.eval();
				
		assertEquals("apple", evaluatable.get("result"));
	}

	
   @Test
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
