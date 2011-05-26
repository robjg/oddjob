package org.oddjob.monitor.actions;

import junit.framework.TestCase;

import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.monitor.action.AddJobAction;
import org.oddjob.monitor.action.DesignInsideAction;
import org.oddjob.monitor.action.DesignerAction;
import org.oddjob.monitor.action.ExecuteAction;
import org.oddjob.monitor.action.HardResetAction;
import org.oddjob.monitor.action.LoadAction;
import org.oddjob.monitor.action.SetPropertyAction;
import org.oddjob.monitor.action.SoftResetAction;
import org.oddjob.monitor.action.StopAction;
import org.oddjob.monitor.action.UnloadAction;

public class ResourceActionProviderTest extends TestCase {

	public void testActions() throws ArooaParseException {
		
		ExplorerAction[] results = new ResourceActionProvider(
				new OddjobSessionFactory(
						).createSession()).getExplorerActions();

		assertEquals(10, results.length);
		
		assertEquals(LoadAction.class, results[0].getClass());
		assertEquals(UnloadAction.class, results[1].getClass());
		assertEquals(ExecuteAction.class, results[2].getClass());
		assertEquals(SoftResetAction.class, results[3].getClass());
		assertEquals(HardResetAction.class, results[4].getClass());
		assertEquals(StopAction.class, results[5].getClass());
		assertEquals(SetPropertyAction.class, results[6].getClass());
		assertEquals(DesignerAction.class, results[7].getClass());
		assertEquals(DesignInsideAction.class, results[8].getClass());
		assertEquals(AddJobAction.class, results[9].getClass());
	}
	
}
