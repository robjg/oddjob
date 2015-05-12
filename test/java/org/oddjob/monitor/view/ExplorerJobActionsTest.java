package org.oddjob.monitor.view;

import java.util.ArrayList;
import java.util.List;

import javax.swing.Action;

import junit.framework.TestCase;

import org.oddjob.arooa.design.actions.ActionMenu;
import org.oddjob.arooa.design.actions.ArooaAction;
import org.oddjob.arooa.design.actions.MockActionRegistry;
import org.oddjob.monitor.action.ExecuteAction;
import org.oddjob.monitor.action.StopAction;
import org.oddjob.monitor.actions.ExplorerAction;

public class ExplorerJobActionsTest extends TestCase {

	class OurRegistry extends MockActionRegistry {

		ActionMenu mainMenu;
		
		List<String> names = new ArrayList<String>();
		List<String> commands = new ArrayList<String>();
		
		
		@Override
		public void addMainMenu(ActionMenu menu) {
			this.mainMenu = menu;
		}
		
		@Override
		public void addMenuItem(String menuId, String group, ArooaAction action) {
			names.add((String) action.getValue(Action.NAME));
			commands.add((String) action.getValue(Action.ACTION_COMMAND_KEY));
		}
		
		@Override
		public void addContextMenuItem(String group, ArooaAction action) {
		}
	}
	
	public void testActionName() {
		
		ExplorerAction[] actions = new ExplorerAction[] {
			new ExecuteAction(), new StopAction()
		};
		
		ExplorerJobActions test = new ExplorerJobActions(actions);
		
		OurRegistry actionRegistry = new OurRegistry();
		
		test.contributeTo(actionRegistry);
	
		assertEquals("Job", actionRegistry.mainMenu.getId());
		
		assertEquals("Start", actionRegistry.names.get(0));
		assertEquals("Stop", actionRegistry.names.get(1));
	}
	
}
