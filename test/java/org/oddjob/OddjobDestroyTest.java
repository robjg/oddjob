package org.oddjob;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaException;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.runtime.RuntimeEvent;
import org.oddjob.arooa.runtime.RuntimeListenerAdapter;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.framework.SimpleJob;
import org.oddjob.state.IsAnyState;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.state.ParentStateHandler;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.tools.StateSteps;

public class OddjobDestroyTest extends TestCase {

	private static final Logger logger = Logger.getLogger(
			OddjobDestroyTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("----------------------"  + getName() + 
				"--------------------");
	}
	
	public void testDestroyWhileChildRunning() throws ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <wait id='wait'/>" +
				" </job>" +
				"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		StateSteps oddjobStates = new StateSteps(oddjob);
		oddjobStates.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.DESTROYED);
		
		oddjob.load();
		
		Stateful wait = new OddjobLookup(oddjob).lookup("wait", Stateful.class);
		
		StateSteps waitStates = new StateSteps(wait);
		waitStates.startCheck(JobState.READY, JobState.EXECUTING);
		
		Thread t = new Thread(oddjob);
		
		t.start();
		
		waitStates.checkWait();
		
		waitStates.startCheck(JobState.EXECUTING, JobState.COMPLETE, 
				JobState.DESTROYED);
		
		oddjob.destroy();
		
		waitStates.checkNow();
		oddjobStates.checkNow();
	}
	
    public static class ReluctantToDie extends SimpleJob {
    	boolean die;
    	@Override
    	protected int execute() throws Throwable {
    		return 0;
    	}
    	@Override
    	public void onDestroy() {
    		super.onDestroy();
    		if (!die) {
    			die = true;
    			throw new IllegalStateException("I'm not ready to die.");
    		}
    	}
    }
    
    public void testFailedDestroy() {
    	
    	String xml = 
    		"<oddjob>" +
    		" <job>" +
    		"  <bean class='" + ReluctantToDie.class.getName() + "'/>" +
    		" </job>" +
    		"</oddjob>";
    	
    	Oddjob test = new Oddjob();
		test.setConfiguration(new XMLConfiguration("XML", xml));
    	
    	test.run();
    	
    	try {
    		test.destroy();
    		fail("Exception expected.");
    	} catch (IllegalStateException e) {
    		// expected.
    	}
    	
    	assertEquals(ParentState.COMPLETE, 
    			test.lastStateEvent().getState());
    	
    	test.destroy();
    	
    	assertEquals(ParentState.DESTROYED, 
    			test.lastStateEvent().getState());
    }
    
	public static class ActiveJob 
	implements Stateful, Runnable, ArooaContextAware {
		
		ParentStateHandler stateHandler = new ParentStateHandler(this);
		
		@Override
		public void run() {
			stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
				@Override
				public void run() {
					stateHandler.setState(ParentState.ACTIVE);
					stateHandler.fireEvent();
				}
			});
		}
		
		@Override
		public void addStateListener(StateListener listener)
				throws JobDestroyedException {
			stateHandler.addStateListener(listener);
		}
		
		@Override
		public void removeStateListener(StateListener listener) {
			stateHandler.removeStateListener(listener);
		}
		
		@Override
		public StateEvent lastStateEvent() {
			return stateHandler.lastStateEvent();
		}
		
		@Override
		@ArooaHidden
		public void setArooaContext(ArooaContext context) {
			
			context.getRuntime().addRuntimeListener(new RuntimeListenerAdapter() {
				@Override
				public void beforeDestroy(RuntimeEvent event) throws ArooaException {
					stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
						@Override
						public void run() {
							stateHandler.setState(ParentState.COMPLETE);
							stateHandler.fireEvent();
						}
					});
				}
				
				@Override
				public void afterDestroy(RuntimeEvent event) throws ArooaException {
					stateHandler.waitToWhen(new IsAnyState(), new Runnable() {
						@Override
						public void run() {
							stateHandler.setState(ParentState.DESTROYED);
							stateHandler.fireEvent();
						}
					});
				}
			});
			
		}
	}
	
	public void testDestroyWhileAsyncJobActive() throws ArooaPropertyException, ArooaConversionException, InterruptedException {
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <bean class='" + ActiveJob.class.getName() + "' id='active'/>" +
				" </job>" +
				"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		StateSteps oddjobStates = new StateSteps(oddjob);
		oddjobStates.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.ACTIVE, ParentState.DESTROYED);
		
		oddjob.load();
		
		Stateful active = new OddjobLookup(oddjob).lookup("active", Stateful.class);
		
		StateSteps activeStates = new StateSteps(active);
		activeStates.startCheck(ParentState.READY, ParentState.ACTIVE);
		
		oddjob.run();
		
		activeStates.checkNow();
		
		activeStates.startCheck(ParentState.ACTIVE, ParentState.COMPLETE, 
				ParentState.DESTROYED);
		
		oddjob.destroy();
		
		activeStates.checkNow();
		oddjobStates.checkNow();
	}
	
}
