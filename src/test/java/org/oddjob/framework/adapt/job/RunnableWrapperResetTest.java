package org.oddjob.framework.adapt.job;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.Resettable;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.Destroy;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.adapt.HardReset;
import org.oddjob.framework.adapt.SoftReset;
import org.oddjob.state.ParentState;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

public class RunnableWrapperResetTest extends OjTestCase {

	public static class Bean1 implements Runnable {

		boolean reset;
		AtomicBoolean destroyed = new AtomicBoolean();
		
		@Override
		public void run() {
			reset = false;
		}
		
		@HardReset
		@SoftReset
		public void reset() {
			reset = true;
		}
		
		public boolean getReset() {
			return reset;
		}
		
		public AtomicBoolean getDestroyed() {
			return destroyed;
		}
		
		@Destroy
		public void destroy() {
			destroyed.set(true);
		}
	}
	
   @Test
	public void testHardReset() throws ArooaPropertyException, ArooaConversionException {
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <bean id='b' class='" + Bean1.class.getName() + "'/>" +
				" </job>" + 
				"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals(false, lookup.lookup("b.reset"));
		
		((Resettable) lookup.lookup("b")).hardReset();
		
		assertEquals(true, lookup.lookup("b.reset"));
		
		assertEquals(ParentState.READY, 
				oddjob.lastStateEvent().getState());
		
		AtomicBoolean destroyed = lookup.lookup("b.destroyed", 
				AtomicBoolean.class);
		
		assertEquals(false, destroyed.get());
		
		oddjob.destroy();
		
		assertEquals(true, destroyed.get());
	}
	
	public static class Bean2 implements Callable<Integer> {

		boolean reset;
		
		@Override
		public Integer call() throws Exception {
			reset = false;
			return 1;
		}
		
		@SoftReset
		public void reset() {
			reset = true;
		}
		
		public boolean getReset() {
			return reset;
		}
	}
	
   @Test
	public void testSoftReset() throws ArooaPropertyException, ArooaConversionException {
		
		String xml = 
				"<oddjob>" +
				" <job>" +
				"  <bean id='b' class='" + Bean2.class.getName() + "'/>" +
				" </job>" + 
				"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		assertEquals(ParentState.INCOMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		assertEquals(false, lookup.lookup("b.reset"));
		
		((Resettable) lookup.lookup("b")).softReset();
		
		assertEquals(true, lookup.lookup("b.reset"));
		
		assertEquals(ParentState.READY, 
				oddjob.lastStateEvent().getState());

		oddjob.run();
		
		assertEquals(false, lookup.lookup("b.reset"));
		
		((Resettable) lookup.lookup("b")).hardReset();
		
		assertEquals(false, lookup.lookup("b.reset"));
				
		oddjob.destroy();
	}
}
