/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.handlers;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.input.InputRequest;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.server.*;
import org.oddjob.jobs.tasks.Task;
import org.oddjob.jobs.tasks.TaskException;
import org.oddjob.jobs.tasks.TaskExecutor;
import org.oddjob.jobs.tasks.TaskView;
import org.oddjob.remote.NotificationType;
import org.oddjob.remote.RemoteException;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanOperationInfo;
import javax.management.ReflectionException;
import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TaskExecutorHandlerFactory 
implements ServerInterfaceHandlerFactory<TaskExecutor, TaskExecutor> {
	
	private static final Logger logger = LoggerFactory.getLogger(TaskExecutorHandlerFactory.class);
	
	public static final HandlerVersion VERSION = new HandlerVersion(2, 0);
		
	public static final JMXOperationPlus<InputRequest[]> GET_PARAMETER_INFO =
			new JMXOperationPlus<>(
					"Tasks.getParameterInfo",
					"Get parameter info for a task executor.",
					InputRequest[].class, 
					MBeanOperationInfo.INFO);
			
	public static final JMXOperationPlus<TaskViewData> EXECUTE =
			new JMXOperationPlus<>(
					"Tasks.execute",
					"Execute a Task.",
					TaskViewData.class, 
					MBeanOperationInfo.ACTION)
				.addParam("task", Task.class, "The task.");

	@Override
	public Class<TaskExecutor> serverClass() {
		return TaskExecutor.class;
	}

	@Override
	public Class<TaskExecutor> clientClass() {
		return TaskExecutor.class;
	}

	@Override
	public HandlerVersion getHandlerVersion() {
		return VERSION;
	}

	@Override
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	@Override
	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
				GET_PARAMETER_INFO.getOpInfo(),
				EXECUTE.getOpInfo()
		};
	}

	@Override
	public List<NotificationType<?>> getNotificationTypes() {
		return Collections.emptyList();
	}

	@Override
	public ServerInterfaceHandler createServerHandler(
			TaskExecutor taskExecutor, 
			ServerSideToolkit ojmb) {

		return new ServerTaskExecutorHelper(taskExecutor, ojmb);
	}
	
	public static class ClientFactory
	implements ClientInterfaceHandlerFactory<TaskExecutor> {

		@Override
		public Class<TaskExecutor> interfaceClass() {
			return TaskExecutor.class;
		}

		@Override
		public HandlerVersion getVersion() {
			return VERSION;
		}

		@Override
		public TaskExecutor createClientHandler(TaskExecutor proxy, ClientSideToolkit toolkit) {
			return new ClientTaskExecutorHandler(proxy, toolkit);
		}		
	}
	
	static class ClientTaskExecutorHandler 
	implements TaskExecutor {

		private final ClientSideToolkit toolkit;

		ClientTaskExecutorHandler(TaskExecutor proxy, ClientSideToolkit toolkit) {
			this.toolkit = toolkit;
		}
		
		@Override
		public InputRequest[] getParameterInfo() {
			try {
				return toolkit.invoke(
						GET_PARAMETER_INFO);
			}
			catch (Throwable e) {
				throw new UndeclaredThrowableException(e);
			}
		}
		
		@Override
		public TaskView execute(Task task) throws TaskException {
			try {
				TaskViewData taskViewData = toolkit.invoke(
						EXECUTE,
						task);
				
				Object taskViewProxy = toolkit.getClientSession().create(
						taskViewData.objectName);
				
				return new TaskViewAdaptor(taskViewProxy);
			}
			catch (Throwable e) {
				throw new UndeclaredThrowableException(e);
			}
		}		
	}	
	
	public static class TaskViewAdaptor implements TaskView {
		
		private final Stateful proxy;
		
		public TaskViewAdaptor(Object proxy) {
			if (!(proxy instanceof DynaBean)) {
				throw new ClassCastException("Proxy is not a DynaBean");
			}
			this.proxy = (Stateful) proxy;
		}
		
		@Override
		public void addStateListener(StateListener listener)
				throws JobDestroyedException {
			proxy.addStateListener(listener);
		}
		
		@Override
		public void removeStateListener(StateListener listener) {
			proxy.removeStateListener(listener);
		}
		
		@Override
		public StateEvent lastStateEvent() {
			return proxy.lastStateEvent();
		}
		
		@Override
		public Object getTaskResponse() {
			return ((DynaBean) proxy).get("taskResponse");
		}
	}
	
	static class ServerTaskExecutorHelper implements ServerInterfaceHandler  {

		private final TaskExecutor taskExecutor;
		private final ServerSideToolkit toolkit;
		
		/** Child remote job nodes. */
		private final LinkedList<Long> taskViews = new LinkedList<>();

		
		ServerTaskExecutorHelper(TaskExecutor taskExecutor, 
				ServerSideToolkit ojmb) {
			this.taskExecutor = taskExecutor;
			this.toolkit = ojmb;
		}

		public Object invoke(RemoteOperation<?> operation, Object[] params) throws MBeanException, ReflectionException {
			
			if (GET_PARAMETER_INFO.equals(operation)) {
				return taskExecutor.getParameterInfo();
			}
			else if (EXECUTE.equals(operation)) {
				TaskView taskView;
				try {
					taskView = taskExecutor.execute((Task) params[0]);
				}
				catch (TaskException e) {
					throw new MBeanException(e);
				}
				if (taskView == null) {

				}
				return createTaskViewMBean(taskView);
			}

			throw new ReflectionException(
					new IllegalStateException("invoked for an unknown method."), 
							operation.toString());
		}
		
		protected TaskViewData createTaskViewMBean(TaskView taskView) {
			final long objectName;
			try {
				objectName = toolkit.getServerSession().createMBeanFor(
						taskView, toolkit.getContext().addChild(taskView));
			} catch (ServerLoopBackException | RemoteException e) {
				throw new IllegalStateException("Failed creating Task View MBean.", e);
			}
			
			taskViews.add(objectName);
			
			taskView.addStateListener(event -> {
				if (event.getState().isDestroyed()) {
					taskViews.remove(objectName);
					destroyTaskViewMBean(objectName);
				}
			});
			
			return new TaskViewData(objectName);
		}
		
		
		public void destroy() {
			
			// destroy the task view MBeans.
			while (!taskViews.isEmpty()) {
				long taskViewObjectName = taskViews.remove();
				destroyTaskViewMBean(taskViewObjectName);
			}
		}

		protected void destroyTaskViewMBean(long taskViewObjectName) {
			try {
				toolkit.getServerSession().destroy(taskViewObjectName);
			} catch (RemoteException e1) {
				logger.error("Failed destroying child [" + taskViewObjectName + "]", e1);
			}
		}
		
	}

	
	public static class TaskViewData implements Serializable {
		private static final long serialVersionUID = 2015051200L;
		
		private final long objectName;
		
		public TaskViewData(long objectName) {
			this.objectName = objectName;
		}
		
		public long getTaskViewObjectName() {
			return objectName;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		return obj.getClass() == this.getClass();
	}
	
	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	
}
