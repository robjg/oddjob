package org.oddjob.monitor.contexts;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.Oddjob;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ExplorerContextImpl;
import org.oddjob.monitor.model.MockExplorerModel;
import org.oddjob.util.ThreadManager;

public class ContextInitializerTest extends OjTestCase {

	class OurInitialiser implements ContextInitialiser {
		public void initialise(ExplorerContext context) {
			if (context.getParent() == null) {
				context.setValue("fruit", "apple");
			}
			else {
				context.setValue("fruit", "orange");
			}
		}
	}

	class OurModel extends MockExplorerModel {

		@Override
		public Oddjob getOddjob() {
			return new Oddjob();
		}
		
		@Override
		public ThreadManager getThreadManager() {
			return null;
		}
		
		@Override
		public ContextInitialiser[] getContextInitialisers() {
			return new ContextInitialiser[] {
				new OurInitialiser()
			};
		}
	}
	
   @Test
	public void testInFirstLevel() {

		ExplorerContext context = new ExplorerContextImpl(new OurModel());
		assertEquals("apple", context.getValue("fruit"));
		
	}
		
   @Test
	public void testSecondLevel() {
		
		ExplorerContext context = new ExplorerContextImpl(new OurModel());
		ExplorerContext next = context.addChild(new Object());
		assertEquals("orange", next.getValue("fruit"));
		
	}
}
