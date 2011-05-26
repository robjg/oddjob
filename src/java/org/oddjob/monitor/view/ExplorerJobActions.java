package org.oddjob.monitor.view;

import java.awt.event.KeyEvent;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.oddjob.arooa.design.actions.ActionContributor;
import org.oddjob.arooa.design.actions.ActionMenu;
import org.oddjob.arooa.design.actions.ActionRegistry;
import org.oddjob.monitor.actions.ExplorerAction;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.SelectedContextAware;

/**
 * Group the Job Actions for Explorer.
 * 
 * @author Rob Gordon
 */

public class ExplorerJobActions
implements ActionContributor, SelectedContextAware {

	private final JobSwingAction[] swingActions;
	
	private final ExplorerAction[] actions;
	
	/**
	 * Constructor.
	 * 
	 * @param explorer The owning explorer.
	 */
	public ExplorerJobActions(ExplorerAction[] actions) {
		this.actions = actions;
		this.swingActions = new JobSwingAction[actions.length];
		for (int i = 0; i < actions.length; ++i) {
			this.swingActions[i] = new JobSwingAction(actions[i]);
		}		
	}

	/**
	 * Called when the Model changes it's selected
	 * node.
	 * 
	 */
	public void setSelectedContext(ExplorerContext context) {
		
		for (ExplorerAction action: actions) {
			action.setSelectedContext(context);
		}
	}

	@Override
	public void prepare() {
		
		for (ExplorerAction action: actions) {
			action.prepare();
		}
	}
	
	public void contributeTo(ActionRegistry actionRegistry) {
	
		actionRegistry.addMainMenu(
				new ActionMenu(
						MonitorMenuBar.JOB_MENU_ID, "Job", KeyEvent.VK_J));
		
		for (int i =0; i < actions.length; ++i) {
			
			actionRegistry.addMenuItem(MonitorMenuBar.JOB_MENU_ID, 
					actions[i].getGroup(), swingActions[i]);
			actionRegistry.addContextMenuItem(actions[i].getGroup(), 
					swingActions[i]);
		}
	}
	
	public void addKeyStrokes(JComponent component) {

		ActionMap actionMap = component.getActionMap();
		InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		for (int i =0; i < actions.length; ++i) {
			actionMap.put(actions[i].getName(), swingActions[i]);
			KeyStroke keyStroke = actions[i].getAcceleratorKey();
			
			if (keyStroke != null) {
				inputMap.put(keyStroke, actions[i].getName());
			}
		}
	}
}
