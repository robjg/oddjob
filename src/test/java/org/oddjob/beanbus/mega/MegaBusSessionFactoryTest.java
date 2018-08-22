package org.oddjob.beanbus.mega;

import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.life.ClassResolverClassLoader;
import org.oddjob.arooa.life.InstantiationContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.standard.StandardArooaSession;

public class MegaBusSessionFactoryTest extends OjTestCase {

   @Test
	public void testCreateSessionFactory() throws URISyntaxException {
		
		MegaBusSessionFactory test = new MegaBusSessionFactory();
		
		ArooaSession existingSession = new StandardArooaSession();

		ArooaDescriptor existingDescriptor = 
				existingSession.getArooaDescriptor();

		ClassLoader classLoader = new ClassResolverClassLoader(
				existingDescriptor.getClassResolver());
		
		ArooaSession session = test.createSession(
				existingSession, classLoader);
		
		ArooaDescriptor descriptor = session.getArooaDescriptor();
		
		ArooaClass cl = descriptor.getElementMappings().mappingFor(
				new ArooaElement(new URI("oddjob:beanbus"), "bean-copy"), 
						new InstantiationContext(ArooaType.COMPONENT, null));
		
		assertNotNull(cl);
	}
}
