/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.state.ParentState;
import org.oddjob.util.ThreadManager;

public class ExplorerContextImplTest extends TestCase {

	class OurModel extends MockExplorerModel {
		
		@Override
		public Oddjob getOddjob() {
			return new Oddjob();
		}
		
		@Override
		public ThreadManager getThreadManager() {
			return null;
		}
		
		@Override
		public ContextInitialiser[] getContextInitialisers() {
			return new ContextInitialiser[0];
		}
	}
	
	public void testChildComponent() {
		
		ExplorerContext test = new ExplorerContextImpl(new OurModel());
		
		Object child = new Object();
		
		ExplorerContext next = test.addChild(child);
		
		assertEquals(child, next.getThisComponent());
	}
	
	/**
	 * Test that a nested Oddjob is the component owner.
	 * @throws ArooaParseException 
	 *
	 */
	public void testComponentOwnerForNestedOddjob() throws ArooaParseException {
		Oddjob oddjob = new Oddjob();
		
		ExplorerModelImpl em = new ExplorerModelImpl(
				new OddjobSessionFactory().createSession());
		
		em.setOddjob(oddjob);
		
		ExplorerContext ec1 = new ExplorerContextImpl(em);
		
		Oddjob nestedOddjob = new Oddjob();
		ExplorerContext ec2 = ec1.addChild(nestedOddjob);
		
		assertEquals(nestedOddjob, 
				ec2.getValue(ConfigContextInialiser.CONFIG_OWNER));
	}
	
	
	/**
	 * Test getting the SessionLite for an Oddjob in different
	 * states.
	 * @throws ArooaParseException 
	 */
	public void testComponentOwner() throws ArooaParseException {
		
		Oddjob oddjob = new Oddjob();
		
		ExplorerModelImpl em = new ExplorerModelImpl(
				new OddjobSessionFactory().createSession());
		
		em.setOddjob(oddjob);
		
		ExplorerContext test = new ExplorerContextImpl(em);
		
		ConfigurationOwner configOwner = 
			(ConfigurationOwner) test.getValue(
					ConfigContextInialiser.CONFIG_OWNER);
		
		// Oddjob not run - no components.
		assertNull(configOwner.provideConfigurationSession());
		
		// run Oddjob on an empty configuration.
		oddjob.setConfiguration(new ArooaConfiguration() {
			public ConfigurationHandle parse(ArooaContext parentContext)
					throws ArooaParseException {
				return null;
			}
		});
		
		oddjob.run();
		
		assertNotNull(configOwner.provideConfigurationSession().dragPointFor(oddjob));
		
		oddjob.setConfiguration(new XMLConfiguration("TEST", "<oddjob/>"));
		
		oddjob.hardReset();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		oddjob.run();
		
		assertNotNull(configOwner.provideConfigurationSession().dragPointFor(oddjob));
	}
	
}
