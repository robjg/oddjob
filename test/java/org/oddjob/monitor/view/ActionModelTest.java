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
import org.oddjob.monitor.actions.ExplorerAction;
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
	
	private class OurSessionLite extends MockConfigurationSession {
		
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
	
	private class ParentContext extends MockExplorerContext {
		
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
	
	private class OurExplorerContext extends MockExplorerContext {
		
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
		
		@Override
		public Object getValue(String key) {
			return null;
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
		
		ExplorerAction[] actions = new ResourceActionProvider(
				new OddjobSessionFactory(
				).createSession()).getExplorerActions(); 
		
		final ExplorerJobActions test = new ExplorerJobActions(
				actions);
		
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

		int i = 0;
		
		assertEquals("Load", menuItems.get(i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		assertFalse(menuItems.get(i).isVisible());

		assertEquals("Unload", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		assertFalse(menuItems.get(i).isVisible());
		
		assertEquals("Start", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		
		assertEquals("Soft Reset", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());

		assertEquals("Hard Reset", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());

		assertEquals("Stop", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());

		assertEquals("Force", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());

		assertEquals("Execute", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		assertFalse(menuItems.get(i).isVisible());
		
		assertEquals("Set Property", menuItems.get(++i).getText());
		assertTrue(menuItems.get(i).isEnabled());

		assertEquals("Designer", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());

		assertEquals("Design Inside", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		assertFalse(menuItems.get(i).isVisible());

		assertEquals("Add Job", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		assertFalse(menuItems.get(i).isVisible());
	}

	private class RootContext extends MockExplorerContext {
		
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

		ExplorerAction[] actions = new ResourceActionProvider(
				new OddjobSessionFactory(
				).createSession()).getExplorerActions(); 
		
		final ExplorerJobActions test = new ExplorerJobActions(
				actions);

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

		int i = 0;
		assertEquals("Load", menuItems.get(i).getText());
		assertTrue(menuItems.get(i).isEnabled());
		assertTrue(menuItems.get(i).isVisible());
		
		assertEquals("Unload", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		assertTrue(menuItems.get(i).isVisible());
		
		assertEquals("Start", menuItems.get(++i).getText());
		assertTrue(menuItems.get(i).isEnabled());
		assertTrue(menuItems.get(i).isVisible());
		
		assertEquals("Soft Reset", menuItems.get(++i).getText());
		assertTrue(menuItems.get(i).isEnabled());
		assertTrue(menuItems.get(i).isVisible());
		
		assertEquals("Hard Reset", menuItems.get(++i).getText());
		assertTrue(menuItems.get(i).isEnabled());
		assertTrue(menuItems.get(i).isVisible());
		
		assertEquals("Stop", menuItems.get(++i).getText());
		assertTrue(menuItems.get(i).isEnabled());
		assertTrue(menuItems.get(i).isVisible());
		
		assertEquals("Force", menuItems.get(++i).getText());
		assertTrue(menuItems.get(i).isEnabled());
		assertTrue(menuItems.get(i).isVisible());
		
		assertEquals("Execute", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		assertFalse(menuItems.get(i).isVisible());
		
		assertEquals("Set Property", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		assertFalse(menuItems.get(i).isVisible());
		
		assertEquals("Designer", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		assertFalse(menuItems.get(i).isVisible());
		
		assertEquals("Design Inside", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		assertTrue(menuItems.get(i).isVisible());
		
		assertEquals("Add Job", menuItems.get(++i).getText());
		assertFalse(menuItems.get(i).isEnabled());
		assertFalse(menuItems.get(i).isVisible());
		
	}
	
}
