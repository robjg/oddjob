/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.jmx.handlers;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.oddjob.MockStateful;
import org.oddjob.OjTestCase;
import org.oddjob.Stateful;
import org.oddjob.framework.JobDestroyedException;
import org.oddjob.input.InputRequest;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.client.ClientSession;
import org.oddjob.jmx.client.MockClientSession;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.*;
import org.oddjob.jobs.tasks.*;
import org.oddjob.state.JobState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

import java.util.Properties;

public class TaskExecutorHandlerFactoryTest extends OjTestCase {

	private static class OurTaskView extends MockStateful
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
	
	private static class OurTaskExecutor implements TaskExecutor {
		
		@Override
		public InputRequest[] getParameterInfo() {
			return new InputRequest[0];
		}
		
		@Override
		public TaskView execute(Task task) throws TaskException {
			return new OurTaskView();
		}
	}

	private static class MockTaskViewProxy implements Stateful, DynaBean {

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
	
	private static class OurClientToolkit extends MockClientSideToolkit {
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
				public Object create(long objectName) {
					return mockTaskViewProxy;
				}
			};
		}
	}

	private static class OurServerSideToolkit extends MockServerSideToolkit {

		@Override
		public ServerSession getServerSession() {
			return new MockServerSession() {
				@Override
				public long createMBeanFor(Object child,
						ServerContext childContext) {
					return 2L;
				}
			};
		}
				
		@Override
		public ServerContext getContext() {
			return new MockServerContext() {
				@Override
				public ServerContext addChild(Object child) {
					return this;
				}
			};
		}
	}
	
   @Test
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
