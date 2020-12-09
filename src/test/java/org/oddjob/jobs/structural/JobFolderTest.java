package org.oddjob.jobs.structural;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;
import org.oddjob.tools.FragmentHelper;
import org.oddjob.tools.OddjobTestHelper;

public class JobFolderTest extends OjTestCase {

   @Test
	public void testInOddjob() {
		
		String config = 
			"<oddjob>" +
			" <job>" +
			"  <folder>" +
			"   <jobs>" +
			"	 <echo id='job1'>Job1</echo>" +
			"	 <echo id='job2'>Job2</echo>" +
			"   </jobs>" +
			"  </folder>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", config));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(oddjob));

		assertNotNull(new OddjobLookup(oddjob).lookup("job1"));
		assertNotNull(new OddjobLookup(oddjob).lookup("job2"));
	}
	
	String xml =
		"<oddjob id='this'>" +
		" <job>" +
		"  <folder id='folder'/>" +
		" </job>" +
		"</oddjob>";

	String job = "<bean id='child'/>";
	
	
   @Test
	public void testCutAndPaste() throws ArooaParseException {
	
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();

		DragPoint point = oddjob.provideConfigurationSession().dragPointFor(
				new OddjobLookup(oddjob).lookup("folder"));
		
		DragTransaction trn = point.beginChange(ChangeHow.FRESH);
		point.paste(0, job);
		trn.commit();

		assertNotNull(new OddjobLookup(oddjob).lookup("child"));
		
		oddjob.destroy();
	}
	
   @Test
	public void testCutFolder() throws ArooaParseException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();

		DragPoint point = oddjob.provideConfigurationSession().dragPointFor(
				new OddjobLookup(oddjob).lookup("folder"));
		
		DragTransaction trn = point.beginChange(ChangeHow.FRESH);
		point.paste(0, "<bean/>");
		trn.commit();
		
		trn = point.beginChange(ChangeHow.FRESH);
		point.paste(1, "<bean/>");
		trn.commit();
		
		trn = point.beginChange(ChangeHow.FRESH);
		point.delete();
		trn.commit();
		
		assertNull(new OddjobLookup(oddjob).lookup("folder"));
		
		oddjob.destroy();
	}
	
   @Test
	public void testExample() throws ArooaParseException {
		
		// Just parse XML.
		
		FragmentHelper helper = new FragmentHelper();
		helper.createComponentFromResource(
				"org/oddjob/jobs/structural/JobFolderExample.xml");
	}
}
