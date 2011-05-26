package org.oddjob.monitor.action;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.view.SwingFormFactory;
import org.oddjob.arooa.design.view.SwingFormView;
import org.oddjob.arooa.design.view.ValueDialog;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.monitor.model.ConfigContextInialiser;
import org.oddjob.monitor.model.MockExplorerContext;

public class AddJobActionTest extends TestCase {
	
	ConfigurationOwner configOwner;
	
	private class ParentContext extends MockExplorerContext {
		
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
	
	AddJobAction test = new AddJobAction();
	
	SwingFormView view;
	
	public void testAll() {
		
		String xml = 
			"<oddjob>" +
			"  <job>" +
			"    <sequential id='sequential'/>" +
			"  </job>" +
			"</oddjob>";
		
		config = new XMLConfiguration("XML", xml);
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(config);
		
		oddjob.run();
		
		Object sequentialJob = new OddjobLookup(oddjob).lookup("sequential");
		
		OurExplorerContext explorerContext = 
			new OurExplorerContext();		
		configOwner = oddjob;
		explorerContext.component = sequentialJob;
				
		test.setSelectedContext(explorerContext);
		test.prepare();
		
		assertTrue(test.isEnabled());
		
		Form form = test.form();
		
		assertNotNull(form);
		
		view = SwingFormFactory.create(form);
		
		assertNotNull(view);
	}
	
	public static void main(String... args) throws Exception {

		AddJobActionTest test = new AddJobActionTest();
		test.testAll();
		
		ValueDialog dialog = new ValueDialog(test.view.dialog());
		
		dialog.showDialog(null);
		
		if (!dialog.isChosen()) {
			return;
		}
		
		test.test.action();
		
		test.configOwner.provideConfigurationSession().save();
		

		System.out.println(test.config.getSavedXml());
	}
}
