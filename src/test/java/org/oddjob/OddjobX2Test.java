package org.oddjob;

import org.junit.Test;
import org.oddjob.arooa.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.LinkedDescriptor;
import org.oddjob.arooa.life.ComponentPersister;
import org.oddjob.arooa.life.ComponentProxyResolver;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ConfigurationSession;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Tests for Cut And Paste.
 * 
 * @author rob
 *
 */
public class OddjobX2Test {

    private static final Logger logger =
            LoggerFactory.getLogger(OddjobX2Test.class);

	class OurStructuralListener implements StructuralListener {
		List<Object> children = new ArrayList<>();
		
		public void childAdded(StructuralEvent event) {
			children.add(event.getChild());
		}
		public void childRemoved(StructuralEvent event) {
			children.add(null);
		}
		
		
	}
	
	@Test
	public void testBadSave() throws ArooaParseException, ArooaConversionException {

		OurStructuralListener structure = new OurStructuralListener();
		
		String xml = 
			"<oddjob id='this'>" +
			" <job>" +
			"  <sequence id='sequence' from='1'/>" +
			" </job>" +
			"</oddjob>";
			
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		oddjob.addStructuralListener(structure);
		
		oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

		Integer i = lookup.lookup("sequence.current", Integer.class);

		// Configuration loaded and all is good.
		assertEquals(new Integer(1), i);

		// Parse sequence to XML.
		Object sequence = lookup.lookup("sequence");

		ConfigurationSession configurationSession = oddjob.provideConfigurationSession();

		DragPoint point = configurationSession.dragPointFor(
				sequence);

		XMLArooaParser xmlParser = new XMLArooaParser(configurationSession.getArooaDescriptor());
		
		ConfigurationHandle<ArooaContext> handle = xmlParser.parse(
				point);

		ArooaContext xmlDoc = handle.getDocumentContext();

		// And replace the XML with rubbish
		CutAndPasteSupport.replace(xmlDoc.getParent(), xmlDoc, 
				new XMLConfiguration("Replace", "<rubbish/>"));		

		Object root = lookup.lookup("this");
        logger.info("Before save:\n{}",
                    oddjob.provideConfigurationSession().dragPointFor(
                            root).copy());

		try {
			handle.save();
			fail("Should fail.");
		} catch (Exception e) {
			// expected.
		}

        logger.info("After save:\n{}",
                    oddjob.provideConfigurationSession().dragPointFor(
                            root).copy());

		Object sequence2 = lookup.lookup("sequence");
		Integer i2 = lookup.lookup("sequence.current", Integer.class);

		// Sequence has been reset because it has been replace with a new on
        // during save rollback.
        assertNull("i2", i2);


		assertEquals("3 events expected",
                     structure.children.size(), 3);
		assertEquals("First event",
                     sequence, structure.children.get(0));
        assertNull("Second event",
                   structure.children.get(1));
		assertEquals("Third event",
                     sequence2, structure.children.get(2));
		
		oddjob.run();
				
		Integer i3 = lookup.lookup("sequence.current", Integer.class);

		// Sequence starts at 0 again and is now 1.
		assertEquals(new Integer(1), i3);
		
		oddjob.destroy();
	}

	class OurComponentPool extends MockComponentPool {
		
		Map<String, ArooaContext> contexts =
                new HashMap<>();
		
		List<Object> components =
                new ArrayList<>();
		
		List<String> actions =
                new ArrayList<>();
		
		@Override
		public String registerComponent(ComponentTrinity trinity, String id) {
            logger.info("[{}] Registering component id={}, {}",
                        components.size(),
                        id,
                        trinity.getTheComponent());

			components.add(trinity.getTheProxy());
			contexts.put(id, trinity.getTheContext());
			actions.add("A");

			return id;
		}
				
		@Override
		public boolean remove(Object component) {
            logger.info("[{}] Removing component, {}",
                        components.size(),
                        component);

			components.add(component);
			actions.add("R");

			return true;
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
	
	@Test
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

		logger.info("** Parsing into components");
		StandardArooaParser parser = new StandardArooaParser(root, session);
		parser.parse(new XMLConfiguration("TEST", xml));
		
		
		ArooaContext context = session.pool.contexts.get("sequence");
		
		XMLArooaParser xmlParser = new XMLArooaParser(session.getArooaDescriptor());

        logger.info("** Copy to XML");

		ConfigurationHandle<ArooaContext> handle = xmlParser.parse(
				context.getConfigurationNode());

		ArooaContext xmlDoc = handle.getDocumentContext();

        logger.info("** Changing XML to rubbish");

		CutAndPasteSupport.replace(xmlDoc.getParent(), xmlDoc,
				new XMLConfiguration("Replace", "<rubbish/>"));

        logger.info("** Saving XML");

		try {
			handle.save();
			fail("Should fail.");
		} catch (Exception e) {
			// expected.
		}

        logger.info("** Destroying");

		session.pool.contexts.get("oj").getRuntime().destroy();

        logger.info("** Asserting");

        assertEquals(6, session.pool.actions.size());
		
		assertEquals("A", session.pool.actions.get(0));
		assertEquals("A", session.pool.actions.get(1));
		assertEquals("R", session.pool.actions.get(2));
		assertEquals("A", session.pool.actions.get(3));
		assertEquals("R", session.pool.actions.get(4));
		assertEquals("R", session.pool.actions.get(5));

		// Component removed is the same sequence that was added.
		assertEquals(session.pool.components.get(1),
			session.pool.components.get(2) );

		// Component removed is the again the same that was added back after
        // rollback.
		assertEquals(session.pool.components.get(3),
			session.pool.components.get(4) );

        assertNotSame(session.pool.components.get(1),
                      session.pool.components.get(3));
	}
	
}
