/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import java.util.HashMap;
import java.util.Map;

import org.oddjob.monitor.context.CompositeContextInitialiser;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.util.ThreadManager;

/**
 * Explorer Context. Used to pass useful things down the job hierarchy.
 * <p>
 * A unique context will exist for each node in the hierarchy but where
 * as the model has specific information about the node in the tree - it's
 * children, is it visable etc, the context contains ancillary informaton
 * about the nodes environment. 
 * 
 * @author Rob Gordon
 */
public class ExplorerContextImpl implements ExplorerContext {

	public static final ExplorerContextFactory FACTORY = 
		new ExplorerContextFactory() {
		
		@Override
		public ExplorerContext createFrom(ExplorerModel explorerModel) {
			return new ExplorerContextImpl(explorerModel);
		}
	};
	
	private final ContextInitialiser initialiser;
	
	/** The component who's context this is. */
	private final Object component;
	
	private final Map<String, Object> values = 
		new HashMap<String, Object>();
	
	/** The parent context */
	private final ExplorerContext parent;

	private final ThreadManager threadManager;
	
	/**
	 * Constructor for the top level context.
	 */
	public ExplorerContextImpl(ExplorerModel explorerModel) {
		this.component = explorerModel.getOddjob();
		
		if (component == null) {
			throw new NullPointerException("Component can't be null");
		}
		this.parent = null;
		
		this.threadManager = explorerModel.getThreadManager();
		
		this.initialiser = new CompositeContextInitialiser(
				explorerModel.getContextInitialisers());

		this.initialiser.initialise(this);
	}
	
	/**
	 * Constructor for a child context.
	 * 
	 * @param parent The parent context.
	 */
	private ExplorerContextImpl(Object component, ExplorerContextImpl parent) {
		if (component == null) {
			throw new NullPointerException("Component can't be null");
		}
		if (parent == null) {
			throw new NullPointerException("Parent can't be null");
		}
		this.component = component;
		this.parent = parent;

		this.threadManager = parent.getThreadManager();
		
		this.initialiser = parent.initialiser;

		this.initialiser.initialise(this);
	}
	
	public ExplorerContext addChild(Object child) {
		return new ExplorerContextImpl(child, this);
	}
	
	public Object getThisComponent() {
		return component;
	}
	
	public ThreadManager getThreadManager() {
		return threadManager;
	}

	public ExplorerContext getParent() {
		return parent;
	}
	
	public Object getValue(String key) {
		return values.get(key);
	}
	
	public void setValue(String key, Object value) {
		values.put(key, value);
	}
}
