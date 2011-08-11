package org.oddjob.monitor.action;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.view.SwingFormFactory;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.MockExplorerContext;
import org.oddjob.state.ParentState;

public class DesignInsideActionTest extends TestCase {

	class OurExplorerContext extends MockExplorerContext {

		Object object;
		
		@Override
		public Object getThisComponent() {
			return object;
		}
		
		@Override
		public ExplorerContext getParent() {
			return null;
		}
	}
	
	XMLConfiguration config;
	
	DesignInsideAction test = new DesignInsideAction();
	
	public void testBadRootConfig() throws Exception {
		
		config = new XMLConfiguration("TEST",
				"<wrongxml/>");
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(config);
		oddjob.run();
		
		assertEquals(ParentState.EXCEPTION, oddjob.lastStateEvent().getState());
		
		OurExplorerContext explorerContext = 
			new OurExplorerContext();		
		explorerContext.object = oddjob;
		
		test.setSelectedContext(explorerContext);
		test.prepare();
		
		assertTrue(test.isVisible());
		assertTrue(test.isEnabled());
		
		Form form = test.form();
		
		assertNotNull(form);
		
		test.setSelectedContext(null);
		
		assertFalse(test.isVisible());
		assertFalse(test.isEnabled());
	}

	public void testGoodRootConfig() throws Exception {
		
		config = new XMLConfiguration("TEST",
				"<oddjob/>");
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(config);
		oddjob.run();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OurExplorerContext explorerContext = 
			new OurExplorerContext();		
		explorerContext.object = oddjob;
		
		test.setSelectedContext(explorerContext);
		test.prepare();
		
		assertTrue(test.isVisible());
		assertTrue(test.isEnabled());
		
		Form form = test.form();
		
		assertNotNull(form);
		
		test.setSelectedContext(null);
		
		assertFalse(test.isVisible());
		assertFalse(test.isEnabled());
	}
	
	public void testNonConfigurationOwner() {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential id='sequential'/>" +
			" </job>" +
			"</oddjob>";
					
		config = new XMLConfiguration("TEST",
				xml);

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(config);
		oddjob.run();

		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OurExplorerContext explorerContext = 
			new OurExplorerContext();		
		explorerContext.object = new OddjobLookup(oddjob).lookup("sequential");
		
		test.setSelectedContext(explorerContext);
		test.prepare();
		
		assertFalse(test.isVisible());
		assertFalse(test.isEnabled());

		test.setSelectedContext(null);
		
		assertFalse(test.isVisible());
		assertFalse(test.isEnabled());
	}
	
	public void testNestedOddjobNoConfig() {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <oddjob id='nested'/>" +
			" </job>" +
			"</oddjob>";
					
		config = new XMLConfiguration("TEST",
				xml);

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(config);
		oddjob.load();

		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OurExplorerContext explorerContext = 
			new OurExplorerContext();		
		explorerContext.object = new OddjobLookup(oddjob).lookup("nested");
		
		test.setSelectedContext(explorerContext);
		test.prepare();
		
		assertTrue(test.isVisible());
		assertFalse(test.isEnabled());

		test.setSelectedContext(null);
		
		assertFalse(test.isVisible());
		assertFalse(test.isEnabled());
	}
	
	public void testNestedOddjob() {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <oddjob id='nested'>" +
			"   <configuration>" +
			"    <arooa:configuration xmlns:arooa='http://rgordon.co.uk/oddjob/arooa'>" +
			"     <xml>" +
			"      <xml>" +
			"       <oddjob/>" +
			"      </xml>" +
			"     </xml>" +
			"    </arooa:configuration>" +
			"   </configuration>" +
			"  </oddjob>" +
			" </job>" +
			"</oddjob>";
					
		config = new XMLConfiguration("TEST",
				xml);

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(config);
		oddjob.run();

		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		OurExplorerContext explorerContext = 
			new OurExplorerContext();		
		explorerContext.object = new OddjobLookup(oddjob).lookup("nested");
		
		test.setSelectedContext(explorerContext);
		
		assertTrue(test.isVisible());
		assertTrue(test.isEnabled());

		test.setSelectedContext(null);
		
		assertFalse(test.isVisible());
		assertFalse(test.isEnabled());

	}
	
	public static void main(String... args) throws Exception {
		
		final DesignInsideActionTest test = new DesignInsideActionTest();
		test.testNestedOddjob();
		
		Component view = SwingFormFactory.create(test.test.form()).dialog();
		
		JFrame frame = new JFrame();
		frame.getContentPane().add(view);
		frame.pack();
		frame.setVisible(true);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				try {
					test.test.action();
				} catch (Exception e1) {
					throw new RuntimeException(e1);
				}		
				// Only get this from the badRootConfig because
				// different ConfigurationSession used.
				System.out.println(test.config.getSavedXml());
			}
		});
					
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
}
