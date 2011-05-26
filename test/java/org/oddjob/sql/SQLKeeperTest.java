package org.oddjob.sql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.io.BufferType;
import org.oddjob.schedules.schedules.CountSchedule;
import org.oddjob.scheduling.LoosingOutcome;
import org.oddjob.scheduling.MockScheduledExecutorService;
import org.oddjob.scheduling.MockScheduledFuture;
import org.oddjob.scheduling.Outcome;
import org.oddjob.scheduling.WinningOutcome;
import org.oddjob.state.JobState;
import org.oddjob.state.JobStateEvent;
import org.oddjob.state.JobStateListener;

public class SQLKeeperTest extends TestCase {
	
	ConnectionType ct;
	
	@Override
	protected void setUp() throws Exception {
		ct = new ConnectionType();
		ct.setDriver("org.hsqldb.jdbcDriver");
		ct.setUrl("jdbc:hsqldb:mem:test");
		ct.setUsername("sa");
		ct.setPassword("");
		
		BufferType buffer = new BufferType();
		buffer.setText("CREATE TABLE oddjob_grabbable(" +
				"key VARCHAR(128), " +
				"instance VARCHAR(32), " +
				"winner VARCHAR(32), " +
				"complete boolean, " +
				"CONSTRAINT oddjob_pk PRIMARY KEY (key))");
		buffer.configured();
		
		SQLJob sql = new SQLJob();
		sql.setArooaSession(new StandardArooaSession());
		sql.setInput(buffer.toInputStream());
		
		sql.setConnection(ct.toValue());
		
		sql.run();
	}
	
	@Override
	protected void tearDown() throws Exception {
		
		BufferType buffer = new BufferType();
		buffer.setText("shutdown");
		buffer.configured();
		
		SQLJob sql = new SQLJob();
		sql.setArooaSession(new StandardArooaSession());
		sql.setInput(buffer.toInputStream());
		
		sql.setConnection(ct.toValue());
		
		sql.run();
	}
	
	private class OurListener implements JobStateListener {
		List<JobState> states = new ArrayList<JobState>();
		
		@Override
		public void jobStateChange(JobStateEvent event) {
			states.add(event.getJobState());
		}
	}
	
	private class OurFuture extends MockScheduledFuture<Void> {
		boolean canceled;
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			this.canceled = true;
			return true;
		}
	}
	
	private class OurExcecutor extends MockScheduledExecutorService {
		Runnable runnable;
		
		OurFuture  future = new OurFuture();
		
		@Override
		public ScheduledFuture<?> schedule(Runnable command, long delay,
				TimeUnit unit) {
			
			this.runnable = command; 
			return future;
		}
	}
	
	public void testFreshRun() throws SQLException, ArooaConversionException {
		
		OurExcecutor executor = new OurExcecutor();
		
		SQLKeeperService test = new SQLKeeperService();
		test.setConnection(ct.toValue());
		test.setScheduleExecutorService(executor);
		
		test.start();
		
		Outcome first = test.getKeeper("secret").grab("apple", "first");
		
		assertTrue(first.isWon());
		assertEquals("apple", first.getWinner());
		
		Outcome second = test.getKeeper("secret").grab("orange", "first");		
		
		assertFalse(second.isWon());
		assertEquals("apple", second.getWinner());

		OurListener listener = new OurListener();
		
		((LoosingOutcome) second).addJobStateListener(listener);
		
		assertEquals(1, listener.states.size());
		assertEquals(JobState.EXECUTING, listener.states.get(0));
		
		((WinningOutcome) first).complete();
		
		Runnable runnable = executor.runnable;
		runnable.run();
		
		assertEquals(2, listener.states.size());
		assertEquals(JobState.COMPLETE, listener.states.get(1));
		
		assertTrue(executor.runnable == runnable);
		
		test.stop();
	}
	
	public void testFreshStop() throws SQLException, ArooaConversionException {
		
		OurExcecutor executor = new OurExcecutor();
		
		SQLKeeperService test = new SQLKeeperService();
		test.setConnection(ct.toValue());
		test.setScheduleExecutorService(executor);
		
		test.start();
		
		Outcome first = test.getKeeper("secret").grab("apple", "first");
		
		assertTrue(first.isWon());
		assertEquals("apple", first.getWinner());
		
		Outcome second = test.getKeeper("secret").grab("orange", "first");		
		
		assertFalse(second.isWon());
		assertEquals("apple", second.getWinner());

		OurListener listener = new OurListener();
		
		((LoosingOutcome) second).addJobStateListener(listener);
		
		assertEquals(1, listener.states.size());
		assertEquals(JobState.EXECUTING, listener.states.get(0));
		
		assertNotNull(executor.runnable);
		
		((LoosingOutcome) second).removeJobStateListener(listener);
		
		test.stop();
		
		assertTrue(executor.future.canceled);
		
	}
	
	public void testTimeout() throws SQLException, ArooaConversionException {
		
		OurExcecutor executor = new OurExcecutor();
		
		CountSchedule count = new CountSchedule(1);
		
		SQLKeeperService test = new SQLKeeperService();
		test.setConnection(ct.toValue());
		test.setScheduleExecutorService(executor);
		test.setPollSchedule(count);
		
		test.start();
		
		Outcome first = test.getKeeper("secret").grab("apple", "first");
		
		assertTrue(first.isWon());
		assertEquals("apple", first.getWinner());
		
		Outcome second = test.getKeeper("secret").grab("orange", "first");
		
		assertFalse(second.isWon());
		assertEquals("apple", second.getWinner());

		OurListener listener = new OurListener();
		
		((LoosingOutcome) second).addJobStateListener(listener);
		
		assertEquals(1, listener.states.size());
		assertEquals(JobState.EXECUTING, listener.states.get(0));
		
		assertNotNull(executor.runnable);
		
		executor.runnable.run();
		
		assertEquals(JobState.EXCEPTION, listener.states.get(1));
		assertEquals(2, listener.states.size());
		
		test.stop();
	}

}
