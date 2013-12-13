package org.oddjob.framework;

import java.io.IOException;

import junit.framework.TestCase;

import org.oddjob.FailedToStopException;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconHelper;
import org.oddjob.images.IconListener;
import org.oddjob.state.JobState;
import org.oddjob.tools.OddjobTestHelper;

public class SerializableJobTest extends TestCase {

	class OurSession extends MockArooaSession {
		
		int count;
		
		@Override
		public ComponentPool getComponentPool() {
			
			return new MockComponentPool() {
				
				@Override
				public void save(Object component) {
					
					++count;
				}
				
				@Override
				public void configure(Object component) {
				}
			};
		}
		
	}
	
	static class OurJob extends SerializableJob {
		private static final long serialVersionUID = 2009031800L;
		
		@Override
		protected int execute() throws Throwable {
			return 0;
		}
	}
	
	public void testSaveOnChangeState() {

		OurSession session = new OurSession();
		
		OurJob test = new OurJob();
		test.setArooaSession(session);
		
		
		test.run();
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		
		assertEquals(1, session.count);
		
		test.hardReset();
		
		assertEquals(2, session.count);
		
		assertEquals(JobState.READY, test.lastStateEvent().getState());
		
		test.run();
		
		assertEquals(3, session.count);
		
		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		
	}

	class IconCatcher implements IconListener {
		
		String iconId;
		
		public void iconEvent(IconEvent e) {
			iconId = e.getIconId();
		}
	}
	
	
	
	public void testSerialization() throws IOException, ClassNotFoundException {
		
		OurJob test = new OurJob();
		
		test.run();

		OurJob copy = OddjobTestHelper.copy(test);
		
		assertEquals(JobState.COMPLETE, copy.lastStateEvent().getState());
		
		IconCatcher icon = new IconCatcher();
		
		copy.addIconListener(icon);
		
		assertEquals(IconHelper.COMPLETE, icon.iconId);
	}
	
	static class OurStopJob extends SerializableJob {
		private static final long serialVersionUID = 2009031800L;
		
		@Override
		protected int execute() {
			if (stop) {
				return 1;
			}
			new Thread() {
				public void run() {
					try {
						OurStopJob.this.stop();
					} catch (FailedToStopException e) {
						e.printStackTrace();
					}
				};
			}.start();
			while(!stop) {
				try {
					synchronized (this) {
						wait(1000);					
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			return 0;
		}
	}

	/**
	 * Finding a bug where stop not reste.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void testStopAndReset() throws IOException, ClassNotFoundException {
		
		OurStopJob test = new OurStopJob();
		
		test.run();

		assertEquals(JobState.COMPLETE, test.lastStateEvent().getState());
		
		OurStopJob copy = OddjobTestHelper.copy(test);
		
		assertEquals(JobState.COMPLETE, copy.lastStateEvent().getState());
		
		copy.hardReset();
		
		assertEquals(JobState.READY, copy.lastStateEvent().getState());
		
		copy.run();
		
		assertEquals(JobState.COMPLETE, copy.lastStateEvent().getState());
		
		copy.hardReset();
		
		assertEquals(JobState.READY, copy.lastStateEvent().getState());
		
		copy.run();

		assertEquals(JobState.COMPLETE, copy.lastStateEvent().getState());
	}	
}
