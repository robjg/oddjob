package org.oddjob.jmx.general;

import static org.mockito.Mockito.*;

import java.util.Set;
import java.util.TreeSet;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import junit.framework.TestCase;

import org.oddjob.arooa.life.ClassLoaderClassResolver;

public class MBeanCacheMapTest extends TestCase {

	
	public void testFindMany() throws Exception {
		
		ObjectName objectName1 = new ObjectName("fruit:name=apple");
		ObjectName objectName2 = new ObjectName("fruit:name=orange");

		Set<ObjectName> objectNames = new TreeSet<ObjectName>();
		objectNames.add(objectName1);
		objectNames.add(objectName2);
		
		MBeanInfo info = new MBeanInfo("foo.Foo", "Some Foo Bean", 
				new MBeanAttributeInfo[0], new MBeanConstructorInfo[0], 
				new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
		
		MBeanServerConnection mbsc = mock(MBeanServerConnection.class);
		when(mbsc.queryNames(new ObjectName("fruit:*"), null)
				).thenReturn(objectNames);
		when(mbsc.getMBeanInfo(any(ObjectName.class))).thenReturn(info);
		
		MBeanCacheMap test = new MBeanCacheMap(mbsc,
				new ClassLoaderClassResolver(getClass().getClassLoader()));
		
		Object[] object = test.findBeans(new ObjectName("fruit:*"));
		
		assertEquals(2, object.length);
		
		assertEquals("fruit:name=apple", object[0].toString());
		assertEquals("fruit:name=orange", object[1].toString());
		
		Object findAgain = test.findBean(
				new ObjectName("fruit:name=orange"));
		
		assertEquals(object[1], findAgain);
	}
	
	public void testFindOne() throws Exception {
		
		ObjectName objectName = new ObjectName("fruit:name=orange");

		Set<ObjectName> objectNames = new TreeSet<ObjectName>();
		objectNames.add(objectName);
		
		MBeanInfo info = new MBeanInfo("foo.Foo", "Some Foo Bean", 
				new MBeanAttributeInfo[0], new MBeanConstructorInfo[0], 
				new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
		
		MBeanServerConnection mbsc = mock(MBeanServerConnection.class);
		when(mbsc.queryNames(new ObjectName("fruit:name=orange"), null)
				).thenReturn(objectNames);
		when(mbsc.getMBeanInfo(any(ObjectName.class))).thenReturn(info);
		
		MBeanCacheMap test = new MBeanCacheMap(mbsc,
				new ClassLoaderClassResolver(getClass().getClassLoader()));
		
		Object object = test.findBean(new ObjectName("fruit:name=orange"));
		
		assertEquals("fruit:name=orange", object.toString());
		
		Object findAgain = test.findBean(
				new ObjectName("fruit:name=orange"));
		
		assertEquals(object, findAgain);
	}
}
