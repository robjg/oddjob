package org.oddjob.jobs.tasks;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.oddjob.FailedToStopException;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.runtime.PropertyLookup;
import org.oddjob.arooa.standard.StandardPropertyLookup;
import org.oddjob.arooa.utils.ListSetterHelper;
import org.oddjob.framework.SimpleService;
import org.oddjob.input.InputRequest;
import org.oddjob.jobs.job.ResetAction;
import org.oddjob.jobs.job.ResetActions;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * A job that can be parameterised.
 * 
 * @author Rob Gordon
 */
public class TaskExecutionService extends SimpleService
implements TaskExecutor, Structural {
	
	/** Track changes to children an notify listeners. */
	protected final ChildHelper<Object> childHelper =
			new ChildHelper<Object>(this);
	
	private final List<InputRequest> requests =
		new ArrayList<InputRequest>();
		
	private volatile InputRequest[] requestArray;
	
	private volatile Properties properties;
	
	private volatile ResetAction reset;
	
	public InputRequest getRequests(int index) {
		return requests.get(index);
	}

	public void setRequests(int index, InputRequest request) {
		new ListSetterHelper<InputRequest>(requests).set(index, request);
	}

	@Override
	public InputRequest[] getParameterInfo() {
		return requestArray;
	}
	
	@Override
	public long execute(Properties properties) 
	throws TaskException {
		
		if (requestArray == null) {
			throw new TaskException("Task Execution Service not Started.");
		}

		if (properties == null) {
			properties = new Properties();
		}
		
		final ArooaSession session = getArooaSession();
		if (session == null) {
			throw new NullPointerException("No session.");
		}
		
		final Object job = childHelper.getChild();
		if (!(job instanceof Runnable)) {
			throw new TaskException("No Job to Execute the Task.");
		}
		
		if (this.properties != null) {
			throw new TaskException("Task Execution in progress.");
		}
		
		this.properties = properties;
		final PropertyLookup propertyLookup = 
				new StandardPropertyLookup(properties, this.toString());
		session.getPropertyManager().addPropertyOverride(
				propertyLookup);
			
		ResetAction reset = this.reset;
		if (reset == null) {
			reset = ResetActions.AUTO;
		}
		
		reset.doWith(job);
		((Runnable) job).run();
					
		if (job instanceof Stateful) {
			
			((Stateful) job).addStateListener(new StateListener() {
				@Override
				public void jobStateChange(StateEvent event) {
					if (!event.getState().isStoppable()) {
						TaskExecutionService.this.properties = null;
						session.getPropertyManager().removePropertyLookup
							(propertyLookup);
						((Stateful) job).removeStateListener(this);
					}
				}
			});
		}
		else {
			this.properties = null;
			session.getPropertyManager().removePropertyLookup
				(propertyLookup);
		}
		
		return 1L;
	}
	
	@Override
	public Stateful getTaskExecution(long executionId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	protected void onStart() throws Throwable {
		
		requestArray = requests.toArray(
				new InputRequest[requests.size()]);
	}
	
	@Override
	protected void onStop() throws FailedToStopException {
		
		requestArray = null;
		
		Object job = childHelper.getChild();
		if (job != null && job instanceof Stoppable) {
			((Stoppable) job).stop();
		}		
	}
	
	
	/**
	 * Add a listener. The listener will immediately receive add
	 * notifications for all existing children.
	 * 
	 * @param listener The listener.
	 */	
	public void addStructuralListener(StructuralListener listener) {
		stateHandler().assertAlive();
		
		childHelper.addStructuralListener(listener);
	}
	
	/**
	 * Remove a listener.
	 * 
	 * @param listener The listener.
	 */
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
	}	
			
	/**
	 * @oddjob.property job
	 * @oddjob.description The job to pass resets on to.
	 * @oddjob.required Yes.
	 */
	@ArooaComponent
	public synchronized void setJob(Object job) {
		if (job == null) {
			childHelper.removeChildAt(0);
		}
		else {
			childHelper.insertChild(0, job);
		}
	}

	public ResetAction getReset() {
		return reset;
	}

	@ArooaAttribute
	public void setReset(ResetAction resetAction) {
		this.reset = resetAction;
	}
	
	public Properties getProperties() {
		return properties;
	}
}
