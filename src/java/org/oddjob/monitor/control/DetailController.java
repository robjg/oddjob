package org.oddjob.monitor.control;

import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.oddjob.monitor.model.DetailModel;
import org.oddjob.monitor.model.JobTreeNode;
import org.oddjob.monitor.view.DetailView;

/**
 * Controller for the detail pane of the monitor. This listens
 * tree selection change event in the view and updates the detail
 * model so correct state, log, properties are displayed.
 * 
 * @author Rob Gordon 
 */

public class DetailController implements TreeSelectionListener {

	/** The model. */
	private final DetailModel detailModel;
	
	/** The view. */
	private final DetailView detailView;
	
	/** The current/last node */
	private JobTreeNode currentNode;
	
	/**
	 * Constructor
	 */
	public DetailController(DetailModel detailModel, DetailView detailView) {		
		this.detailModel = detailModel;
		this.detailView = detailView;
		
		// create detailed view
		detailView.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (currentNode != null) {
					DetailController.this.detailModel.setTabSelected(
							DetailController.this.detailView.getSelectedIndex()); 
				}
			}
		});
	}

	/*
	 *  (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	public void valueChanged(TreeSelectionEvent event) {
		JTree tree = (JTree)event.getSource();
		currentNode = (JobTreeNode)tree.getLastSelectedPathComponent();
		
		if (currentNode == null) {
			detailModel.setSelectedContext(null);
		}
		else {
			detailModel.setSelectedContext(
					currentNode.getExplorerContext());
		}
	}
	
}
