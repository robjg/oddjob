/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.view;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.design.actions.ConfigurableMenus;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.MockConfigurationOwner;
import org.oddjob.arooa.parsing.MockConfigurationSession;
import org.oddjob.arooa.standard.StandardArooaDescriptor;
import org.oddjob.monitor.actions.ResourceActionProvider;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ConfigContextInialiser;
import org.oddjob.monitor.model.MockExplorerContext;
import org.oddjob.util.MockThreadManager;
import org.oddjob.util.ThreadManager;

/**
 * 
 */
public class ActionModelTest extends TestCase {

	private static final Logger logger = Logger.getLogger(ActionModelTest.class); 
	
	class OurSessionLite extends MockConfigurationSession {
		
		ArooaDescriptor descriptor = new StandardArooaDescriptor();
		
		@Override
		public ArooaDescriptor getArooaDescriptor() {
			return descriptor;
		}
		
		@Override
		public DragPoint dragPointFor(Object component) {
			return null;
		}
	}
	
	class ParentContext extends MockExplorerContext {
		
		ConfigurationSession session = new OurSessionLite();
		
		
		@Override
		public Object getValue(String key) {
			if (ConfigContextInialiser.CONFIG_OWNER.equals(key)) {
				return new MockConfigurationOwner() {
					public ConfigurationSession provideConfigurationSession() {
						return session;
					}
				};
			}
			throw new RuntimeException("Unexpected: " + key);
		}
	}
	
	class OurExplorerContext extends MockExplorerContext {
		
		Object thisComponent;
		
		@Override
		public Object getThisComponent() {
			return thisComponent;
		}
		
		@Override
		public ThreadManager getThreadManager() {
			return new MockThreadManager() {
				
			};
		}
				
		@Override
		public ExplorerContext getParent() {
			return new ParentContext();
		}
	}
	
	
	/**
	 * Test Action Menus are created OK and how they are
	 * enabled for an Object.
	 * @throws ArooaParseException 
	 */
	public void testSelectObject() throws ArooaParseException {
		
		Object object = new Object();

		OurExplorerContext econ = new OurExplorerContext();
		econ.thisComponent = object;
		
		final ExplorerJobActions test = new ExplorerJobActions(
				new ResourceActionProvider(
						new OddjobSessionFactory(
								).createSession()).getExplorerActions());
		
		ConfigurableMenus menus = new ConfigurableMenus();
		
		test.contributeTo(menus);
		
		test.setSelectedContext(econ);
		test.prepare();
		
		JMenu menu = menus.getJMenuBar()[0];
				
		Component[] components = menu.getMenuComponents();

		List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
		
		for (Component component: components) {
			if (component instanceof JMenuItem) {
				JMenuItem menuItem = (JMenuItem) component;
				logger.debug(menuItem.getText() + " " + menuItem.isEnabled());
				menuItems.add(menuItem);
			}
			else if (component instanceof JSeparator){
				logger.debug("Separator");
			}
			else {
				logger.debug(component.getClass());
			}
		}

		assertEquals("Load", menuItems.get(0).getText());
		assertFalse(menuItems.get(0).isEnabled());
		assertFalse(menuItems.get(0).isVisible());
		assertEquals("Unload", menuItems.get(1).getText());
		assertFalse(menuItems.get(1).isEnabled());
		assertFalse(menuItems.get(1).isVisible());
		assertEquals("Run", menuItems.get(2).getText());
		assertFalse(menuItems.get(2).isEnabled());
		assertEquals("Soft Reset", menuItems.get(3).getText());
		assertFalse(menuItems.get(3).isEnabled());
		assertEquals("Hard Reset", menuItems.get(4).getText());
		assertFalse(menuItems.get(4).isEnabled());
		assertEquals("Stop", menuItems.get(5).getText());
		assertFalse(menuItems.get(5).isEnabled());
		assertEquals("Set Property", menuItems.get(6).getText());
		assertTrue(menuItems.get(6).isEnabled());
		assertEquals("Designer", menuItems.get(7).getText());
		assertFalse(menuItems.get(7).isEnabled());
		assertEquals("Design Inside", menuItems.get(8).getText());
		assertFalse(menuItems.get(8).isEnabled());
		assertFalse(menuItems.get(8).isVisible());
		assertEquals("Add Job", menuItems.get(9).getText());
		assertFalse(menuItems.get(9).isEnabled());
		assertFalse(menuItems.get(9).isVisible());
	}

	class RootContext extends MockExplorerContext {
		
		Oddjob thisComponent;
		
		@Override
		public Object getThisComponent() {
			return thisComponent;
		}
		
		@Override
		public ThreadManager getThreadManager() {
			return new MockThreadManager() {
				
			};
		}
		
		@Override
		public Object getValue(String key) {
			if (ConfigContextInialiser.CONFIG_OWNER.equals(key)) {
				return thisComponent;
			}
			throw new RuntimeException("Unexpected: " + key);
		}
		
		@Override
		public ExplorerContext getParent() {
			return null;
		}
	}
	
	/**
	 * Test Action Menus are created OK and how they are
	 * enabled for an top level Oddjob.
	 * 
	 * @throws ArooaParseException 
	 */
	public void testSelectOddjob() throws ArooaParseException {
		
		Oddjob oj = new Oddjob();

		RootContext econ = new RootContext();
		econ.thisComponent = oj;

		final ExplorerJobActions test = new ExplorerJobActions(
				new ResourceActionProvider(
						new OddjobSessionFactory(
								).createSession()).getExplorerActions());

		ConfigurableMenus menus = new ConfigurableMenus();
		
		test.contributeTo(menus);
		test.setSelectedContext(econ);
		test.prepare();
		
		JMenu menu = menus.getJMenuBar()[0];

		Component[] components = menu.getMenuComponents();

		List<JMenuItem> menuItems = new ArrayList<JMenuItem>();
		
		for (Component component: components) {
			if (component instanceof JMenuItem) {
				JMenuItem menuItem = (JMenuItem) component;
				logger.debug(menuItem.getText() + " " + menuItem.isEnabled());
				menuItems.add(menuItem);
			}
			else if (component instanceof JSeparator){
				logger.debug("Separator");
			}
			else {
				logger.debug(component.getClass());
			}
		}

		assertEquals("Load", menuItems.get(0).getText());
		assertTrue(menuItems.get(0).isEnabled());
		assertTrue(menuItems.get(0).isVisible());
		assertEquals("Unload", menuItems.get(1).getText());
		assertFalse(menuItems.get(1).isEnabled());
		assertTrue(menuItems.get(1).isVisible());
		assertEquals("Run", menuItems.get(2).getText());
		assertTrue(menuItems.get(2).isEnabled());
		assertTrue(menuItems.get(2).isVisible());
		assertEquals("Soft Reset", menuItems.get(3).getText());
		assertTrue(menuItems.get(3).isEnabled());
		assertTrue(menuItems.get(3).isVisible());
		assertEquals("Hard Reset", menuItems.get(4).getText());
		assertTrue(menuItems.get(4).isEnabled());
		assertTrue(menuItems.get(4).isVisible());
		assertEquals("Stop", menuItems.get(5).getText());
		assertFalse(menuItems.get(5).isEnabled());
		assertTrue(menuItems.get(5).isVisible());
		assertEquals("Set Property", menuItems.get(6).getText());
		assertFalse(menuItems.get(6).isEnabled());
		assertFalse(menuItems.get(6).isVisible());
		assertEquals("Designer", menuItems.get(7).getText());
		assertFalse(menuItems.get(7).isEnabled());
		assertFalse(menuItems.get(7).isVisible());
		assertEquals("Design Inside", menuItems.get(8).getText());
		assertFalse(menuItems.get(8).isEnabled());
		assertTrue(menuItems.get(8).isVisible());
		assertEquals("Add Job", menuItems.get(9).getText());
		assertFalse(menuItems.get(9).isEnabled());
		assertFalse(menuItems.get(9).isVisible());
		
	}
	
}
