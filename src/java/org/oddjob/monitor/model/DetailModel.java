/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Observable;

import org.oddjob.Stateful;
import org.oddjob.framework.PropertyChangeNotifier;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogLevel;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

/**
 * Model for controlling the detail views.
 * 
 * @author rob
 *
 */
public class DetailModel implements PropertyChangeNotifier {

	public static final String SELECTED_CONTEXT_PROPERTY = "selectedContext";
	public static final String TAB_SELECTED_PROPERTY = "tabSelected";
	
	public static final int STATE_TAB = 0;
	public static final int CONSOLE_TAB = 1;
	public static final int LOG_TAB = 2;
	public static final int PROPERTIES_TAB = 3;

	/** Tab selected */
	private int tabSelected = STATE_TAB;

	/** The selected Job */
	private ExplorerContext selectedContext;
	
	/** Console model */
	private final LogModel consoleModel = new LogModel();
	
	/** Log model */
	private final LogModel logModel = new LogModel();

	/** Property model */
	private PropertyModel propertyModel = new PropertyModel();

	/** State model */
	private final StateModel stateModel = new StateModel();

	/** State control */
	private final StateListener stateListener = new StateListener() {		
		public void jobStateChange(StateEvent event) {
			stateModel.change(event);
		}
	};

	
	
	
	private final PropertyChangeSupport propertySupport =
		new PropertyChangeSupport(this);
	
	/**
	 * Get the model for the Console panel.
	 * 
	 * @return The LogModel for the console.
	 */
	public LogModel getConsoleModel() {
		return consoleModel;
	}

	/**
	 * Get the model for the Log panel.
	 * 
	 * @return The LogModel for the log;
	 */
	public LogModel getLogModel() {
		return logModel;
	}

	/**
	 * Get the model for the Property panel.
	 * 
	 * @return The PropertyModel.
	 */
	public PropertyModel getPropertyModel() {
		return propertyModel;
	}
	
	/**
	 * Set currently selected tab.
	 * 
	 * @param tabSelected The tab number.
	 */
	public void setTabSelected(int tabSelected) {

		if (this.tabSelected == tabSelected) {
			return;
		}
		
		if (selectedContext != null) {
			freeTab(this.tabSelected);
		}
		
		PropertyChangeEvent event = new PropertyChangeEvent(
				this, TAB_SELECTED_PROPERTY, 
				new Integer(this.tabSelected), new Integer(tabSelected));
		
		this.tabSelected = tabSelected;
		
		if (selectedContext != null) {
			engageTab(tabSelected);
		}
		
		propertySupport.firePropertyChange(event);
	}
	
	/**
	 * Get the selected detail tab.
	 * 
	 * @return The selected tab.
	 */
	public int getTabSelected() {
		return tabSelected;
	}
	
	private void freeTab(int index) {
		
		Object selectedJob = selectedContext.getThisComponent();
		
		switch (index) {
		case STATE_TAB:
			// state
			stateModel.clear();
			if (selectedJob instanceof Stateful) {
				((Stateful) selectedJob).removeStateListener(stateListener);
			}
			break;
		case CONSOLE_TAB:
			// console
			ConsoleArchiver consoleArchiver = 
				(ConsoleArchiver) selectedContext.getValue(LogContextInialiser.CONSOLE_ARCHIVER);
			consoleArchiver.removeConsoleListener(consoleModel, selectedJob);
			consoleModel.setClear();
			break;
		case LOG_TAB:
			// logger
			LogArchiver logArchiver = 
				(LogArchiver) selectedContext.getValue(LogContextInialiser.LOG_ARCHIVER);
			logArchiver.removeLogListener(logModel, selectedJob);
			logModel.setClear();
			break;
		case PROPERTIES_TAB:
			break;
		default:
			throw new IllegalArgumentException("Index " + index + " > 3.");
		}
	}
	
	private void engageTab(int index) {
		
		Object selectedJob = selectedContext.getThisComponent();
		
		switch (index) {
		case STATE_TAB:
			// stateful
			if (selectedJob instanceof Stateful) {	
				((Stateful) selectedJob).addStateListener(stateListener);
			}
			break;
		case CONSOLE_TAB:
			ConsoleArchiver consoleArchiver = 
				(ConsoleArchiver) selectedContext.getValue(LogContextInialiser.CONSOLE_ARCHIVER);
			consoleArchiver.addConsoleListener(consoleModel, selectedJob, -1, 1000);
			break;
		case LOG_TAB:
			// add logging pane
			LogArchiver logArchiver = 
				(LogArchiver) selectedContext.getValue(LogContextInialiser.LOG_ARCHIVER);
			logArchiver.addLogListener(logModel, selectedJob, LogLevel.DEBUG, -1, 1000);
			break;
		case PROPERTIES_TAB:
			break;
		default:
			throw new IllegalArgumentException("Index " + index + " > 3.");
		}
	}
	
	public void setSelectedContext(ExplorerContext newContext) {
		
		if (selectedContext == newContext) {
			return;
		}
		
		if (selectedContext != null) {
			freeTab(tabSelected);
		}
		
		PropertyChangeEvent event = new PropertyChangeEvent(
				this, SELECTED_CONTEXT_PROPERTY, selectedContext, newContext);
		
		selectedContext = newContext;
		
		if (selectedContext != null) {
			engageTab(tabSelected);
		}
		
		propertySupport.firePropertyChange(event);
	}
	
	/**
	 * Get the selected job.
	 * 
	 * @return The selected job or null if none is. 
	 */
	public Object getSelectedJob() {
		if (selectedContext == null) {
			return null;
		}
		return selectedContext.getThisComponent();
	}
	
	/**
	 * Get the state model.
	 * 
	 * @return The StateModel.
	 */
	public Observable getStateModel() {
		return stateModel;
	}
	
	public void addPropertyChangeListener(
			PropertyChangeListener listener) {
		propertySupport.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(
			PropertyChangeListener listener) {
		propertySupport.removePropertyChangeListener(listener);
	}
	
}
