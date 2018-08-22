package org.oddjob.designer.components;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.DesignSeedContext;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.CutAndPasteSupport;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;

public class RootDCTest extends OjTestCase {

   @Test
	public void testPasteAndCut() throws ArooaParseException {
				
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
    	
		DesignSeedContext context = new DesignSeedContext(
				ArooaType.COMPONENT,
				(new StandardArooaSession(descriptor)));
		
		DesignInstance design = new RootDC().createDesign(
				new ArooaElement("oddjob"), context);

		String paste = 
			    "<folder>" +
				" <jobs>" +
				"  <folder id='stuff'/>" +
				" </jobs>" +
				"</folder>";
	
		CutAndPasteSupport cutAndPaste = new CutAndPasteSupport(design.getArooaContext());
		
		assertTrue(cutAndPaste.supportsPaste());
		
		cutAndPaste.paste(0, new XMLConfiguration("PASTE", paste));
		
		Oddjob oddjob = new Oddjob();
		
		oddjob.setConfiguration(design.getArooaContext().getConfigurationNode());

		oddjob.run();
		
		assertNotNull(new OddjobLookup(oddjob).lookup("stuff"));
	}
	
	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<oddjob>" +
				" <job>" +
				"  <echo name='Test' text='Hello'/>" +
				" </job>" +
				"</oddjob>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor),
				new RootDC());
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(RootDesign.class, design.getClass());
	}

	public static void main(String args[]) throws ArooaParseException {

		RootDCTest test = new RootDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}
}
