package org.oddjob.monitor.view;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.oddjob.monitor.model.JobTreeNode;

/**
 * Display the icon in the job tree.
 * 
 * @author Rob Gordon 
 */

public class JobTreeCellRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 2005010100L;

	/**
	 * Set the icon and tool tip.
	 */

	public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {

		super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);

		JobTreeNode node = (JobTreeNode)value;

		ImageIcon icon = node.getIcon();
		if (icon != null) {
			setIcon(icon);
			setToolTipText(icon.getDescription());			
		}
        return this;
	}

}
