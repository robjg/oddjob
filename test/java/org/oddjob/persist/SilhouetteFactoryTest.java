package org.oddjob.persist;

import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

import org.oddjob.Describeable;
import org.oddjob.Helper;
import org.oddjob.Iconic;
import org.oddjob.Stateful;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.images.IconHelper;
import org.oddjob.jobs.EchoJob;
import org.oddjob.jobs.structural.SequentialJob;
import org.oddjob.monitor.model.Describer;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;

public class SilhouetteFactoryTest extends TestCase {

	public void testSimple() throws IOException, ClassNotFoundException {
		
		EchoJob echo = new EchoJob();
		echo.setText("Hello");
		echo.setName("Greeting");
		
		echo.run();
		
		ArooaSession session = new StandardArooaSession();
		
		Object silhouette = Helper.copy(
				new SilhouetteFactory().create(echo, session));
		
		assertTrue(silhouette instanceof Describeable);
		assertFalse(silhouette instanceof Stateful);
		assertFalse(silhouette instanceof Structural);
		assertFalse(silhouette instanceof Iconic);
		
		Map<String, String> description = Describer.describe(silhouette);
		
		assertEquals("Hello", description.get("text"));
		
		assertEquals("Greeting", silhouette.toString());		
	}
	
	public void testStateful() throws IOException, ClassNotFoundException {
		
		FlagState flag = new FlagState();
		
		flag.run();
		
		ArooaSession session = new StandardArooaSession();
		
		Object silhouette = Helper.copy(
				new SilhouetteFactory().create(flag, session));
		
		assertTrue(silhouette instanceof Stateful);
		assertTrue(silhouette instanceof Iconic);
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(silhouette));
		assertEquals(IconHelper.COMPLETE, Helper.getIconId(silhouette));
	}
	
	public void testStructural() throws IOException, ClassNotFoundException {
		
		SequentialJob sequential = new SequentialJob();
		
		FlagState flag = new FlagState();
		
		sequential.setJobs(0, flag);
		
		sequential.run();
		
		final ArooaSession session = new StandardArooaSession();
		
		ComponentPool components = session.getComponentPool();
		components.registerComponent(
				new ComponentTrinity(flag, flag, 
						new MockArooaContext() {
					@Override
					public ArooaSession getSession() {
						return session;
					}
				}), null);
		
		Object silhouette = Helper.copy(
				new SilhouetteFactory().create(sequential, session));
		
		assertTrue(silhouette instanceof Structural);
		
		Object[] children = Helper.getChildren((Structural) silhouette);
		
		assertEquals(1, children.length);
		
		assertEquals(JobState.COMPLETE, Helper.getJobState(children[0]));
	}
	
	public void testNotOurChildren() throws IOException, ClassNotFoundException {
		
		SequentialJob sequential = new SequentialJob();
		
		FlagState flag = new FlagState();
		
		sequential.setJobs(0, flag);
		
		sequential.run();
		
		final ArooaSession session = new StandardArooaSession();
		
		Object silhouette = Helper.copy(
				new SilhouetteFactory().create(sequential, session));
		
		assertTrue(silhouette instanceof Structural);
		
		Object[] children = Helper.getChildren((Structural) silhouette);
		
		assertEquals(0, children.length);		
	}
}
