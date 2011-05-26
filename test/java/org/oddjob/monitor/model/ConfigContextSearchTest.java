package org.oddjob.monitor.model;

import java.io.IOException;

import org.custommonkey.xmlunit.XMLTestCase;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.util.ThreadManager;
import org.xml.sax.SAXException;

public class ConfigContextSearchTest extends XMLTestCase{

	class OurModel extends MockExplorerModel {
		
		Oddjob oddjob;
		
		@Override
		public Oddjob getOddjob() {
			return oddjob;
		}
		
		@Override
		public ContextInitialiser[] getContextInitialisers() {
			return new ContextInitialiser[] {
					new ConfigContextInialiser(this)
			};
		}
		
		@Override
		public ThreadManager getThreadManager() {
			return null;
		}
	}
	
	public void testOddjobDragPoint() throws SAXException, IOException {
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <oddjob id='nested' name='Fred'/>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.load();
		
		OurModel model = new OurModel();
		model.oddjob = oddjob;
		
		ExplorerContext context = new ExplorerContextImpl(model);
		
		Object nested = new OddjobLookup(oddjob).lookup("nested");
		
		ExplorerContext nestedContext = context.addChild(nested);
		
		DragPoint result = 
			new ConfigContextSearch().dragPointFor(nestedContext);
		
		assertNotNull(result);
		
		String copy = result.copy();
		
		assertXMLEqual("<oddjob id='nested' name='Fred'/>", copy);

		oddjob.destroy();
	}
	
}
