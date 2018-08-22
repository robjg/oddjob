package org.oddjob.monitor.actions;

import javax.swing.KeyStroke;

import org.oddjob.framework.PropertyChangeNotifier;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.SelectedContextAware;

/**
 * Abstraction for an Action that acts on a Job and is executed
 * in from OddjobExplorer or another user interface.
 * <p>
 * The life cycle for an action is:
 * <ul>
 *  <li>{@link #setSelectedContext(ExplorerContext)} when the job
 *  selection changes.</li>
 *  <li>{@link #prepare()} before a menu is shown.</li>
 *  <li>{@link #action()} to perform the action.</li>
 * </ul>
 * 
 * @author rob
 *
 */
public interface ExplorerAction 
extends PropertyChangeNotifier, SelectedContextAware {

	/** The enabled property name. */
	public static final String ENABLED_PROPERTY = "enabled";
	
	/** The visible property name. */
	public static final String VISIBLE_PROPERTY = "visible";
	
	/** The name of the 'job' menu group. */
	public static final String JOB_GROUP = "job";
	
	/** The name of the 'property' menu group. */
	public static final String PROPERTY_GROUP = "property";
	
	/** The name of the 'design' menu group. */
	public static final String DESIGN_GROUP = "design";
	
	/**
	 * Get the name for the option. This will typically
	 * be used as a menu item name.
	 * 
	 * @return The name for the action.
	 */
	public String getName();
	

	/**
	 * Get the group name. This is which group in the Job
	 * menu to place the action in. 
	 * 
	 * @return A name. Must not be null.
	 */
	public String getGroup();
	

	/**
	 * Get the Mnemonic Key for the action.
	 * 
	 * @return The MnemonicKey. May be null.
	 */
	public Integer getMnemonicKey();
	
	/**
	 * Get the Accelerator key for the action.
	 * 
	 * @return The KeyStroke. May be null.
	 */
	public KeyStroke getAcceleratorKey();
	
	/**
	 * This method will be called when a component
	 * is selected or unselected.
	 * 
	 * @param eContext the ExplorerContext for the current
	 * node. Null when the node is unselected. 
	 */
	public void setSelectedContext(ExplorerContext eContext);

	
	/**
	 * Called to perform the action.
	 * <p>
	 * If this is a {@link FormAction} this method will be called once
	 * the form has been completed. Otherwise this method is called
	 * immediately the menu item or other trigger for the action has
	 * been selected.
	 * 
	 * @throws Exception The exception will be caught and
	 * presented to the user.
	 */
	public void action() throws Exception;
	
	/**
	 * Called when the Job Menu is selected. This provides an action
	 * with the opportunity to work out if it is disabled or not.
	 * <p>
	 * This method was added in addition to 
	 * {@link #setSelectedContext(ExplorerContext)} because prepare can be 
	 * relatively slow if, for instance, it involves ascertaining the state
	 * of a remote job, so this method allows this to be done only when 
	 * the menu is to be shown. 
	 */
	public void prepare();
	
	/**
	 * Is this action currently enabled?
	 * 
	 * @return true if this action is enabled, false if it isn't.
	 */
	public boolean isEnabled();
	
	/**
	 * Is this action currently visible?
	 * 
	 * @return true if this action is visible, false if it isn't.
	 */
	public boolean isVisible();
}
