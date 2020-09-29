package org.oddjob.script;

import org.junit.Test;
import org.oddjob.OjTestCase;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import java.io.StringReader;

public class ScriptCompilerTest extends OjTestCase {

   @Test
	public void testCompile() throws ScriptException {
		
		ScriptCompiler test = new ScriptCompiler(null, null);
		
		Evaluatable evaluatable = test.compileScript(
				new StringReader("var result = 'hello';"));

		assertEquals(PreCompiled.class, evaluatable.getClass());

	   ScriptContext scriptContext = evaluatable.getScriptContext();

		evaluatable.eval(scriptContext);

	   Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);

		assertEquals("hello",
				bindings.get("result"));
	}

   @Test
	public void testNoVarAtComile() throws ScriptException {
		
		ScriptCompiler test = new ScriptCompiler(null, null);
		
		Evaluatable evaluatable = test.compileScript(
				new StringReader("result = fruit;"));

		assertEquals(PreCompiled.class, evaluatable.getClass());

		ScriptContext scriptContext = evaluatable.getScriptContext();

		scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).put("fruit", "apple");
		
		evaluatable.eval(scriptContext);
				
		assertEquals("apple",
				scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).get("result"));
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

		ScriptContext scriptContext = evaluatable.getScriptContext();

		evaluatable.eval(scriptContext);
		
		Object result = invocable.invokeFunction("hello");
		
		assertEquals("hello", result);
	}
}
