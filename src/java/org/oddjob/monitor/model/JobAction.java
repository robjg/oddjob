/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.oddjob.monitor.actions.ExplorerAction;
import org.oddjob.monitor.context.ExplorerContext;

/**
 * The interface for an action which can be performed
 * on a selected job node. This interface defines 
 * a view independent sequence for the action.
 * 
 */
abstract public class JobAction implements ExplorerAction {

	private final PropertyChangeSupport propertySupport = 
		new PropertyChangeSupport(this);
	
	private boolean enabled = true;
	
	private boolean visible = true;
	
	private boolean prepared = false;
	
	private ExplorerContext explorerContext;
	
	/**
	 * Is this action currently enabled?
	 * 
	 * @return true if this action is enabled, false if it isn't.
	 */
	final public boolean isEnabled() {
		return enabled;
	}
	
	protected void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}
		
		PropertyChangeEvent event = new PropertyChangeEvent(
				this, ENABLED_PROPERTY, this.enabled, enabled);
		
		this.enabled = enabled;
		
		propertySupport.firePropertyChange(event);
	}
	
	final public boolean isVisible() {
		return visible;
	}
	
	protected void setVisible(boolean visible) {
		if (this.visible == visible) {
			return;
		}
		
		PropertyChangeEvent event = new PropertyChangeEvent(
				this, VISIBLE_PROPERTY, this.visible, visible);
		
		this.visible = visible;
		
		propertySupport.firePropertyChange(event);
	}
	
	public void addPropertyChangeListener(
			PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(
				listener);
	}
	
	public void removePropertyChangeListener(
			PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(
				listener);
	}
	
	@Override
	public final void setSelectedContext(ExplorerContext explorerContext) {
		if (this.explorerContext != null) {			
			if (prepared) {
				doFree(this.explorerContext);
			}
			this.explorerContext = null;
		}
		
		if (explorerContext == null) {
			setVisible(false);
			setEnabled(false);
		}
		else {
			setVisible(true);
			setEnabled(true);
		}
		
		this.prepared = false;
		this.explorerContext = explorerContext;
	}
	
	@Override
	public final void prepare() {
		if (explorerContext == null) {
			throw new NullPointerException("This shouldn't be possible.");
		}
		doPrepare(explorerContext);
		prepared = true;
	}
		
	protected ExplorerContext getExplorerContext() {
		return explorerContext;
	}

	protected boolean isPrepared() {
		return prepared;
	}
	
	abstract protected void doAction() throws Exception;
	
	protected final boolean checkPrepare() {
		if (!prepared) {
			doPrepare(explorerContext);
			prepared = true;
		}
		return enabled;
	}
	
	@Override
	public final void action() throws Exception {
		if (checkPrepare()) {
			doAction();
		}
	}	
	
	protected void doPrepare(ExplorerContext explorerContext) {
		
	}
	
	protected void doFree(ExplorerContext explorerContext) {
		
	}
	
}
