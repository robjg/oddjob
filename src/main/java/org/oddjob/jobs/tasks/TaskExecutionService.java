package org.oddjob.jobs.tasks;


import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.oddjob.FailedToStopException;
import org.oddjob.Stateful;
import org.oddjob.Stoppable;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.deploy.annotations.ArooaInterceptor;
import org.oddjob.arooa.runtime.ExpressionParser;
import org.oddjob.arooa.runtime.ParsedExpression;
import org.oddjob.arooa.runtime.PropertyLookup;
import org.oddjob.arooa.runtime.PropertySource;
import org.oddjob.arooa.utils.ListSetterHelper;
import org.oddjob.framework.extend.SimpleService;
import org.oddjob.input.InputRequest;
import org.oddjob.jobs.job.ResetAction;
import org.oddjob.jobs.job.ResetActions;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;
import org.oddjob.arooa.utils.PropertiesOverrideSession;

/**
 * @oddjob.description Provide a very simple task execution service.
 * <p>
 * The task to be executed is defined by the nested jobs which may use the properties.
 * which will be defined when executing the tasks.
 * <p>
 * This implementation only supports the single execution of a task at one time. If 
 * the task is running additional requests to execute the task will be ignored.
 * <p>
 * Future version will support multiple parallel executions of tasks.
 * 
 * @oddjob.example
 * 
 * A Task Service that greets people by name. Three {@link TaskRequest}s call the 
 * service with different names.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/tasks/TaskRequestSimple.xml} 
 * 
 * @author Rob Gordon
 */
@ArooaInterceptor("org.oddjob.values.properties.PropertiesInterceptor")
public class TaskExecutionService extends SimpleService
implements TaskExecutor, Structural {
	
	/** Track changes to children an notify listeners. */
	protected final ChildHelper<Object> childHelper =
			new ChildHelper<Object>(this);
	
	private final Properties properties = new Properties();

	private final List<InputRequest> requests =
		new ArrayList<InputRequest>();
		
	private volatile InputRequest[] requestArray;
	
	private volatile TaskView taskView;
	
	private volatile ResetAction reset;
	
	private volatile String response;
	
	@Override
	public void setArooaSession(ArooaSession session) {
		super.setArooaSession(session);
		if (! (session instanceof PropertiesOverrideSession)) {
			throw new IllegalStateException();
		}
		((PropertiesOverrideSession) session).getPropertyManager(
				).addPropertyOverride(new TaskPropertyLookup());
	}
	
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
	public TaskView execute(Task task) 
	throws TaskException {
		
		if (requestArray == null) {
			throw new TaskException("Task Execution Service not Started.");
		}

		final ArooaSession session = getArooaSession();
		if (session == null) {
			throw new NullPointerException("No session.");
		}
		
		final Object job = childHelper.getChild();
		if (!(job instanceof Runnable)) {
			throw new TaskException("No Job to Execute the Task.");
		}
		
		if (taskView != null && 
				taskView.lastStateEvent().getState().isStoppable()) {
			throw new TaskException("Task Execution in progress.");
		}
		
		Properties properties = task.getProperties();
		if (properties != null) {
			this.properties.clear();
			this.properties.putAll(properties);
		}
		
		ResetAction reset = this.reset;
		if (reset == null) {
			reset = ResetActions.AUTO;
		}
		
		reset.doWith(job);
		
		((Runnable) job).run();
					
		taskView = new JobTaskView((Stateful) job) {
			
			@Override
			protected Object onDone() {
				
				String responseExpression = response;
				if (responseExpression != null) {
					ExpressionParser parser = session.getTools().getExpressionParser();

					try {
						ParsedExpression expression = parser.parse(responseExpression);
						return expression.evaluate(session, String.class);
					}
					catch (ArooaConversionException e) {
						return "Failed to evaluate response" + e.toString();
					}			
					
				}
				return "OK";
			}
			
			@Override
			public String toString() {
				return "TaskView for " + TaskExecutionService.this.toString();
			}
		};
		
		return taskView;
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
	
	class TaskPropertyLookup implements PropertyLookup {

		private final PropertySource source =  new PropertySource() {
				@Override
				public String toString() {
					return TaskExecutionService.this.toString();
				}
			};
	
		@Override
		public String lookup(String propertyName) {
			return properties.getProperty(propertyName);
		}

		@Override
		public PropertySource sourceFor(String propertyName) {
			if (properties.containsKey(propertyName)) {
				return source;
			}
			else {
				return null;
			}
		}
		
		@Override
		public Set<String> propertyNames() {
			return properties.stringPropertyNames();
		}
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String responseExpression) {
		this.response = responseExpression;
	}
}
