package org.oddjob.designer.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jobs.structural.ParallelJob;
import org.oddjob.state.WorstStateOp;
import org.oddjob.tools.OddjobTestHelper;

/**
 *
 */
public class ParallelDCTest extends OjTestCase {
	private static final Logger logger = LoggerFactory.getLogger(ParallelDCTest.class);
	
	@Before
   public void setUp() {
		logger.debug("========================== " + getName() + "===================" );
	}

	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		String xml =  
				"<parallel name='Test' join='true'" +
				"            stateOperator='WORST' transient='true'>" +
				" <executorService>" +
				"  <bean class='org.oddjob.scheduling.MockExecutorService'/>" +
				" </executorService>" +
				" <jobs>" +
				"  <echo/>" +
				"  <echo/>" +
				" </jobs>" +
				"</parallel>";
	
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(
    				getClass().getClassLoader());
		
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = parser.getDesign();
		
		assertEquals(ParallelDesign.class, design.getClass());
		
		ParallelJob test = (ParallelJob) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals("Test", test.getName());
		assertEquals(true, test.isJoin());
		assertEquals(WorstStateOp.class, test.getStateOperator().getClass());
		assertEquals(true, test.isTransient());
		
		Object[] children = OddjobTestHelper.getChildren(test);

		assertEquals(2, children.length);
	}

	public static void main(String args[]) throws ArooaParseException {

		ParallelDCTest test = new ParallelDCTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
		
	}

}
