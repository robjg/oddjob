package org.oddjob.jobs.structural;
import org.junit.Before;

import org.junit.Test;

import java.util.Arrays;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.life.ArooaContextAware;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.LinkedBeanRegistry;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.SimpleJob;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;

public class ForEachPsudoComponentPoolTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(
			ForEachPsudoComponentPoolTest.class);
	
   @Before
   public void setUp() throws Exception {

		
		logger.info("-----------------------------------  " + 
				getName() + "  --------------------------------");
	}
	
	public static class ContextCatcher extends SimpleJob
	implements ArooaContextAware {
		ArooaContext context;
		
		@Override
		public void setArooaContext(ArooaContext context) {
			this.context = context;
		}
		
		@Override
		protected int execute() throws Throwable {
			return 0;
		}
	}
	
	/**
	 * Tracking down a problem where duplicate ids are reported for components
	 * with an id in the foreach configuration.
	 * 
	 * @throws ArooaParseException
	 */
   @Test
	public void testSessionsAndRegistriesUsed() throws ArooaParseException {
		
		String xml = 
			"<foreach id='foreach'>" +
			" <job>" +
			"  <bean class='" + ContextCatcher.class.getName() + "'" +
					" id='fruit'/>" +
			" </job>" +
			"</foreach>";
		
		ForEachJob test = new ForEachJob();
		test.setArooaSession(new OddjobSessionFactory().createSession());
		test.setConfiguration(new XMLConfiguration("XML", xml));
		test.setValues(Arrays.asList("apple", "orange"));
		
		test.run();
		
		assertEquals(ParentState.COMPLETE, test.lastStateEvent().getState());
		
		Object[] children = OddjobTestHelper.getChildren(test);
		
		ContextCatcher job1 = (ContextCatcher) children[0];
		ContextCatcher job2 = (ContextCatcher) children[1];
		
		ArooaSession creationSession1 = job1.context.getSession();
		ArooaSession creationSession2 = job2.context.getSession();
		
		assertEquals(ForEachJob.RegistryOverrideSession.class,
				creationSession1.getClass());
		assertEquals(false, creationSession1 == creationSession2);
		
		BeanRegistry registry1 = creationSession1.getBeanRegistry();
		
		assertEquals(LinkedBeanRegistry.class, registry1.getClass());
		
		BeanRegistry registry2 = creationSession2.getBeanRegistry();
		
		assertEquals(LinkedBeanRegistry.class, registry2.getClass());
		
		assertEquals(false, registry1 == registry2);
		
		assertEquals(job1, registry1.lookup("fruit"));
		assertEquals(job2, registry2.lookup("fruit"));
	}

	
	
	private static class OurContext extends MockArooaContext {
		
		private final ArooaSession session;
		
		public OurContext(ArooaSession session) {
			this.session = session;
		}
		
		@Override
		public ArooaSession getSession() {
			return session;
		}
	}
	
   @Test
	public void testAddSameIdInTwoPoolsDoesntWarn() {
		

		ArooaSession baseSession = new StandardArooaSession();
		
		BeanRegistry registry1 = new LinkedBeanRegistry(baseSession);
		BeanRegistry registry2 = new LinkedBeanRegistry(baseSession);
		
		ArooaSession session1 = new ForEachJob.RegistryOverrideSession(
				baseSession, registry1);
		
		ArooaSession session2 = new ForEachJob.RegistryOverrideSession(
				baseSession, registry2);
		
		ComponentPool existingPool1 = session1.getComponentPool();;
		
		ComponentPool existingPool2 = session2.getComponentPool();;
		
		ForEachJob.PseudoComponentPool test1 = 
				new ForEachJob.PseudoComponentPool(existingPool1);
		
		ForEachJob.PseudoComponentPool test2 = 
				new ForEachJob.PseudoComponentPool(existingPool2);
		
		Object component1 = new Object();
		Object component2 = new Object();
		
		test1.registerComponent(new ComponentTrinity(
				component1, component1, new OurContext(session1)), "fruit");
		
		test2.registerComponent(new ComponentTrinity(
				component2, component2, new OurContext(session2)), "fruit");
		
		assertEquals(null, existingPool1.trinityForId("fruit"));
		assertEquals(null, existingPool2.trinityForId("fruit"));
		
	}
	
}
