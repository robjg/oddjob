package org.oddjob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.ComponentTrinity;
import org.oddjob.arooa.ConfigurationHandle;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.LinkedDescriptor;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.CutAndPasteSupport;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.registry.ComponentPool;
import org.oddjob.arooa.registry.MockComponentPool;
import org.oddjob.arooa.standard.ExtendedTools;
import org.oddjob.arooa.standard.StandardArooaDescriptor;
import org.oddjob.arooa.standard.StandardArooaParser;
import org.oddjob.arooa.standard.StandardTools;
import org.oddjob.arooa.xml.XMLArooaParser;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

/**
 * Tests for Cut And Paste.
 * 
 * @author rob
 *
 */
public class OddjobTest2 extends TestCase {

	class OurStructuralListener implements StructuralListener {
		List<Object> children = new ArrayList<Object>();
		
		public void childAdded(StructuralEvent event) {
			children.add(event.getChild());
		}
		public void childRemoved(StructuralEvent event) {
			children.add(null);
		}
		
		
	}
	
	
	public void testBadSave() throws ArooaParseException, ArooaConversionException {

		OurStructuralListener structure = new OurStructuralListener();
		
		String xml = 
			"<oddjob>" +
			" <job>" +
			"  <sequence id='sequence' from='1'/>" +
			" </job>" +
			"</oddjob>";
			
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.addStructuralListener(structure);
		
		oddjob.run();

		Integer i = new OddjobLookup(
				oddjob).lookup("sequence.current", Integer.class);
		
		assertEquals(new Integer(1), i);
		
		Object sequence = new OddjobLookup(oddjob).lookup("sequence");
		
		
		DragPoint point = oddjob.provideConfigurationSession().dragPointFor(
				sequence);
		
		XMLArooaParser xmlParser = new XMLArooaParser();
		
		ConfigurationHandle handle = xmlParser.parse(
				point);

		ArooaContext xmlDoc = handle.getDocumentContext();
		
		CutAndPasteSupport.replace(xmlDoc.getParent(), xmlDoc, 
				new XMLConfiguration("Replace", "<rubbish/>"));		
		
		try {
			handle.save();
			fail("Should fail.");
		} catch (Exception e) {
			// expected.
		}
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		Object sequence2 = lookup.lookup("sequence");
		Integer i2 = lookup.lookup("sequence.current", Integer.class);
		assertEquals(null, i2);
		
		assertEquals(structure.children.size(), 3);
		assertEquals(sequence, structure.children.get(0));
		assertEquals(null, structure.children.get(1));
		assertEquals(sequence2, structure.children.get(2));
		
		oddjob.run();
				
		Integer i3 = lookup.lookup("sequence.current", Integer.class);
		assertEquals(new Integer(1), i3);
		
		oddjob.destroy();
	}

	class OurComponentPool extends MockComponentPool {
		
		Map<String, ArooaContext> contexts =
			new HashMap<String, ArooaContext>();
		
		List<Object> components = 
			new ArrayList<Object>();
		
		List<String> actions = 
			new ArrayList<String>();
		
		@Override
		public void registerComponent(ComponentTrinity trinity, String id) {
			components.add(trinity.getTheProxy());
			contexts.put(id, trinity.getTheContext());
			actions.add("A");
		}
				
		@Override
		public void remove(Object component) {
			components.add(component);
			actions.add("R");
		}
		
		@Override
		public void configure(Object component) {
		}
	}

	class OurSession extends MockArooaSession {
		
		OurComponentPool pool = new OurComponentPool();
		ArooaDescriptor descriptor;
		ArooaTools tools;
		
		public OurSession(ArooaDescriptor descriptor) {
			this.descriptor = descriptor;
			this.tools = new ExtendedTools(new StandardTools(),
					descriptor);
		}
		
		@Override
		public ArooaDescriptor getArooaDescriptor() {
			return descriptor;
		}
		
		@Override
		public ComponentPool getComponentPool() {
			return pool;
		}
		
		@Override
		public ArooaTools getTools() {
			return tools;
		}
		
		@Override
		public ComponentPersister getComponentPersister() {
			return null;
		}

		@Override
		public ComponentProxyResolver getComponentProxyResolver() {
			return null;
		}
		
	}
	
	public void testBadSave2() throws ArooaParseException {

    	ArooaDescriptor descriptor = new OddjobDescriptorFactory(
    		).createDescriptor(null);
    	
		OurSession session = new OurSession(
				new LinkedDescriptor(descriptor, 
						new StandardArooaDescriptor()));
		
		String xml = 
			"<oddjob id='oj'>" +
			" <job>" +
			"  <sequence id='sequence'/>" +
			" </job>" +
			"</oddjob>";
			
		OddjobServices services = new MockOddjobServices() {
			@Override
			public ClassLoader getClassLoader() {
				return getClass().getClassLoader();
			}
		};
		
		Oddjob.OddjobRoot root = new Oddjob().new OddjobRoot(
				services);

		StandardArooaParser parser = new StandardArooaParser(root, session);
		parser.parse(new XMLConfiguration("TEST", xml));
		
		
		ArooaContext context = session.pool.contexts.get("sequence");
		
		XMLArooaParser xmlParser = new XMLArooaParser();
		
		ConfigurationHandle handle = xmlParser.parse(
				context.getConfigurationNode());

		ArooaContext xmlDoc = handle.getDocumentContext();
		
		CutAndPasteSupport.replace(xmlDoc.getParent(), xmlDoc, 
				new XMLConfiguration("Replace", "<rubbish/>"));		
		
		try {
			handle.save();
			fail("Should fail.");
		} catch (Exception e) {
			// expected.
		}
		
		session.pool.contexts.get("oj").getRuntime().destroy();

		assertEquals(6, session.pool.actions.size());
		
		assertEquals("A", session.pool.actions.get(0));
		assertEquals("A", session.pool.actions.get(1));
		assertEquals("R", session.pool.actions.get(2));
		assertEquals("A", session.pool.actions.get(3));
		assertEquals("R", session.pool.actions.get(4));
		assertEquals("R", session.pool.actions.get(5));
		
		assertEquals(session.pool.components.get(0), 
			session.pool.components.get(2) );
		
		assertEquals(session.pool.components.get(3),
			session.pool.components.get(4) );
		
		assertTrue(session.pool.components.get(1) != 
			session.pool.components.get(3) );
	}
	
}
