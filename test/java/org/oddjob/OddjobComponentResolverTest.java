package org.oddjob;

import java.io.IOException;
import java.io.Serializable;

import junit.framework.TestCase;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.parsing.MockArooaContext;

public class OddjobComponentResolverTest extends TestCase {

	private class OurContext extends MockArooaContext {
		
		@Override
		public ArooaSession getSession() {
			return new MockArooaSession();
		}
	}
	
	public void testRunnable() {
		
		OddjobComponentResolver test = 
			new OddjobComponentResolver();
		
		Runnable runnable = new Runnable() {
			public void run() {
			}
		};
		
		Object proxy = test.resolve(runnable, new OurContext());

		assertTrue(proxy instanceof Runnable);
	}
	
	public static class OurService {
		
		public void start() {}
		
		public void stop() {}
	}
	
	public void testService() {
		
		OddjobComponentResolver test = 
			new OddjobComponentResolver();
				
		Object proxy = test.resolve(new OurService(), new OurContext());

		assertTrue(proxy instanceof Runnable);
	}
	
	static class OurSerializableRunnable implements Runnable, Serializable {
		private static final long serialVersionUID = 2009011000L;

		String colour="red";
		
		public void run() {
		}
	}
	
	public void testRestore() throws IOException, ClassNotFoundException {
		
		OddjobComponentResolver test = 
			new OddjobComponentResolver();
		
		Object job = new OurSerializableRunnable();
		
		Object proxy = test.resolve(job, new OurContext());

		Object restoredProxy = Helper.copy(proxy);
		
		Object restoredJob = test.restore(restoredProxy, new OurContext());
		
		assertEquals(OurSerializableRunnable.class, restoredJob.getClass());
		
		assertEquals(((OurSerializableRunnable) job).colour, "red");
	}
}
