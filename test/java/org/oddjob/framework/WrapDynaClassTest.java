/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.framework;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.apache.commons.beanutils.DynaProperty;

public class WrapDynaClassTest extends TestCase {

	public static class MyBean {
		public String getSimple() { return null; }
		public String getMapped(String foo) { return null; }
		public String[] getIndexed() { return null; }
		public boolean isOk() { return true; }
	}
	
	public void testProperties() {
		WrapDynaClass test = WrapDynaClass.createDynaClass(MyBean.class);

		DynaProperty result;
		
		result = test.getDynaProperty("simple"); 
		assertNotNull("simple", result);
		assertEquals("simple class", String.class, result.getType());

		result = test.getDynaProperty("indexed");
		assertNotNull("indexed", result);
		assertTrue("is indexed", result.isIndexed());

		result = test.getDynaProperty("mapped");
		assertNotNull("mapped", result);
		// this isn't true which must be a bug.
		assertTrue("is mapped", result.isMapped());
		
		result = test.getDynaProperty("ok");
		assertNotNull("boolean", result);
		
	}
	
	public static class MixedTypes {
		public String getStuff(String key) {
			return "Stuff";
		}
		public void setStuff(String key) {
		}
	}
	
	public void testMixedTypes() {
		WrapDynaClass test = WrapDynaClass.createDynaClass(MixedTypes.class);

		DynaProperty result;
		
		result = test.getDynaProperty("stuff");
		assertNull(result);
		
	}
	
	public void testSerialize() throws Exception {
		WrapDynaClass test = WrapDynaClass.createDynaClass(MyBean.class);
		
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bytes);
		oos.writeObject(test);
		oos.close();
		
		ObjectInputStream ois = new ObjectInputStream(
				new ByteArrayInputStream(bytes.toByteArray()));
		
		Object o = ois.readObject();
		
		WrapDynaClass clone = (WrapDynaClass) o;
		
		assertEquals(test.getDynaProperties().length, clone.getDynaProperties().length);
	}
}
