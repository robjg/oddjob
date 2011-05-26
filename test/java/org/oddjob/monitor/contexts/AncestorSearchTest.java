package org.oddjob.monitor.contexts;

import org.oddjob.monitor.context.AncestorSearch;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.MockExplorerContext;

import junit.framework.TestCase;

public class AncestorSearchTest extends TestCase {

	class Context1 extends MockExplorerContext {
		
		@Override
		public Object getValue(String key) {
			assertEquals("fruit", key);
			return "apple";
		}
	}
	
	public void testInFirstLevel() {
		
		AncestorSearch test = new AncestorSearch(new Context1());
		
		Object result = test.getValue("fruit");
		
		assertEquals("apple", result);
	}
	
	
	class Context2 extends MockExplorerContext {
		
		@Override
		public Object getValue(String key) {
			assertEquals("fruit", key);
			return null;
		}
		
		@Override
		public ExplorerContext getParent() {
			return new Context1();
		}
	}
	
	public void testInParent() {
		
		AncestorSearch test = new AncestorSearch(new Context2());
		
		Object result = test.getValue("fruit");
		
		assertEquals("apple", result);
	}
}
