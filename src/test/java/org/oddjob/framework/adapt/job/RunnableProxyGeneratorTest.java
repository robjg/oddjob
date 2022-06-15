package org.oddjob.framework.adapt.job;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;

import java.util.Optional;

import static org.hamcrest.Matchers.is;

public class RunnableProxyGeneratorTest extends OjTestCase {

	public interface MyInterface {
		
	}
	
	public static class MyJob implements Runnable, MyInterface {
		
		@Override
		public void run() {
		}
	}
	
   @Test
	public void testAProxyImplementsAllInterfaces() {

	   ArooaSession session = new StandardArooaSession();

		MyJob job = new MyJob();

	   Optional<JobAdaptor> jobAdaptor = new JobStrategies().adapt(job, session);
	   assertThat(jobAdaptor.isPresent(), is(true));

	   Object proxy = new JobProxyGenerator().generate(
			   jobAdaptor.get(),
			   getClass().getClassLoader());

		assertTrue(proxy instanceof MyInterface);
	}
	
}
