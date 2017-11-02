package org.oddjob.monitor.control;

import org.junit.Test;

import java.awt.Component;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.tree.TreePath;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.model.DetailModel;
import org.oddjob.monitor.model.JobTreeModel;
import org.oddjob.monitor.model.JobTreeNode;
import org.oddjob.monitor.model.MockExplorerModel;
import org.oddjob.monitor.view.DetailView;
import org.oddjob.util.ThreadManager;

public class DetailControllerTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(DetailControllerTest.class);
	Component comp;
	
	class OurExplorerModel extends MockExplorerModel {
		
		Oddjob oddjob;
		@Override
		public Oddjob getOddjob() {
			return oddjob;
		}
		
		@Override
		public ThreadManager getThreadManager() {
			return null;
		}
		
		@Override
		public ContextInitialiser[] getContextInitialisers() {
			return new ContextInitialiser[0];
		}
	}
		
   @Test
	public void testSelectionOnCut() throws Exception {
		
		OurExplorerModel explorerModel = new OurExplorerModel();
		
		Oddjob oddjob = new Oddjob();
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean id='x'/>" +
			" </job>" +
			"</oddjob>";
		
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.run();
		
		explorerModel.oddjob = oddjob;
		
		JobTreeModel model = new JobTreeModel();
		
		JobTreeNode root = new JobTreeNode(explorerModel, model);
		model.setRootTreeNode(root);
		
		final JTree tree = new JTree(model);
		tree.setShowsRootHandles(true);

		DetailModel detailModel = new DetailModel();
		
		if (Thread.currentThread().getContextClassLoader() == null) {
			logger.warn("Context class loader is null -  " +
					"What is setting this to null!!!???");
			Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		}
		
		// Test fails sometimes with NPE. Not sure why. 
		// Something to do with ContextClassLoader being null.
		DetailController test = new DetailController(
				detailModel, new DetailView(detailModel));
		tree.addTreeSelectionListener(test);
	
		root.setVisible(true);
		
		assertNull(detailModel.getSelectedJob());
		assertEquals(false, tree.isExpanded(0));
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				tree.expandRow(0);
				tree.setSelectionRow(1);
			}
		});
		
		Object x = detailModel.getSelectedJob();
		assertNotNull(x);
		
		final DragPoint xDrag = 
			oddjob.provideConfigurationSession().dragPointFor(x);
		
		final AtomicReference<Exception> er = new AtomicReference<Exception>();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				DragTransaction trn = xDrag.beginChange(ChangeHow.FRESH);
				xDrag.cut();
				try {
					trn.commit();
				} catch (ArooaParseException e) {
					trn.rollback();
					er.set(e);
				}
			}
		});
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				// Wait for queue to drain.
			}
		});
		
		if (er.get() != null) {
			throw er.get();
		}
		
		TreePath path = tree.getSelectionPath();
		assertNull(path);
		
		assertNull(detailModel.getSelectedJob());
		assertEquals(false, tree.isExpanded(0));
		
		comp = tree;
	}
	
	public static void main(String[] args) throws Exception {
		DetailControllerTest test = new DetailControllerTest();
		test.testSelectionOnCut();
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		frame.getContentPane().add(test.comp);
		frame.pack();
		frame.setVisible(true);
	}
}
