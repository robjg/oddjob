package org.oddjob.monitor.model;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.monitor.context.ContextInitialiser;
import org.oddjob.monitor.context.ExplorerContext;
import org.oddjob.util.ThreadManager;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class ConfigContextSearchTest {

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
	
   @Test
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
		
		Diff diff = DiffBuilder.compare(copy)
				.withTest("<oddjob id='nested' name='Fred'/>").ignoreWhitespace()
				.build();

		assertFalse(diff.toString(), diff.hasDifferences());
		
		oddjob.destroy();
	}
	
}
