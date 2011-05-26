/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.control;

import java.util.Map;

import junit.framework.TestCase;

import org.oddjob.Stateful;
import org.oddjob.monitor.model.DetailModel;
import org.oddjob.monitor.model.MockExplorerContext;
import org.oddjob.monitor.model.PropertyModel;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

public class PropertyPollingTest extends TestCase {

	private class OurExplorerContext extends MockExplorerContext {
		@Override
		public Object getThisComponent() {
			return new Comp();
		}
	}

	public static class Comp {
		public String getFruit() {
			return "apples";
		}
	}
	
	/**
	 * Test what happens when the property tab
	 * is selected.
	 *
	 */
	public void testSelected() {
		
		PropertyModel model = new PropertyModel();
		
		PropertyPolling test = new PropertyPolling(this);
		test.setPropertyModel(model);

		DetailModel detailModel = new DetailModel();
		
		detailModel.addPropertyChangeListener(test);
		
		detailModel.setTabSelected(DetailModel.PROPERTIES_TAB);

		OurExplorerContext ec = new OurExplorerContext();
		
		detailModel.setSelectedContext(ec);
		
		test.poll();
		
		String result = (String) model.getProperties().get("fruit");
		assertEquals("apples", result);
	}
	
	/**
	 * Test what happens when the property tab
	 * is unselected.
	 *
	 */
	public void testNotSelected() {
		
		PropertyModel model = new PropertyModel();
		
		PropertyPolling test = new PropertyPolling(this);
		test.setPropertyModel(model);

		DetailModel detailModel = new DetailModel();
		detailModel.addPropertyChangeListener(
				test);
		
		OurExplorerContext ec = new OurExplorerContext();
		
		detailModel.setSelectedContext(ec);
		
		test.poll();
		
		Map<String, String> props = model.getProperties();
		assertEquals(0, props.size());
	}
	
	public class OurStateful implements Stateful {
		
		private JobStateListener listener;
		
		@Override
		public void addJobStateListener(JobStateListener listener) {
			assertNotNull(listener);
			assertNull(this.listener);
			this.listener = listener;
		}
		
		@Override
		public JobStateEvent lastJobStateEvent() {
			throw new RuntimeException("Unexpected.");
		}
		
		@Override
		public void removeJobStateListener(JobStateListener listener) {
			assertNotNull(listener);
			assertSame(this.listener, listener);
			this.listener = null;
		}
	}
	
	private class OurExplorerContext2 extends MockExplorerContext {
		
		OurStateful stateful = new OurStateful();
		
		@Override
		public Object getThisComponent() {
			return stateful;
		}
	}

	public void testSelectedStateful() {
		
		PropertyModel model = new PropertyModel();
		
		PropertyPolling test = new PropertyPolling(this);
		test.setPropertyModel(model);

		DetailModel detailModel = new DetailModel();
		
		detailModel.addPropertyChangeListener(test);
		
		detailModel.setTabSelected(DetailModel.PROPERTIES_TAB);

		OurExplorerContext2 ec = new OurExplorerContext2();
		
		detailModel.setSelectedContext(ec);

		assertNotNull(ec.stateful.listener); 
		
		ec.stateful.listener.jobStateChange(
				new JobStateEvent(ec.stateful, JobState.COMPLETE)); 
		
		detailModel.setSelectedContext(null);
		
		assertNull(ec.stateful.listener); 
		
	}
}
