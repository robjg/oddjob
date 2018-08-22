package org.oddjob.monitor.view;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JTabbedPane;

import org.oddjob.monitor.model.DetailModel;

/**
 * The detail view component of the monitor.
 * 
 * @author Rob Gordon 
 */

public class DetailView extends JTabbedPane implements Observer {
	private static final long serialVersionUID = 0;
	
	/** State view */
	private final StatePanel statePanel;

	/** Consol panel */
	private final LogTextPanel consolePanel;

	/** Log panel */
	private final LogTextPanel logPanel;

	/** Property view */
	private PropertyPanel propertyPanel;

	/**
	 * Constructor. Create the view and add the three pannels.
	 *
	 */

	public DetailView(DetailModel model) {
		
		// state
		statePanel = new StatePanel();
		model.getStateModel().addObserver(statePanel);

		this.consolePanel = new LogTextPanel(model.getConsoleModel());
		this.logPanel = new LogTextPanel(model.getLogModel());
		
		propertyPanel = new PropertyPanel(
				model.getPropertyModel());
		
//		setPreferredSize(new Dimension(400, 350));

		add("State", statePanel);
		add("Console", consolePanel);		
		add("Log", logPanel);		
		add("Properties", propertyPanel);		
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
	}
}
