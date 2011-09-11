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
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ConfigContextInialiser;
import org.oddjob.monitor.model.MockExplorerContext;
import org.oddjob.state.ParentState;

public class DesignerActionTest extends TestCase {

	private class RootContext extends MockExplorerContext {

		@Override
		public ExplorerContext getParent() {
			return null;
		}
	}
	
	public void testRoot() {
		
		DesignerAction test = new DesignerAction();
		
		test.setSelectedContext(new RootContext());
		test.prepare();
		
		assertFalse(test.isVisible());
		assertFalse(test.isEnabled());
		
		test.setSelectedContext(null);
		
		assertFalse(test.isVisible());
		assertFalse(test.isEnabled());
		
		test.setSelectedContext(null);		
	}

	private class ParentContext extends MockExplorerContext {
		
		ConfigurationOwner configOwner;
		
		@Override
		public Object getValue(String key) {
			assertEquals(ConfigContextInialiser.CONFIG_OWNER, key);
			return configOwner;
		}
	}
	
	private class OurExplorerContext extends MockExplorerContext {
		
		Object component;

		ParentContext parent = new ParentContext();
		
		@Override
		public Object getThisComponent() {
			return component;
		}
		
		@Override
		public ExplorerContext getParent() {
			return parent;
		}
	}

	XMLConfiguration config;
	
	DesignerAction test = new DesignerAction();
	
	
	public void testGoodConfig() {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequential id='sequential'/>" +
			" </job>" +
			"</oddjob>";
					
		config = new XMLConfiguration("TEST2", xml);
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(config);
		
		oddjob.run();
		
		assertEquals(ParentState.READY, oddjob.lastStateEvent().getState());
		
		Object sequentialJob = new OddjobLookup(oddjob).lookup("sequential");
		
		OurExplorerContext explorerContext = 
			new OurExplorerContext();		
		explorerContext.parent.configOwner = oddjob;
		explorerContext.component = sequentialJob;
				
		test.setSelectedContext(explorerContext);
		test.prepare();
		
		assertTrue(test.isEnabled());
		
		Form form = test.form();
		
		assertNotNull(form);
	}

	public static void main(String... args) throws Exception {
		
		final DesignerActionTest test = new DesignerActionTest();
		test.testGoodConfig();
		
		test.config.setSaveHandler(new XMLConfiguration.SaveHandler() {
			
			@Override
			public void acceptXML(String xml) {

				System.out.println(xml);
			}
		});
		
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
			}
		});
					
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}
	
}

