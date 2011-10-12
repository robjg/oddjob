package org.oddjob.monitor.view;

import java.awt.GraphicsEnvironment;

import javax.swing.JMenu;

import junit.framework.TestCase;

import org.oddjob.monitor.action.HardResetAction;
import org.oddjob.monitor.actions.ExplorerAction;
import org.oddjob.monitor.model.DetailModel;

public class MonitorMenuBarTest extends TestCase {

	public void testSetSession() {
		
		if (GraphicsEnvironment.isHeadless()) {
			return;
		}

		MonitorMenuBar test = new MonitorMenuBar();
		
		DetailModel detailModel = new DetailModel();
		
		ExplorerJobActions jobActions = new ExplorerJobActions(
				new ExplorerAction[] {
			new HardResetAction() });
		
		test.setSession(jobActions, detailModel);
		
		assertEquals(3, test.getMenuCount());
		
		JMenu jobMenu = test.getMenu(2);
		
		assertEquals("Job", jobMenu.getActionCommand());
		
		assertEquals(1, jobMenu.getMenuComponentCount());
	}
}
