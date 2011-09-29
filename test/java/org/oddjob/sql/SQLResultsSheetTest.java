package org.oddjob.sql;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConsoleCapture;
import org.oddjob.Oddjob;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.BadBeanException;
import org.oddjob.beanbus.BeanBus;
import org.oddjob.beanbus.BeanSheetTest.Fruit;
import org.oddjob.beanbus.BusEvent;
import org.oddjob.beanbus.BusListener;
import org.oddjob.beanbus.CrashBusException;
import org.oddjob.beanbus.StageEvent;
import org.oddjob.beanbus.StageListener;
import org.oddjob.beanbus.StageNotifier;
import org.oddjob.io.BufferType;
import org.oddjob.io.CopyJob;
import org.oddjob.state.ParentState;

public class SQLResultsSheetTest extends TestCase {

	private static final Logger logger = Logger.getLogger(SQLResultsSheetTest.class);
	
	String EOL = System.getProperty("line.separator");
	
	private class OurBus implements BeanBus {

		BusListener busListener;
		StageListener stageListener;
		
		@Override
		public void addBusListener(BusListener listener) {
			assertNull(this.busListener);
			assertNotNull(listener);
			
			this.busListener = listener;
		}
		
		@Override
		public void addStageListener(StageListener listener) {
			assertNull(this.stageListener);
			assertNotNull(listener);
			
			this.stageListener = listener;
		}
		
		@Override
		public void removeBusListener(BusListener listener) {
			assertEquals(this.busListener, listener);
			assertNotNull(listener);
			this.busListener = null;
		}
		
		@Override
		public void removeStageListener(StageListener listener) {
			assertEquals(this.stageListener, listener);
			assertNotNull(listener);
			this.stageListener = null;
		}
		
		@Override
		public void run() {
			throw new RuntimeException("Unexpected.");
		}
		
		@Override
		public void stop() {
			throw new RuntimeException("Unexpected.");
		}
	}

	private class OurStage implements StageNotifier {
		@Override
		public void addStageListener(StageListener listener) {
			throw new RuntimeException("Unexpected.");
		}
		@Override
		public void removeStageListener(StageListener listener) {
			throw new RuntimeException("Unexpected.");
		}
	}
	
	public void testNoHeaders() throws BadBeanException, CrashBusException {
		
		SQLResultsSheet test = new SQLResultsSheet();
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		Object[] values = createFruit(); 
		
		test.setOutput(out);
		test.setDataOnly(true);
		test.setArooaSession(new StandardArooaSession());
		
		OurBus bus = new OurBus();
		OurStage stage = new OurStage();
		
		test.setBus(bus);
		
		bus.busListener.busStarting(new BusEvent(bus));
		bus.stageListener.stageStarting(new StageEvent(stage, "stuff"));
		
		test.accept(Arrays.asList(values));
		
		bus.stageListener.stageComplete(new StageEvent(stage, "stuff"));
		bus.busListener.busStopping(new BusEvent(bus));
		bus.busListener.busTerminated(new BusEvent(bus));
		
		
		String expected = 
			"Red and Green  7.6    Apple   Cox" + EOL +
			"Orange         9.245  Orange  Jaffa" + EOL;
		
		assertEquals(expected, out.toString());
		
		assertNull(bus.busListener);
		assertNull(bus.stageListener);
	}
	
	private Object[] createFruit() {
		
		Fruit fruit1 = new Fruit();
		fruit1.setType("Apple");
		fruit1.setVariety("Cox");
		fruit1.setColour("Red and Green");
		fruit1.setSize(7.6);
		
		Fruit fruit2 = new Fruit();
		fruit2.setType("Orange");
		fruit2.setVariety("Jaffa");
		fruit2.setColour("Orange");
		fruit2.setSize(9.245);

		return new Object[] { fruit1, fruit2 };
	}
	
	public void testExample() {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/sql/SQLResultsSheetExample.xml",
				getClass().getClassLoader()));
		
		ConsoleCapture console = new ConsoleCapture();
		console.capture(Oddjob.CONSOLE);
				
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		console.close();
		console.dump(logger);
		
		BufferType buffer = new BufferType();
		buffer.configured();
		
		CopyJob copy = new CopyJob();
		copy.setInput(getClass().getResourceAsStream("SQLResultsSheetExample.txt"));
		copy.setOutput(buffer.toOutputStream());
		copy.run();
		
		String[] expected = buffer.getLines();
		
		String[] lines = console.getLines();
		
		assertEquals(expected.length, lines.length);
		
		for (int i = 0; i < expected.length; ++i) {
			assertTrue(expected[i] + " regexp does not match " + lines[i], 
					lines[i].trim().matches(expected[i].trim()));	
		}
		
		oddjob.destroy();
		
	}
}
