package org.oddjob.beanbus.mega;

import org.junit.Test;

import java.util.Collection;

import org.oddjob.OjTestCase;

import org.oddjob.beanbus.AbstractDestination;
import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusListener;
import org.oddjob.beanbus.destinations.Batcher;
import org.oddjob.beanbus.destinations.BeanCapture;
import org.oddjob.framework.extend.SimpleJob;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;

public class StatefulBusConductorAdapterTest extends OjTestCase {

	int started;
	int tripStarted;
	int tripComplete;
	int stopped;
	int crashed;
	int terminated;
	
	private class OurBusListener implements BusListener {

		@Override
		public void busStarting(BusEvent event) throws BusCrashException {
			++started;
		}

		@Override
		public void tripBeginning(BusEvent event) throws BusCrashException {
			++tripStarted;
		}

		@Override
		public void tripEnding(BusEvent event) throws BusCrashException {
			++tripComplete;
		}

		@Override
		public void busStopping(BusEvent event) throws BusCrashException {
			++stopped;
		}

		@Override
		public void busStopRequested(BusEvent event) {
			throw new RuntimeException("Unexpected!");
		}

		@Override
		public void busTerminated(BusEvent event) {
			++terminated;
		}

		@Override
		public void busCrashed(BusEvent event) {
			++crashed;
		}
	}
	
   @Test
	public void testStartedAndStopped() {
		
		FlagState flag = new FlagState();
		
		StatefulBusConductorAdapter test = 
				new StatefulBusConductorAdapter(flag);
		
		OurBusListener listener = new OurBusListener();
		
		test.addBusListener(listener);
		
		flag.run();
		
		assertEquals(1, started);
		assertEquals(1, tripStarted);
		assertEquals(1, tripComplete);
		assertEquals(1, stopped);
		assertEquals(1, terminated);
		assertEquals(0, crashed);
		
		// test listener removed ok.
		
		test.removeBusListener(listener);
		
		flag.hardReset();
		flag.run();
		
		assertEquals(1, started);
		assertEquals(1, stopped);
	}
	
   @Test
	public void testStartedAndCrashed() {
		
		FlagState flag = new FlagState(JobState.EXCEPTION);
		
		StatefulBusConductorAdapter test = 
				new StatefulBusConductorAdapter(flag);
		
		OurBusListener listener = new OurBusListener();
		
		test.addBusListener(listener);
		
		flag.run();
		
		assertEquals(1, started);
		assertEquals(1, tripStarted);
		assertEquals(0, tripComplete);
		assertEquals(0, stopped);
		assertEquals(1, terminated);
		assertEquals(1, crashed);
		
		flag.setState(JobState.COMPLETE);
		
		flag.hardReset();
		flag.run();
		
		assertEquals(2, started);
		assertEquals(2, tripStarted);
		assertEquals(1, tripComplete);
		assertEquals(1, stopped);
		assertEquals(2, terminated);
		assertEquals(1, crashed);
	}
	
	private class CrashingOnStartListener extends OurBusListener {
		
		@Override
		public void busStarting(BusEvent event) throws BusCrashException {
			
			throw new BusCrashException("Bang!");
		}
	}
	
   @Test
	public void testCrashedByListenerWhenStarting() {
		
		FlagState flag = new FlagState();
		
		StatefulBusConductorAdapter test = 
				new StatefulBusConductorAdapter(flag);
		
		CrashingOnStartListener listener = new CrashingOnStartListener();
		
		test.addBusListener(listener);
		
		flag.run();
		
		assertEquals(0, started);
		assertEquals(0, tripStarted);
		assertEquals(0, tripComplete);
		assertEquals(0, stopped);
		assertEquals(1, terminated);
		assertEquals(1, crashed);
		
	}
	
	private class OurJob extends SimpleJob {

		private Collection<String> to;
		
		@Override
		protected int execute() throws Throwable {
			to.add("apples");
			to.add("oranges");
			to.add("pears");
			return 0;
		}
	}
	
   @Test
	public void testCleanBusWithBatcher() {

		Batcher<String> batcher = new Batcher<String>();
		batcher.setBatchSize(2);

		BeanCapture<Collection<String>> results = 
				new BeanCapture<Collection<String>>();

		OurJob job = new OurJob();
		
		job.to = batcher;
		batcher.setTo(results);
		
		
		StatefulBusConductorAdapter test = 
				new StatefulBusConductorAdapter(job);
		
		batcher.setBeanBus(test);
		results.setBusConductor(test);
		
		OurBusListener listener = new OurBusListener();
		
		test.addBusListener(listener);
		
		job.run();
		
		assertEquals(JobState.COMPLETE, job.lastStateEvent().getState());
		
		assertEquals(1, started);
		assertEquals(2, tripStarted);
		assertEquals(2, tripComplete);
		assertEquals(1, stopped);
		assertEquals(1, terminated);
		assertEquals(0, crashed);
		
		assertEquals(3, batcher.getCount());
		assertEquals(2, results.getCount());
		
		job.hardReset();
		
		job.run();
		
		assertEquals(JobState.COMPLETE, job.lastStateEvent().getState());
		
		assertEquals(2, started);
		assertEquals(4, tripStarted);
		assertEquals(4, tripComplete);
		assertEquals(2, stopped);
		assertEquals(2, terminated);
		assertEquals(0, crashed);
		
		assertEquals(3, batcher.getCount());
		assertEquals(2, results.getCount());
		
	}

	private class NaughtyDestination extends AbstractDestination<String> {
		
		@Override
		public boolean add(String e) {
			throw new RuntimeException("Naughty!");
		}
	}
	
   @Test
	public void testWithNaughtyDestination() {

		NaughtyDestination naughty = new NaughtyDestination();

		OurJob job = new OurJob();
		
		job.to = naughty;
		
		StatefulBusConductorAdapter test = 
				new StatefulBusConductorAdapter(job);
		
		OurBusListener listener = new OurBusListener();
		
		test.addBusListener(listener);
		
		job.run();
		
		assertEquals(JobState.EXCEPTION, job.lastStateEvent().getState());
		
		assertEquals(1, started);
		assertEquals(1, tripStarted);
		assertEquals(0, tripComplete);
		assertEquals(0, stopped);
		assertEquals(1, terminated);
		assertEquals(1, crashed);
				
	}
	
}
