/*
 * Copyright © 2004, Rob Gordon.
 */
package org.oddjob.monitor.control;

import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.ExpandVetoException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.monitor.model.JobTreeNode;

/**
 * Listens for tree expansion events to lazily create nodes, add listeners
 * only when they are required, and remove listeners when they are not.
 * The intention is to put as little strain on the network as possible.
 * 
 * @author Rob Gordon.
 */
public class NodeControl implements TreeWillExpandListener {
	private static final Logger logger = LoggerFactory.getLogger(NodeControl.class);
	
    public void treeWillCollapse(TreeExpansionEvent event)
    		throws ExpandVetoException {
    	
		JobTreeNode node = (JobTreeNode)event.getPath().getLastPathComponent();
		JobTreeNode[] children = node.getChildren();
		for (int i = 0; i < children.length; ++i) {
			logger.debug("Tree Node [" + children[i].getComponent() 
					+ "] no longer visible.");
			children[i].setVisible(false);			
		}
	}

    public void treeWillExpand(TreeExpansionEvent event)
    		throws ExpandVetoException {
		JobTreeNode node = (JobTreeNode)event.getPath().getLastPathComponent();
		JobTreeNode[] children = node.getChildren();
		for (int i = 0; i < children.length; ++i) {
			logger.debug("Tree Node [" + children[i].getComponent() 
					+ "] now visible.");
			children[i].setVisible(true);			
		}
	}
}
