/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.jmx.handlers;

import java.util.Properties;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.mockito.Mockito;
import org.oddjob.MockStateful;
import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.input.InputRequest;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientSession;
import org.oddjob.jmx.client.MockClientSession;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.MockServerContext;
import org.oddjob.jmx.server.MockServerSession;
import org.oddjob.jmx.server.MockServerSideToolkit;
import org.oddjob.jmx.server.ServerContext;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerLoopBackException;
import org.oddjob.jmx.server.ServerSession;
import org.oddjob.jobs.tasks.BasicTask;
import org.oddjob.jobs.tasks.Task;
import org.oddjob.jobs.tasks.TaskException;
import org.oddjob.jobs.tasks.TaskExecutor;
import org.oddjob.jobs.tasks.TaskState;
import org.oddjob.jobs.tasks.TaskView;
import org.oddjob.state.JobState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

public class TaskExecutorHandlerFactoryTest extends TestCase {

	private class OurTaskView extends MockStateful 
	implements TaskView {
		StateListener l;
		public void addStateListener(StateListener listener) {
			assertNull(l);
			l = listener;
			l.jobStateChange(new StateEvent(this, JobState.READY));
		}
		public void removeStateListener(StateListener listener) {
			assertNotNull(l);
			l = null;
		}		
		@Override
		public Object getTaskResponse() {
			return "Apples";
		}
	}
	
	private class OurTaskExecutor implements TaskExecutor {
		
		@Override
		public InputRequest[] getParameterInfo() {
			return new InputRequest[0];
		}
		
		@Override
		public TaskView execute(Task task) throws TaskException {
			return new OurTaskView();
		}
	}

	private class MockTaskViewProxy implements Stateful, DynaBean {

		private StateListener stateListener;
		
		@Override
		public boolean contains(String arg0, String arg1) {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public Object get(String property) {
			assertEquals("taskResponse", property);
			return "Hello";
		}

		@Override
		public Object get(String arg0, int arg1) {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public Object get(String arg0, String arg1) {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public DynaClass getDynaClass() {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public void remove(String arg0, String arg1) {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public void set(String arg0, Object arg1) {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public void set(String arg0, int arg1, Object arg2) {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public void set(String arg0, String arg1, Object arg2) {
			throw new RuntimeException("Unexpected");
		}

		@Override
		public void addStateListener(StateListener listener)
				throws JobDestroyedException {
			stateListener = listener;
		}

		@Override
		public void removeStateListener(StateListener listener) {
			stateListener = null;
		}

		@Override
		public StateEvent lastStateEvent() {
			return new StateEvent(this, TaskState.INPROGRESS);
		}
		
	}
	
	private class OurClientToolkit extends MockClientSideToolkit {
		ServerInterfaceHandler server;

		MockTaskViewProxy mockTaskViewProxy = new MockTaskViewProxy();
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> T invoke(RemoteOperation<T> remoteOperation, Object... args)
				throws Throwable {
			return (T) server.invoke(remoteOperation, args);
		}
		
		@Override
		public ClientSession getClientSession() {
			return new MockClientSession() {
				@Override
				public Object create(ObjectName objectName) {
					return mockTaskViewProxy;
				}
			};
		}
	}

	private class OurServerSideToolkit extends MockServerSideToolkit {

		@Override
		public ServerSession getServerSession() {
			return new MockServerSession() {
				@Override
				public ObjectName createMBeanFor(Object child,
						ServerContext childContext) {
					try {
						return new ObjectName("Foo:name=Foo");
					} catch (MalformedObjectNameException e) {
						throw new RuntimeException(e);
					}
				}
			};
		}
				
		@Override
		public ServerContext getContext() {
			return new MockServerContext() {
				@Override
				public ServerContext addChild(Object child)
						throws ServerLoopBackException {
					return this;
				}
			};
		}
	}
	
	public void testExecuteTaskAndAddRemoveListenerToView() throws Exception {

		TaskExecutorHandlerFactory test = new TaskExecutorHandlerFactory();
		
		OurTaskExecutor taskExecutor = new OurTaskExecutor(); 
		OurServerSideToolkit serverToolkit = new OurServerSideToolkit();

		// create the handler
		ServerInterfaceHandler serverHandler = test.createServerHandler(
				taskExecutor, serverToolkit);

		OurClientToolkit clientToolkit = new OurClientToolkit();

		TaskExecutor local = new TaskExecutorHandlerFactory.ClientTaskExecutorHandlerFactory(
				).createClientHandler(null, clientToolkit);
		
		clientToolkit.server = serverHandler;

		assertNotNull(local.getParameterInfo());
		
		TaskView view = local.execute(new BasicTask(new Properties()));

		view.addStateListener(Mockito.mock(StateListener.class));

		assertNotNull(clientToolkit.mockTaskViewProxy.stateListener);
		
		view.removeStateListener(Mockito.mock(StateListener.class));

		assertNull(clientToolkit.mockTaskViewProxy.stateListener);
		
		assertEquals(TaskState.INPROGRESS, view.lastStateEvent().getState());
		
		assertEquals("Hello", view.getTaskResponse());
	}
	
}
