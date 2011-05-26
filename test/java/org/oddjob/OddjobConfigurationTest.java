package org.oddjob;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.MockConfigurationHandle;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.parsing.MockArooaContext;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.runtime.ConfigurationNode;
import org.oddjob.arooa.runtime.ConfigurationNodeListener;
import org.oddjob.arooa.runtime.MockConfigurationNode;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.EchoJob;
import org.xml.sax.SAXException;

/**
 * Test being a ConfigurationOwner.
 * 
 * @author rob
 *
 */
public class OddjobConfigurationTest extends XMLTestCase {

	private static final Logger logger = Logger.getLogger(OddjobConfigurationTest.class); 

	public void testCopy() throws SAXException, IOException {
		
		XMLUnit.setIgnoreWhitespace(true);
		
		String xml = 
			"<oddjob>" +
			"    <job>" +
			"        <echo id='simple' text='Hello'/>" + 
			"    </job>" + 
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.load();
		
		DragPoint dragPoint = oddjob.provideConfigurationSession().dragPointFor(oddjob);

		String result = dragPoint.copy();
	
		logger.debug("XML:" + result);
		
		assertXMLEqual(xml, result);
	}
	
	public void testPaste() throws SAXException, IOException, ArooaParseException {
		
		XMLUnit.setIgnoreWhitespace(true);
		
		String xml = 
			"<oddjob/>";
		
		XMLConfiguration config = new XMLConfiguration("TEST", xml);
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(config);
		oddjob.load();
		
		DragPoint dragPoint = oddjob.provideConfigurationSession().dragPointFor(oddjob);

		String paste = 
			"<echo id='simple' text='Hello'/>";
		
		DragTransaction trn = dragPoint.beginChange(ChangeHow.FRESH);
		dragPoint.paste(0, paste);
		trn.commit();
		
		oddjob.provideConfigurationSession().save();

		String expected = 
			"<oddjob>" +
			"    <job>" +
			"        <echo id='simple' text='Hello'/>" + 
			"    </job>" + 
			"</oddjob>";
		
		String result = config.getSavedXml();
		
		logger.debug("XML:" + result);
		
		assertXMLEqual(expected, result);
	}
	
	private class OurConfig implements ArooaConfiguration {
		public ConfigurationHandle parse(final ArooaContext parentContext)
				throws ArooaParseException {
			return new MockConfigurationHandle() {
				@Override
				public ArooaContext getDocumentContext() {
					return new MockArooaContext() {
						@Override
						public ConfigurationNode getConfigurationNode() {
							return new MockConfigurationNode() {
								@Override
								public void addNodeListener(
										ConfigurationNodeListener listener) {
									// used by modification stuff
								}
							};
						}
						
						@Override
						public ArooaContext getParent() {
							return parentContext;
						}
					};
				}
			};
		}
	}
	
	/** 
	 * Tracking down a failure to understand an 
	 * OddjobExplorer Test for the new action.
	 */
	public void testConfigurationSession() {
		
		Oddjob oddjob = new Oddjob();
		
		oddjob.setConfiguration(new OurConfig());
		
		oddjob.load();
		
		ArooaDescriptor descriptor = 
			oddjob.provideConfigurationSession().getArooaDescriptor();

		ArooaClass cl = descriptor.getElementMappings().mappingFor(
				new ArooaElement("echo"), 
				new InstantiationContext(ArooaType.COMPONENT, null));

		assertEquals(cl, new SimpleArooaClass(EchoJob.class));
		
	}
}
