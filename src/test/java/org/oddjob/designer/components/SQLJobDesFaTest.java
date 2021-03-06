package org.oddjob.designer.components;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.apache.commons.beanutils.DynaBean;
import org.oddjob.OddjobDescriptorFactory;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignParser;
import org.oddjob.arooa.design.view.ViewMainHelper;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.sql.BasicGenericDialect;
import org.oddjob.sql.SQLJob;
import org.oddjob.tools.OddjobTestHelper;

public class SQLJobDesFaTest extends OjTestCase {

	DesignInstance design;
	
   @Test
	public void testCreate() throws ArooaParseException {
		
		String xml =  
			"<sql name='A Test'" +
			"		     id='this'" +
			"            autocommit='true'" +
			"            callable='true'" +
			"            escapeProcessing='true'" +
			"            onError='CONTINUE'" +
			"            delimiter='go'" +
			"            delimiterType='ROW'" +
			"                  >" +
			"   <parameters>" +
			"    <value value='Apple'/>" +
			"    <value value='42'/>" +
			"   </parameters>" +
			"   <input>" +
			"    <buffer>select * from dual</buffer>" +
			"   </input>" +
			"   <results>" +
			"    <sql-results-sheet/>" +
			"   </results>" +
			"   <dialect>" +
			"    <bean class='org.oddjob.sql.BasicGenericDialect'/>" +
			"   </dialect>" +
			"</sql>";
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
    	
		DesignParser parser = new DesignParser(
				new StandardArooaSession(descriptor));
		parser.setArooaType(ArooaType.COMPONENT);
		
		parser.parse(new XMLConfiguration("TEST", xml));
		
		design = (SqlDesign) parser.getDesign();
		
		assertEquals(SqlDesign.class, design.getClass());
		
		DynaBean test = (DynaBean) OddjobTestHelper.createComponentFromConfiguration(
				design.getArooaContext().getConfigurationNode());
		
		assertEquals(true, test.get("callable"));
		assertEquals(true, test.get("autocommit"));
		assertEquals(true, test.get("escapeProcessing"));
		assertEquals("go", test.get("delimiter"));
		assertEquals(SQLJob.DelimiterType.ROW, test.get("delimiterType"));
		assertEquals(SQLJob.OnError.CONTINUE, test.get("onError"));
		assertEquals(BasicGenericDialect.class, test.get("dialect").getClass());
		
	}

	public static void main(String args[]) throws ArooaParseException {

		SQLJobDesFaTest test = new SQLJobDesFaTest();
		test.testCreate();
		
		ViewMainHelper view = new ViewMainHelper(test.design);
		view.run();
	}
}
