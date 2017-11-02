package org.oddjob.doclet;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.beandocs.BeanDoc;
import org.oddjob.arooa.beandocs.SessionArooaDocFactory;
import org.oddjob.arooa.beandocs.WriteableArooaDoc;
import org.oddjob.arooa.beandocs.WriteableBeanDoc;
import org.oddjob.arooa.standard.StandardArooaSession;

public class JobsAndTypesTest extends OjTestCase {

   @Test
	public void testTypes() {
		
		StandardArooaSession session = new StandardArooaSession();

		SessionArooaDocFactory factory = new SessionArooaDocFactory(session);

		WriteableArooaDoc jobs = factory.createBeanDocs(ArooaType.COMPONENT);
		WriteableArooaDoc types = factory.createBeanDocs(ArooaType.VALUE);
		
		JobsAndTypes test = new JobsAndTypes(jobs, types);			

		assertNotNull(
				test.docFor("org.oddjob.arooa.types.IsType"));
	}
	
   @Test
	public void testDuplicateType() {
		
		StandardArooaSession session = new StandardArooaSession();

		SessionArooaDocFactory factory = new SessionArooaDocFactory(session);

		WriteableArooaDoc jobs = factory.createBeanDocs(ArooaType.COMPONENT);
		WriteableArooaDoc types = factory.createBeanDocs(ArooaType.VALUE);
		
		JobsAndTypes test = new JobsAndTypes(jobs, types);			

		WriteableBeanDoc instance = test.docFor("org.oddjob.arooa.types.IsType");
	
		BeanDoc jobDoc = test.docForJob("is");
		
		BeanDoc typeDoc = test.docForType("is");
		
		assertSame(instance, jobDoc);
		assertSame(instance, typeDoc);
	}
	
}
