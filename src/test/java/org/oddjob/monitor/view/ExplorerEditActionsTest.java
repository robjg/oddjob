/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.view;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.design.actions.ConfigurableMenus;
import org.oddjob.arooa.design.designer.MenuProvider;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.MockConfigurationOwner;
import org.oddjob.arooa.parsing.MockConfigurationSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ConfigContextInitialiser;
import org.oddjob.monitor.model.MockExplorerContext;
import org.oddjob.util.MockThreadManager;
import org.oddjob.util.ThreadManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */
public class ExplorerEditActionsTest extends OjTestCase {

	private static final Logger logger = LoggerFactory.getLogger(ExplorerEditActionsTest.class); 
	
	class ParentContext extends MockExplorerContext {
		
		ConfigurationSession session;
		
		
		@Override
		public Object getValue(String key) {
			if (ConfigContextInitialiser.CONFIG_OWNER.equals(key)) {
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
		
		ParentContext parent = new ParentContext();
		
		@Override
		public Object getThisComponent() {
			return thisComponent;
		}
		@Override
		public ExplorerContext getParent() {
			return parent;
		}
	}
	
	class NoDragPointSession extends MockConfigurationSession {
		
		@Override
		public DragPoint dragPointFor(Object component) {
			return null;
		}
	}
		
	/**
	 * Test Edit Menus for no drag point. 
	 * 
	 * @throws ArooaParseException 
	 */
   @Test
	public void testNoDragPoint() throws ArooaParseException {
		
		Object object = new Object();

		OurExplorerContext econ = new OurExplorerContext();
		econ.parent.session = new NoDragPointSession();
		econ.thisComponent = object;
		
		final ExplorerEditActions test = new ExplorerEditActions();
		
		ConfigurableMenus menus = new ConfigurableMenus();
		
		test.contributeTo(menus);
		
		test.setSelectedContext(econ);
		test.prepare();
		
		List<JMenuItem> menuItems = extractMenuItems(menus);
		
		assertEquals("Cut", menuItems.get(0).getText());
		assertFalse(menuItems.get(0).isEnabled());
		assertEquals("Copy", menuItems.get(1).getText());
		assertFalse(menuItems.get(1).isEnabled());
		assertEquals("Paste", menuItems.get(2).getText());
		assertFalse(menuItems.get(2).isEnabled());
		assertEquals("Delete", menuItems.get(3).getText());
		assertFalse(menuItems.get(3).isEnabled());
	}
	
	/**
	 * Test Edit Actions for a no child job.
	 * 
	 * @throws ArooaParseException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
   @Test
	public void testNormalSelction() throws ArooaParseException, SAXException, IOException {

		String xml = 
			"<oddjob>" +
			"  <job>" +
			"   <echo id='simple'>Hello</echo>" +
			"  </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.load();
		
		Object object = new OddjobLookup(oddjob).lookup("simple");

		assertNotNull(object);
		
		OurExplorerContext econ = new OurExplorerContext();
		econ.parent.session = oddjob.provideConfigurationSession();
		econ.thisComponent = object;
		
		final ExplorerEditActions test = new ExplorerEditActions();
		
		ConfigurableMenus menus = new ConfigurableMenus();
		
		test.contributeTo(menus);
		
		test.setSelectedContext(econ);
		test.prepare();
		
		List<JMenuItem> menuItems = extractMenuItems(menus);

		assertEquals("Cut", menuItems.get(0).getText());
		assertTrue(menuItems.get(0).isEnabled());
		assertEquals("Copy", menuItems.get(1).getText());
		assertTrue(menuItems.get(1).isEnabled());
		assertEquals("Paste", menuItems.get(2).getText());
		assertFalse(menuItems.get(2).isEnabled());
		assertEquals("Delete", menuItems.get(3).getText());
		assertTrue(menuItems.get(3).isEnabled());		
	}

	/**
	 * Test for nested Oddjob - should be the same as above...
	 * 
	 * @throws ArooaParseException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
   @Test
	public void testNestedOddjob() throws ArooaParseException {

		String xml = 
			"<oddjob>" +
			"  <job>" +
			"   <oddjob id='simple'/>" +
			"  </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.load();
		
		Object object = new OddjobLookup(oddjob).lookup("simple");

		assertNotNull(object);
		
		OurExplorerContext econ = new OurExplorerContext();
		econ.parent.session = oddjob.provideConfigurationSession();
		econ.thisComponent = object;
		
		final ExplorerEditActions test = new ExplorerEditActions();
		
		ConfigurableMenus menus = new ConfigurableMenus();
		
		test.contributeTo(menus);
		
		test.setSelectedContext(econ);
		test.prepare();
		
		List<JMenuItem> menuItems = extractMenuItems(menus);

		assertEquals("Cut", menuItems.get(0).getText());
		assertTrue(menuItems.get(0).isEnabled());
		assertEquals("Copy", menuItems.get(1).getText());
		assertTrue(menuItems.get(1).isEnabled());
		assertEquals("Paste", menuItems.get(2).getText());
		assertFalse(menuItems.get(2).isEnabled());
		assertEquals("Delete", menuItems.get(3).getText());
		assertTrue(menuItems.get(3).isEnabled());		
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
			if (ConfigContextInitialiser.CONFIG_OWNER.equals(key)) {
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
   @Test
	public void testSelectOddjob() throws ArooaParseException {
		
		Oddjob oj = new Oddjob();

		RootContext econ = new RootContext();
		econ.thisComponent = oj;

		final ExplorerEditActions test = new ExplorerEditActions();

		ConfigurableMenus menus = new ConfigurableMenus();
		
		test.contributeTo(menus);
		test.setSelectedContext(econ);
		test.prepare();
		
		List<JMenuItem> menuItems = extractMenuItems(menus);
		
		assertEquals("Cut", menuItems.get(0).getText());
		assertFalse(menuItems.get(0).isEnabled());
		assertTrue(menuItems.get(0).isVisible());
		assertEquals("Copy", menuItems.get(1).getText());
		assertFalse(menuItems.get(1).isEnabled());
		assertTrue(menuItems.get(1).isVisible());
		assertEquals("Paste", menuItems.get(2).getText());
		assertFalse(menuItems.get(2).isEnabled());
		assertTrue(menuItems.get(2).isVisible());
		assertEquals("Delete", menuItems.get(3).getText());
		assertFalse(menuItems.get(3).isEnabled());
		assertTrue(menuItems.get(3).isVisible());
		
		oj.setConfiguration(new XMLConfiguration(
				"XML", "<oddjob/>"));

		oj.load();
		
		test.setSelectedContext(econ);
		test.prepare();
		
		assertEquals("Cut", menuItems.get(0).getText());
		assertFalse(menuItems.get(0).isEnabled());
		assertTrue(menuItems.get(0).isVisible());
		assertEquals("Copy", menuItems.get(1).getText());
		assertTrue(menuItems.get(1).isEnabled());
		assertTrue(menuItems.get(1).isVisible());
		assertEquals("Paste", menuItems.get(2).getText());
		assertTrue(menuItems.get(2).isEnabled());
		assertTrue(menuItems.get(2).isVisible());
		assertEquals("Delete", menuItems.get(3).getText());
		assertFalse(menuItems.get(3).isEnabled());
		assertTrue(menuItems.get(3).isVisible());
	}
	
	private static List<JMenuItem> extractMenuItems(MenuProvider menus) {
		
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
				logger.debug("Component class " + component.getClass());
			}
		}

		return menuItems;
	}
}
