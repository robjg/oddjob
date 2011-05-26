package org.oddjob.monitor.model;

import org.oddjob.Oddjob;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.util.MockThreadManager;
import org.oddjob.util.ThreadManager;

import junit.framework.TestCase;

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
		
		JobTreeModel treeModel = new JobTreeModel();
		
		JobTreeNode test = new JobTreeNode(
				explorerModel, treeModel);
		
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
