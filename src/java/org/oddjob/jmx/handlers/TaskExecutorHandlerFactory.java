/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx.handlers;

import java.io.Serializable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedList;

import javax.management.JMException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.apache.commons.beanutils.DynaBean;
import org.apache.log4j.Logger;
import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.input.InputRequest;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientHandlerResolver;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.HandlerVersion;
import org.oddjob.jmx.client.SimpleHandlerResolver;
import org.oddjob.jmx.server.JMXOperationPlus;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceHandlerFactory;
import org.oddjob.jmx.server.ServerLoopBackException;
import org.oddjob.jmx.server.ServerSideToolkit;
import org.oddjob.jobs.tasks.Task;
import org.oddjob.jobs.tasks.TaskException;
import org.oddjob.jobs.tasks.TaskExecutor;
import org.oddjob.jobs.tasks.TaskView;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

public class TaskExecutorHandlerFactory 
implements ServerInterfaceHandlerFactory<TaskExecutor, TaskExecutor> {
	
	private static final Logger logger = Logger.getLogger(TaskExecutorHandlerFactory.class);
	
	public static final HandlerVersion VERSION = new HandlerVersion(1, 0);
		
	static final JMXOperationPlus<InputRequest[]> GET_PARAMETER_INFO = 
			new JMXOperationPlus<>(
					"Tasks.getParameterInfo",
					"Get parameter info for a task executor.",
					InputRequest[].class, 
					MBeanOperationInfo.INFO);
			
	static final JMXOperationPlus<TaskViewData> EXECUTE = 
			new JMXOperationPlus<>(
					"Tasks.execute",
					"Execute a Task.",
					TaskViewData.class, 
					MBeanOperationInfo.ACTION)
				.addParam("task", Task.class, "The task.");
			
	public Class<TaskExecutor> interfaceClass() {
		return TaskExecutor.class;
	}
	
	public MBeanAttributeInfo[] getMBeanAttributeInfo() {
		return new MBeanAttributeInfo[0];
	}

	public MBeanOperationInfo[] getMBeanOperationInfo() {
		return new MBeanOperationInfo[] {
				GET_PARAMETER_INFO.getOpInfo(),
				EXECUTE.getOpInfo()
		};
	}
	
	public MBeanNotificationInfo[] getMBeanNotificationInfo() {

		MBeanNotificationInfo[] nInfo = new MBeanNotificationInfo[] {};
		return nInfo;
	}

	public ServerInterfaceHandler createServerHandler(
			TaskExecutor taskExecutor, 
			ServerSideToolkit ojmb) {
		
		ServerTaskExecutorHelper structuralHelper = 
			new ServerTaskExecutorHelper (taskExecutor, ojmb);
		
		return structuralHelper;
	}
	
	public ClientHandlerResolver<TaskExecutor> clientHandlerFactory() {
		return new SimpleHandlerResolver<TaskExecutor>(
				ClientTaskExecutorHandlerFactory.class.getName(),
				VERSION);
	}
	
	public static class ClientTaskExecutorHandlerFactory 
	implements ClientInterfaceHandlerFactory<TaskExecutor> {
		
		public Class<TaskExecutor> interfaceClass() {
			return TaskExecutor.class;
		}
		
		public HandlerVersion getVersion() {
			return VERSION;
		}
		
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
						GET_PARAMETER_INFO,
						new Object[] { } );
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
						new Object[] { task } );
				
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
	
	class ServerTaskExecutorHelper implements ServerInterfaceHandler  {

		private final TaskExecutor taskExecutor;
		private final ServerSideToolkit toolkit;
		
		/** Child remote job nodes. */
		private final LinkedList<ObjectName> taskViews = new LinkedList<>();

		
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
				return createTaskViewMBean(taskView);
			}

			throw new ReflectionException(
					new IllegalStateException("invoked for an unknown method."), 
							operation.toString());
		}
		
		protected TaskViewData createTaskViewMBean(TaskView taskView) {
			final ObjectName objectName;
			try {
				objectName = toolkit.getServerSession().createMBeanFor(
						taskView, toolkit.getContext().addChild(taskView));
			} catch (ServerLoopBackException | JMException e) {
				throw new IllegalStateException("Faild creating Task View MBean.", e);
			}
			
			taskViews.add(objectName);
			
			taskView.addStateListener(new StateListener() {
				@Override
				public void jobStateChange(StateEvent event) {
					if (event.getState().isDestroyed()) {
						taskViews.remove(objectName);
						destroyTaskViewMBean(objectName);
					}
				}
			});
			
			return new TaskViewData(objectName);
		}
		
		
		public void destroy() {
			
			// destroy the task view MBeans.
			while (!taskViews.isEmpty()) {
				ObjectName taskViewObjectName = taskViews.remove();
				destroyTaskViewMBean(taskViewObjectName);
			}
		}

		protected void destroyTaskViewMBean(ObjectName taskViewObjectName) {
			try {
				toolkit.getServerSession().destroy(taskViewObjectName);
			} catch (JMException e1) {
				logger.error("Failed destroying child [" + taskViewObjectName + "]", e1);
			}
		}
		
	}

	
	static class TaskViewData implements Serializable {
		private static final long serialVersionUID = 2015051200L;
		
		private final ObjectName objectName;
		
		public TaskViewData(ObjectName objectName) {
			this.objectName = objectName;
		}
		
		public ObjectName getTaskViewObjectName() {
			return objectName;
		}
	}
	
}
