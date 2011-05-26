package org.oddjob.monitor.model;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import org.oddjob.Iconic;
import org.oddjob.Structural;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.IconTip;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

/**
 * This class encapsulates the model of a job to be
 * used in the monitor.
 * 
 * @author Rob Gordon 
 */
public class JobTreeNode 
		implements TreeNode {

	/** For list of children */
	private final Vector<JobTreeNode> nodeList = 
		new Vector<JobTreeNode>();

	/** Parent node */
	final private JobTreeNode parent;

	/** Save the JobTreeModel. */
	final private JobTreeModel model;

	/** Save icon information */
	private final OurIconListener iconListener = new OurIconListener();
	private volatile IconTip iconTip = IconHelper.nullIcon;

	/** The job this is modelling. */
	final private Object component;
	
	final private ExplorerContext explorerContext;
	
	private String nodeName;
	
	private boolean visible;
	
	private boolean listening;
	
	private final StructuralListener structuralListner = 
		new StructuralListener() {
		
		/*
		 *  (non-Javadoc)
		 * @see org.oddjob.structural.StructuralListener#childAdded(org.oddjob.structural.StructuralEvent)
		 */
		public void childAdded(StructuralEvent e) {

			int index = e.getIndex();
			JobTreeNode child = new JobTreeNode(JobTreeNode.this, e.getChild());

			// If this node is visible, then this must be the result of a
			// external insert (paste), so our child will be visible to.
			if (visible) {
				child.setVisible(true);
			}
			
			synchronized (nodeList) {
				nodeList.insertElementAt(child, index);
			}

			model.fireTreeNodesInserted(JobTreeNode.this, child, index);
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.oddjob.structural.StructuralListener#childRemoved(org.oddjob.structural.StructuralEvent)
		 */
		public void childRemoved(StructuralEvent e) {
			
			int index = e.getIndex();
			JobTreeNode child = null;
			synchronized (nodeList) {
				child = (JobTreeNode)nodeList.elementAt(index);
				nodeList.removeElementAt(index);
			}
			child.destroy();

			model.fireTreeNodesRemoved(JobTreeNode.this, child, index);
		}

	};
	
	/**
	 * Constructor for the root node.
	 * 
	 * @param explorerModel The ExplorerModel.
	 * @param model The JobTreeModel.
	 */
	public JobTreeNode(ExplorerModel explorerModel, JobTreeModel model) {
		this.parent = null;
		this.model = model;
		this.component = explorerModel.getOddjob(); 		
		this.explorerContext = new ExplorerContextImpl(explorerModel);
	}
	
	/**
	 * Constructor for child nodes.
	 * 
	 * @param parent The parent node.
	 * @param node The structure node this is modelling.
	 */
	public JobTreeNode(JobTreeNode parent, Object node) {
		if (parent == null) {
			throw new NullPointerException("Parent must not be null!");
		}
		this.parent = parent;
		this.model = parent.model;
		this.component = node;
		this.explorerContext = parent.explorerContext.addChild(node); 
	}
	
	public void setVisible(boolean visible) {
		if (this.visible == visible) {
			return;
		}
		if (visible) {
			if (!listening && component instanceof Structural) {
				((Structural)component).addStructuralListener(structuralListner);
				listening = true;
			}			
		    iconListener.listen();
		}
		else {
		    iconListener.dont();
			
		}
		this.visible = visible;
	}

	public boolean isVisible() {
		return visible;
	}
	
	void setIcon(IconTip iconTip) {
	    synchronized (this) {
	        this.iconTip = iconTip;
	    }
		model.fireTreeNodesChanged(this);
	}
	
	public Object getComponent() {
		return component;
	}
	
	// TreeNode methods

	public Enumeration<JobTreeNode> children() {
		return nodeList.elements();
	}

	public boolean getAllowsChildren() {
		return true;
	}
		
	public TreeNode getChildAt(int index) {
		return (JobTreeNode)nodeList.elementAt(index);
	}

	public int getChildCount() {
		return nodeList.size();
	}

	public boolean isLeaf() {
		return nodeList.size() == 0 ? true : false;		
	}

	public int getIndex(TreeNode child) {

		return nodeList.indexOf(child);		
	}

	public TreeNode getParent() {
		
		return parent;
	}

	public String toString() {
		
		if (nodeName == null) {
			nodeName = component.toString();
		}
		return nodeName;
	}

	synchronized public IconTip getIcon() {
		return iconTip;	
	}	

	public JobTreeNode[] getChildren() {
		synchronized (nodeList) {
			return (JobTreeNode[]) nodeList.toArray(new JobTreeNode[0]);
		}
	}
		
	public void destroy() {
		while (nodeList.size() > 0) {			
			int index= nodeList.size() - 1;
			JobTreeNode child = (JobTreeNode)nodeList.remove(index);
			child.destroy();
			model.fireTreeNodesRemoved(this, child, index);
		}
	
		if (component instanceof Structural) {
			((Structural)component).removeStructuralListener(structuralListner);	
		}
		
		iconListener.dont();
	}

	class OurIconListener implements IconListener {
		private boolean listening;
		
		private final Map<String, IconTip> icons = 
			new HashMap<String, IconTip>();
		
		void listen() {
			if (listening) {
				return;
			}
			if (component instanceof Iconic) {
				((Iconic)component).addIconListener(this);
			}
			listening = true;
		}

		public void iconEvent(IconEvent event) {
			String iconId = event.getIconId();
			IconTip it = (IconTip) icons.get(iconId);
			if (it == null) {
				it = ((Iconic)component).iconForId(iconId);
				if (it == null) {
					throw new NullPointerException("No icon for " + iconId);
				}
				icons.put(iconId, it);
			}
			setIcon(it);
		}
		
		public void dont() {
			if (component instanceof Iconic) {
			    ((Iconic)component).removeIconListener(this);
			}
			listening = false;
		}
	}
	
	/**
	 * @return Returns the context.
	 */
	public ExplorerContext getExplorerContext() {
		return explorerContext;
	}
	
}
