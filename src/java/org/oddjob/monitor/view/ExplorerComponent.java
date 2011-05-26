package org.oddjob.monitor.view;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DropMode;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.oddjob.arooa.design.designer.ArooaTransferHandler;
import org.oddjob.arooa.design.designer.ArooaTree;
import org.oddjob.arooa.design.designer.TransferEvent;
import org.oddjob.arooa.design.designer.TransferEventListener;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.monitor.control.DetailController;
import org.oddjob.monitor.control.NodeControl;
import org.oddjob.monitor.control.PropertyPolling;
import org.oddjob.monitor.model.ConfigContextSearch;
import org.oddjob.monitor.model.DetailModel;
import org.oddjob.monitor.model.ExplorerModel;
import org.oddjob.monitor.model.JobTreeModel;
import org.oddjob.monitor.model.JobTreeNode;

/**
 * The tree view.
 * 
 * @author Rob Gordon
 */

public class ExplorerComponent extends JPanel {
	private static final long serialVersionUID = 0;

	private final JTree tree;
	
	private final JScrollPane treeScroll;

	private MonitorMenuBar menuBar;

	private final DetailModel detailModel;
	
	private final ExplorerModel explorerModel;
	
	private final PropertyPolling propertyPolling;
	
	private final JSplitPane split;
	/**
	 * Constructor. Create the main Explorer Component.
	 * 
	 * @param explorerModel The Model for this explorer component.
	 * @param menuBar The Main Menu Bar.
	 * @param propertyPolling The property polling.
	 */	
	public ExplorerComponent(ExplorerModel explorerModel, 
			PropertyPolling propertyPolling) {
		this.propertyPolling = propertyPolling;
		this.explorerModel = explorerModel;
		
		// create the only detail model. At the moment this component
		// can only show the detail on one node at a time but the
		// model is written so that this could change.
		detailModel = new DetailModel();
		DetailView detailView = new DetailView(detailModel);
		
		propertyPolling.setPropertyModel(detailModel.getPropertyModel());
		detailModel.addPropertyChangeListener(
				propertyPolling);
				
		DetailController detailControl = new DetailController(detailModel, detailView);
		
		JobTreeModel treeModel = new JobTreeModel();
		JobTreeNode rootTreeNode = new JobTreeNode(explorerModel, treeModel);
		treeModel.setRootTreeNode(rootTreeNode);
		
		tree = new ArooaTree(treeModel) {
			private static final long serialVersionUID = 200801150100L;
			
			@Override
			public DragPoint getDragPoint(Object treeNode) {
				JobTreeNode jobTreeNode = (JobTreeNode) treeNode;
				
				ConfigContextSearch search = new ConfigContextSearch();
				return search.dragPointFor(
						jobTreeNode.getExplorerContext());
			}
		};
		
		NodeControl nodeControl = new NodeControl();
		tree.addTreeWillExpandListener(nodeControl);

		tree.setShowsRootHandles(true);
		
		tree.getSelectionModel().setSelectionMode
			(TreeSelectionModel.SINGLE_TREE_SELECTION);

		tree.addTreeSelectionListener(detailControl);
		tree.addMouseListener(new PopupListener());

		tree.setCellRenderer(new JobTreeCellRenderer());
		ToolTipManager.sharedInstance().registerComponent(tree);
		
		tree.setDragEnabled(true);		
		tree.setDropMode(DropMode.ON_OR_INSERT);
				
		ArooaTransferHandler transferHandler = new ArooaTransferHandler();
		transferHandler.addTransferEventListener(new TransferEventListener() {			
			public void transferException(TransferEvent event, String message, Exception exception) {
				String text = message + "\n" + "Cause: " + exception.getMessage();
				JOptionPane.showMessageDialog(
						tree, 
						text,
						"Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});

		tree.setTransferHandler(transferHandler);

		// Order is important here otherwise the child is show without firing
		// the WillExpand listener - Don't know why.
		rootTreeNode.setVisible(true);
		
		setLayout(new BorderLayout());
		treeScroll = new JScrollPane();
//		treeScroll.setPreferredSize(new Dimension(200, 350));
		
		treeScroll.setViewportView(tree);

		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			treeScroll, detailView);
		
		add(split);
	}
	
	/**
	 * 
	 * @param monitorMeuBar
	 */
	public void bindTo(MonitorMenuBar monitorMenuBar) {
		if (this.menuBar != null) {
			throw new IllegalStateException("MonitorMenuBar already bound.");
		}
		
		this.menuBar = monitorMenuBar;
		
		ExplorerJobActions jobActions = new ExplorerJobActions(
				explorerModel.getExplorerActions());
		
		menuBar.setSession(jobActions, detailModel);
		
		jobActions.addKeyStrokes(this);
	}

	/**
	 * Clear up the component.
	 *
	 */
	public void destroy() {
		detailModel.removePropertyChangeListener(
				propertyPolling);
	}
	
	public void balance() {
		split.setDividerLocation((int) (0.33 * split.getPreferredSize().getWidth()));
	}
	
	/**
	 * Listen to mouse events to trigger the popup.
	 *
	 */
	class PopupListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}
		public void mouseClicked(MouseEvent e) {
			maybeShowPopup(e);
		}
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}
		
		private void maybeShowPopup(MouseEvent e) {
			if (!e.isPopupTrigger()) {
				return;
			}
			TreePath path = tree.getPathForLocation(e.getX(), e.getY());
			tree.setSelectionPath(path);
			JPopupMenu menu = menuBar.getPopupMenu();
			if (menu != null) {
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	
	public JTree getTree() {
		return tree;
	}
}
