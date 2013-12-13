package org.oddjob.persist;

import java.io.IOException;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Describeable;
import org.oddjob.Iconic;
import org.oddjob.Stateful;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.images.IconHelper;
import org.oddjob.jobs.EchoJob;
import org.oddjob.jobs.structural.SequentialJob;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.tools.OddjobTestHelper;

public class SilhouetteFactoryTest extends TestCase {

	private static final Logger logger = Logger.getLogger(SilhouetteFactoryTest.class);
	@Override
	protected void setUp() throws Exception {
		if (Thread.interrupted()) {
			logger.warn("Clearing interrupted flag left by some other test because it messes with image loading.");
		}
	}
	
	/**
	 * Needed for storing in hash sets.
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public void testEqualsAndHashCode() throws IOException, ClassNotFoundException {
		
		String a = new String("A");
		
		String b = new String("B");
		
		
		ArooaSession session = new StandardArooaSession();
		
		Object silhouetteA = OddjobTestHelper.copy(
				new SilhouetteFactory().create(a, session));
		Object silhouetteB = OddjobTestHelper.copy(
				new SilhouetteFactory().create(b, session));
		
		assertEquals(silhouetteA, silhouetteA);
		assertEquals(silhouetteB, silhouetteB);
		assertFalse(silhouetteA.equals(silhouetteB));
		
		assertEquals(silhouetteA.hashCode(), silhouetteA.hashCode());
		assertEquals(silhouetteB.hashCode(), silhouetteB.hashCode());
		assertFalse(silhouetteA.hashCode() == silhouetteB.hashCode());
	}
	
	public void testSimple() throws IOException, ClassNotFoundException {
		
		EchoJob echo = new EchoJob();
		echo.setText("Hello");
		echo.setName("Greeting");
		
		echo.run();
		
		ArooaSession session = new StandardArooaSession();
		
		Object silhouette = OddjobTestHelper.copy(
				new SilhouetteFactory().create(echo, session));
		
		assertTrue(silhouette instanceof Describeable);
		assertFalse(silhouette instanceof Stateful);
		assertFalse(silhouette instanceof Structural);
		assertFalse(silhouette instanceof Iconic);
		
		Map<String, String> description = new UniversalDescriber(
				new StandardArooaSession()).describe(silhouette);
		
		assertEquals("Hello", description.get("text"));
		
		assertEquals("Greeting", silhouette.toString());		
	}
	
	public void testStateful() throws IOException, ClassNotFoundException {
		
		FlagState flag = new FlagState();
		
		flag.run();
		
		ArooaSession session = new StandardArooaSession();
		
		Object silhouette = OddjobTestHelper.copy(
				new SilhouetteFactory().create(flag, session));
		
		assertTrue(silhouette instanceof Stateful);
		assertTrue(silhouette instanceof Iconic);
		
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(silhouette));
		assertEquals(IconHelper.COMPLETE, OddjobTestHelper.getIconId(silhouette));
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
		
		Object silhouette = OddjobTestHelper.copy(
				new SilhouetteFactory().create(sequential, session));
		
		assertTrue(silhouette instanceof Structural);
		
		Object[] children = OddjobTestHelper.getChildren((Structural) silhouette);
		
		assertEquals(1, children.length);
		
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(children[0]));
	}
	
	/**
	 * Test we don't try and include children not in our session. So no 
	 * nested Oddjobs or remote nodes.
	 * 
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void testNotOurChildren() throws IOException, ClassNotFoundException {
		
		SequentialJob sequential = new SequentialJob();
		
		FlagState flag = new FlagState();
		
		sequential.setJobs(0, flag);
		
		sequential.run();
		
		final ArooaSession session = new StandardArooaSession();
		
		Object silhouette = OddjobTestHelper.copy(
				new SilhouetteFactory().create(sequential, session));
		
		assertTrue(silhouette instanceof Structural);
		
		Object[] children = OddjobTestHelper.getChildren((Structural) silhouette);
		
		assertEquals(0, children.length);		
	}
}
