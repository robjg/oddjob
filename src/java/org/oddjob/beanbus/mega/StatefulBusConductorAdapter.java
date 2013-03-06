package org.oddjob.beanbus.mega;

import org.apache.log4j.Logger;
import org.oddjob.FailedToStopException;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.beanbus.AbstractBusConductor;
import org.oddjob.beanbus.BusConductor;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusPhase;
import org.oddjob.state.State;
import org.oddjob.state.StateCondition;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

/**
 * Adapts a {@link Stateful} into a {@Link BusConductor}.
 * 
 * @author rob
 *
 */
public class StatefulBusConductorAdapter extends AbstractBusConductor
implements BusConductor {

	private static final Logger logger = 
			Logger.getLogger(StatefulBusConductorAdapter.class);
	
	private final Stateful stateful; 

	boolean started;
	
	private final StateListener stateListener =
			new StateListener() {
				
				@Override
				public void jobStateChange(StateEvent event) {
					State state = event.getState();
					
					if (state.isStoppable() && !started) {
						
						try {
							fireBusStarting();
							started = true;
						} catch (BusCrashException e) {
							fireBusCrashed(BusPhase.BUS_STARTING, e);
						}
						try {
							fireTripBeginning();
						} catch (BusCrashException e) {
							fireBusCrashed(BusPhase.TRIP_BEGINNING, e);
						}
					}
					
					// javacc via ant wont resolve FINISHED if it's in-lined
					// No idea why!!!!
					StateCondition finished = StateConditions.FINISHED;
					if (finished.test(state) && started) {
						
						if (state.isException()) {
							fireBusCrashed(BusPhase.BUS_RUNNING, 
									new BusCrashException(
									"Stateful Conductor Exception", 
									event.getException()));
						}
						else {
							try {
								fireTripEnding();
							} catch (BusCrashException e) {
								fireBusCrashed(BusPhase.TRIP_ENDING, e);
							}
							try {
								fireBusStopping();
							} catch (BusCrashException e) {
								fireBusCrashed(BusPhase.BUS_STOPPING, e);
							}
							
						}
						started = false;
					}
				}
			};
	
	
	public StatefulBusConductorAdapter(Stateful stateful) {
		this.stateful = stateful;
		stateful.addStateListener(stateListener);
	}
		
	@Override
	public void cleanBus() throws BusCrashException {
		
		fireTripEnding();
		
		fireTripBeginning();
	}
	
	@Override
	public void requestBusStop() {
		
		fireBusStopRequested(started);
		
		if (stateful instanceof Stoppable) {
			try {
				((Stoppable) stateful).stop();
			} catch (FailedToStopException e) {
				logger.error("[" + stateful + "] Failed to stop.", e);
			}
		}
		else {
			logger.info("[" + stateful + "] is not stoppable, " +
					"will need to wait for it to stop.");
		}
	}

	/**
	 * Must be called to remove listener.
	 */
	public void close() {
		stateful.removeStateListener(stateListener);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " for [" + stateful + "]";
	}
}
