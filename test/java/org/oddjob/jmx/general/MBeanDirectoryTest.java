package org.oddjob.jmx.general;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.DynaProperty;
import org.mockito.Mockito;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaTools;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;

public class MBeanDirectoryTest extends TestCase {

	public static class Vendor {
		
		public String getName() {
			return "Farmer Pickles";
		}
	}
	
	public void testLookup() throws Exception {

		DynaProperty prop = new DynaProperty("vendor");
		
		DynaClass dynaClass = mock(DynaClass.class);
		when(dynaClass.getDynaProperty("vendor")).thenReturn(prop);
		
		MBeanNode node = mock(MBeanNode.class);
		when(node.get("vendor")).thenReturn(new Vendor());
		when(node.getDynaClass()).thenReturn(dynaClass);
		
		MBeanCache cache = mock(MBeanCache.class);
		when(cache.findBean(
				new ObjectName("mydomain:name=fruit"))).thenReturn(node);
		
		BeanUtilsPropertyAccessor accessor = 
				new BeanUtilsPropertyAccessor();
		
		ArooaTools tools = mock(ArooaTools.class);
		when(tools.getPropertyAccessor()).thenReturn(accessor);
		
		ArooaSession arooaSession = mock(ArooaSession.class);
		when(arooaSession.getTools()).thenReturn(tools);
		
		MBeanSession session = Mockito.mock(MBeanSession.class);
		when(session.getArooaSession()).thenReturn(arooaSession);
		when(session.getMBeanCache()).thenReturn(cache);
		
		MBeanDirectory test = new MBeanDirectory(session);
		
		Object result = test.lookup("mydomain:name=fruit.vendor.name");
		
		Mockito.verify(node).get("vendor");
		
		assertEquals("Farmer Pickles", result);
	}
}
