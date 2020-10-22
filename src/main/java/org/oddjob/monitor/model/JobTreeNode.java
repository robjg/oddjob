package org.oddjob.monitor.model;

import org.oddjob.Iconic;
import org.oddjob.Structural;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.images.ImageData;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

/**
 * This class encapsulates the model of a job to be
 * used in the monitor.
 * 
 * @author Rob Gordon 
 */
public class JobTreeNode 
		implements TreeNode {

	private static final Logger logger = LoggerFactory.getLogger(JobTreeNode.class);

	private final ConcurrentMap<String, ImageIcon> icons = new ConcurrentHashMap<>();

	/** How to dispatch tree model changes. */
	private final Executor executor;
	
	/** For list of children from the AWT Event Thread perspective. */
	private final Vector<JobTreeNode> nodeList =
			new Vector<>();

	/** From the Job perspective. */	
	private final Vector<JobTreeNode> currentList =
			new Vector<>();

	/** Parent node */
	final private JobTreeNode parent;

	/** Save the JobTreeModel. */
	final private JobTreeModel model;

	/** Save icon information */
	private final OurIconListener iconListener = new OurIconListener();

	private volatile ImageIcon iconTip = icons.computeIfAbsent(IconHelper.NULL,
			s -> IconHelper.imageIconFrom(IconHelper.nullIcon));

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
		public synchronized void childAdded(final StructuralEvent e) {

			final int index = e.getIndex();
			Object childJob = e.getChild();

			final JobTreeNode childNode = new JobTreeNode(JobTreeNode.this, childJob);

			logger.debug("Received add event for [" + childNode.getComponent() + "]");
			
			// If this node is visible, then this must be the result of a
			// external insert (paste), so our child will be visible to.
			if (visible) {
				childNode.setVisible(true);
			}
			
			currentList.add(index, childNode);
			
			executor.execute(() -> {
				logger.debug("Adding node for [" + childNode.getComponent() + "]");

				nodeList.add(index, childNode);

				model.fireTreeNodesInserted(JobTreeNode.this, childNode, index);
			});
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.oddjob.structural.StructuralListener#childRemoved(org.oddjob.structural.StructuralEvent)
		 */
		public synchronized void childRemoved(final StructuralEvent e) {
			
			
			final int index = e.getIndex();
			
			final JobTreeNode child = currentList.remove(index);

			logger.debug("Received remove event for [" + child.getComponent() + "]");
			
			child.destroy();
			
			executor.execute(() -> {

				logger.debug("Removing node for [" + child.getComponent() + "]");

				JobTreeNode child1 = nodeList.remove(index);

				model.fireTreeNodesRemoved(JobTreeNode.this, child1, index);

			});
		}

	};
	
	/**
	 * Constructor for the root node.
	 * 
	 * @param explorerModel The ExplorerModel.
	 * @param model The JobTreeModel.
	 */
	public JobTreeNode(ExplorerModel explorerModel, JobTreeModel model) {
		this(explorerModel, model, new EventThreadLaterExecutor(),
				ExplorerContextImpl.FACTORY);
	}
	
	/**
	 * Constructor for testing so we can change the {@link Executor} and
	 * {@link ExplorerContextFactory}.
	 * 
	 * @param explorerModel
	 * @param model
	 * @param executor
	 * @param contextFactory
	 */
	public JobTreeNode(ExplorerModel explorerModel, JobTreeModel model, 
			Executor executor, ExplorerContextFactory contextFactory) {
		this.parent = null;
		this.model = model;
		this.component = explorerModel.getOddjob(); 		
		this.explorerContext = contextFactory.createFrom(explorerModel);
		this.executor = executor;
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
		this.executor = parent.executor;
	}
	
	/**
	 * Called when a node is made visible. This is to reduce the
	 * amount of listeners added to the job tree.
	 * 
	 * @param visible True if visible.
	 */
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
	
	/**
	 * Called from Icon Listener.
	 * 
	 * @param icon
	 */
	void setIcon(ImageIcon icon) {
	    synchronized (this) {
	        this.iconTip = icon;
	    }
	    executor.execute(() -> model.fireTreeNodesChanged(JobTreeNode.this));
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
		return nodeList.get(index);
	}

	public int getChildCount() {
		return nodeList.size();
	}

	public boolean isLeaf() {
		return nodeList.isEmpty();
	}

	public int getIndex(TreeNode child) {

		if (!( child instanceof JobTreeNode)) {
			throw new IllegalStateException("Should be a " + JobTreeNode.class.getSimpleName());
		}

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

	synchronized public ImageIcon getIcon() {
		return iconTip;	
	}	

	public JobTreeNode[] getChildren() {
		synchronized (nodeList) {
			return nodeList.toArray(new JobTreeNode[0]);
		}
	}
		
	/**
	 * Destroy the node. Remove listeners and destroy any remaining child
	 * nodes. Child node will remain in situations where a child is removed 
	 * from it's parent before being destroyed This happens with both the
	 * {@link org.oddjob.jobs.structural.ForEachJob} and the 
	 * {@link org.oddjob.jmx.JMXClientJob} jobs.
	 */
	public void destroy() {
		
		logger.debug("Destroying node for [" + getComponent() + "]");
		
		if (component instanceof Structural) {
			((Structural)component).removeStructuralListener(structuralListner);	
		}
		
		iconListener.dont();
		
		for (int i = currentList.size(); i > 0; --i) {			
			
			final int index = i - 1;
			final JobTreeNode child = currentList.remove(index);
			
			child.destroy();
			
			executor.execute(() -> {
				logger.debug("Removing node for [" + child.getComponent() + "]");

				JobTreeNode child1 = nodeList.remove(index);

				model.fireTreeNodesRemoved(JobTreeNode.this, child1, index);
			});
		}
	}

	class OurIconListener implements IconListener {
		private boolean listening;
		
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
			ImageIcon it = icons.computeIfAbsent(iconId,
					s -> {
				ImageData iconData = ((Iconic)component).iconForId(iconId);
				if (iconData == null) {
					throw new NullPointerException("No icon for " + iconId);
				}
				return IconHelper.imageIconFrom(iconData);
			});
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
