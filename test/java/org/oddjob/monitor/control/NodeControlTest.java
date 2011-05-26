package org.oddjob.monitor.control;

import java.awt.Component;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.tree.TreePath;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconListener;
import org.oddjob.images.IconTip;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.model.JobTreeModel;
import org.oddjob.monitor.model.JobTreeNode;
import org.oddjob.monitor.model.MockExplorerModel;
import org.oddjob.util.ThreadManager;

public class NodeControlTest extends TestCase {

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
		
	public static class OurIconic extends SimpleJob {
	
		String icon = "apple"; 
		IconListener listener;
		
		@Override
		protected int execute() throws Throwable {
			return 0;
		}
		
		public void addIconListener(IconListener listener) {
			if (this.listener != null) {
				throw new RuntimeException("Doh!");
			}
			this.listener = listener;
			listener.iconEvent(new IconEvent(this, "apple"));
		}
		
		public IconTip iconForId(String id) {
			assertEquals("apple", id);
			return new IconTip(new byte[0], "apple");
		}
		
		public void removeIconListener(IconListener listener) {
			assertEquals(this.listener, listener);
			this.listener = null;
		}
	}
	
	public void testIconsOKonOddjobStart() throws InterruptedException, InvocationTargetException {
		
		OurExplorerModel explorerModel = new OurExplorerModel();
		
		Oddjob oddjob = new Oddjob();
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean id='x' class='" + OurIconic.class.getName() + "'/>" +
			" </job>" +
			"</oddjob>";
		
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.run();
		
		explorerModel.oddjob = oddjob;
		
		JobTreeModel model = new JobTreeModel();
		
		JobTreeNode root = new JobTreeNode(explorerModel, model);
		model.setRootTreeNode(root);
		
		JTree tree = new JTree(model);
		tree.setShowsRootHandles(true);
		
		NodeControl test = new NodeControl();
		
		root.setVisible(true);
		
		tree.addTreeWillExpandListener(test);
		
		assertEquals(false, tree.isExpanded(0));
		tree.expandRow(0);
		
		TreePath path = tree.getPathForRow(1);
		
		JobTreeNode result = (JobTreeNode) path.getLastPathComponent();

		assertEquals("apple", result.getIcon().getToolTip());
		
		this.comp = tree;
	}
	
	public void testIconListenerRemovedFromCutNode() throws Exception {
		
		OurExplorerModel explorerModel = new OurExplorerModel();
		
		final Oddjob oddjob = new Oddjob();
		
		explorerModel.oddjob = oddjob;
		
		JobTreeModel model = new JobTreeModel();
		
		JobTreeNode root = new JobTreeNode(explorerModel, model);
		model.setRootTreeNode(root);
		root.setVisible(true);
		
		JTree tree = new JTree(model);
		
		NodeControl test = new NodeControl();
		
		tree.addTreeWillExpandListener(test);
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <bean id='x' class='" + OurIconic.class.getName() + "'/>" +
			" </job>" +
			"</oddjob>";
		
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		Toolkit.getDefaultToolkit();
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				oddjob.run();
			}
		});
		
		assertEquals(false, tree.isExpanded(0));
		tree.expandRow(0);
		
		TreePath path = tree.getPathForRow(1);
		
		JobTreeNode result = (JobTreeNode) path.getLastPathComponent();

		assertEquals("apple", result.getIcon().getToolTip());
		
		OurIconic component = (OurIconic) new OddjobLookup(oddjob).lookup("x");
		final DragPoint drag = oddjob.provideConfigurationSession().dragPointFor(component);
		
		final AtomicReference<Exception> er = new AtomicReference<Exception>();
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				DragTransaction trn = drag.beginChange(ChangeHow.FRESH);
				drag.cut();
				try {
					trn.commit();
				} catch (ArooaParseException e) {
					trn.rollback();
					er.set(e);
				}
			}
		});
		
		if (er.get() != null) {
			throw er.get();
		}
		
		assertEquals(null, component.listener);
		
		this.comp = tree;
	}
	
	public void testPasteIntoAnAlreadyExpandedNode() throws InterruptedException, InvocationTargetException {
		
		OurExplorerModel explorerModel = new OurExplorerModel();
		
		final Oddjob oddjob = new Oddjob();
		
		explorerModel.oddjob = oddjob;
		
		JobTreeModel model = new JobTreeModel();
		
		JobTreeNode root = new JobTreeNode(explorerModel, model);
		model.setRootTreeNode(root);
		root.setVisible(true);
		
		JTree tree = new JTree(model);
		tree.setShowsRootHandles(true);
		
		NodeControl test = new NodeControl();
		
		tree.addTreeWillExpandListener(test);
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential id='x'>" +
			"   <jobs>" +
			"    <bean/>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		Toolkit.getDefaultToolkit();
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				oddjob.run();
			}
		});
		
		tree.expandRow(0);
		tree.expandRow(1);
		
		Object seqential = new OddjobLookup(oddjob).lookup("x");
		
		final DragPoint drag = oddjob.provideConfigurationSession().dragPointFor(
				seqential);
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				try {
					DragTransaction trn = drag.beginChange(ChangeHow.FRESH);
					drag.paste(0, 			
							"<bean id='x' class='" + OurIconic.class.getName() + "'/>");
					trn.commit();
				} catch (ArooaParseException e) {
					throw new RuntimeException(e);
				}
			}
		});
		
		TreePath path = tree.getPathForRow(2);
		
		JobTreeNode result = (JobTreeNode) path.getLastPathComponent();

		assertEquals("apple", result.getIcon().getToolTip());
		
		this.comp = tree;
	}
	
	public static void main(String[] args) throws Exception {
		NodeControlTest test = new NodeControlTest();
		test.testPasteIntoAnAlreadyExpandedNode();
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		frame.getContentPane().add(test.comp);
		frame.pack();
		frame.setVisible(true);
	}
}
