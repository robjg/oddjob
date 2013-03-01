package org.oddjob.beanbus.mega;

import junit.framework.TestCase;

import org.oddjob.beanbus.BusCrashException;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusListener;
import org.oddjob.state.FlagState;

public class StatfulsBusConductorAdapterTest extends TestCase {

	int started;
	int tripStarted;
	int tripComplete;
	int stopped;
	
	
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
			throw new RuntimeException("Unexpected!");
		}

		@Override
		public void busCrashed(BusEvent event) {
			throw new RuntimeException("Unexpected!");
		}
	}
	
	public void testStartedAndStopped() {
		
		FlagState flag = new FlagState();
		
		StatefulBusConductorAdaptor test = 
				new StatefulBusConductorAdaptor(flag);
		
		OurBusListener listener = new OurBusListener();
		
		test.addBusListener(listener);
		
		flag.run();
		
		assertEquals(1, started);
		assertEquals(1, tripStarted);
		assertEquals(1, tripComplete);
		assertEquals(1, stopped);
		
		// test listener removed ok.
		
		test.removeBusListener(listener);
		
		flag.hardReset();
		flag.run();
		
		assertEquals(1, started);
		assertEquals(1, stopped);
	}
}
