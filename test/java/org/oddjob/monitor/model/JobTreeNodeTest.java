package org.oddjob.monitor.model;

import java.util.concurrent.Executor;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.util.MockThreadManager;
import org.oddjob.util.ThreadManager;

public class JobTreeNodeTest extends TestCase {

	class OurModel extends MockExplorerModel {
		
		Oddjob oddjob;
		
		@Override
		public Oddjob getOddjob() {
			return oddjob;
		}
		
		@Override
		public ThreadManager getThreadManager() {
			return new MockThreadManager();
		}
		
		@Override
		public ContextInitialiser[] getContextInitialisers() {
			return new ContextInitialiser[0];
		}
	}
	
	class InlineExecutor implements Executor {
		
		@Override
		public void execute(Runnable command) {
			command.run();
		}		
	}
	
	class OurContextFactory implements ExplorerContextFactory {
		@Override
		public ExplorerContext createFrom(ExplorerModel explorerModel) {
			return new MockExplorerContext() {
				@Override
				public ExplorerContext addChild(Object child) {
					return this;
				}
			};
		}
	}
	
	public void testChildren() {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.load();

		OurModel explorerModel = new OurModel();
		explorerModel.oddjob = oddjob;
		
		JobTreeModel treeModel = new JobTreeModel(new InlineExecutor());
		
		JobTreeNode test = new JobTreeNode(
				explorerModel, treeModel, 
				new InlineExecutor(), 
				new OurContextFactory());
		
		assertEquals(0, test.getChildCount());
		
		test.setVisible(true);
		
		assertEquals(1, test.getChildCount());
		
		test.setVisible(false);
		
		assertEquals(1, test.getChildCount());
		
		test.setVisible(true);
		
		assertEquals(1, test.getChildCount());
		
		test.destroy();
	}
}
