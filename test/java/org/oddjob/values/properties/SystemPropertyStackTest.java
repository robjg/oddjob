package org.oddjob.values.properties;

import java.util.Properties;

import junit.framework.TestCase;

public class SystemPropertyStackTest extends TestCase {

	public static final String PROP0 = "oddjob.syspropstack.test.prop0";
	
	public static final String PROP1 = "oddjob.syspropstack.test.prop1";
	
	public static final String PROP2 = "oddjob.syspropstack.test.prop2";
	
	public static final String PROP3 = "oddjob.syspropstack.test.prop3";
	
	public static final String PROP4 = "oddjob.syspropstack.test.prop4";
	
	Properties props1 = new Properties();
	
	Properties props2 = new Properties();
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		System.setProperty(PROP0, "SetBySystem");
		System.setProperty(PROP1, "SetBySystem");
		
		props1.setProperty(PROP1, "SetByProps1");
		props1.setProperty(PROP2, "SetByProps1");
		props1.setProperty(PROP4, "SetByProps1");
		
		props2.setProperty(PROP1, "SetByProps2");
		props2.setProperty(PROP2, "SetByProps2");
		props2.setProperty(PROP3, "SetByProps2");
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		
		System.getProperties().remove(PROP1);
	}
	
	public void testSetThenUnsetLeavesPropertiesAsOriginals() {
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetBySystem", System.getProperty(PROP1));
		assertEquals(null, System.getProperty(PROP2));
		assertEquals(null, System.getProperty(PROP3));
		assertEquals(null, System.getProperty(PROP4));
		
		SystemPropertyStack.Token token1 = 
				SystemPropertyStack.addProperties(props1);
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetByProps1", System.getProperty(PROP1));
		assertEquals("SetByProps1", System.getProperty(PROP2));
		assertEquals(null, System.getProperty(PROP3));
		assertEquals("SetByProps1", System.getProperty(PROP4));
		
		SystemPropertyStack.removeProperties(token1);
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetBySystem", System.getProperty(PROP1));
		assertEquals(null, System.getProperty(PROP2));
		assertEquals(null, System.getProperty(PROP3));
		assertEquals(null, System.getProperty(PROP4));
	}		
	
	public void testSetTwiceThenUnsetInReverseOrder() {
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetBySystem", System.getProperty(PROP1));
		assertEquals(null, System.getProperty(PROP2));
		assertEquals(null, System.getProperty(PROP3));
		assertEquals(null, System.getProperty(PROP4));
		
		SystemPropertyStack.Token token1 = 
				SystemPropertyStack.addProperties(props1);
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetByProps1", System.getProperty(PROP1));
		assertEquals("SetByProps1", System.getProperty(PROP2));
		assertEquals(null, System.getProperty(PROP3));
		assertEquals("SetByProps1", System.getProperty(PROP4));
		
		SystemPropertyStack.Token token2 = 
				SystemPropertyStack.addProperties(props2);
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetByProps2", System.getProperty(PROP1));
		assertEquals("SetByProps2", System.getProperty(PROP2));
		assertEquals("SetByProps2", System.getProperty(PROP3));
		assertEquals("SetByProps1", System.getProperty(PROP4));
		
		SystemPropertyStack.removeProperties(token2);
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetByProps1", System.getProperty(PROP1));
		assertEquals("SetByProps1", System.getProperty(PROP2));
		assertEquals(null, System.getProperty(PROP3));
		assertEquals("SetByProps1", System.getProperty(PROP4));
		
		SystemPropertyStack.removeProperties(token1);
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetBySystem", System.getProperty(PROP1));
		assertEquals(null, System.getProperty(PROP2));
		assertEquals(null, System.getProperty(PROP3));
		assertEquals(null, System.getProperty(PROP4));
	}
	
	public void testSetTwiceThenUnsetInSameOrder() {
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetBySystem", System.getProperty(PROP1));
		assertEquals(null, System.getProperty(PROP2));
		assertEquals(null, System.getProperty(PROP3));
		assertEquals(null, System.getProperty(PROP4));
		
		SystemPropertyStack.Token token1 = 
				SystemPropertyStack.addProperties(props1);
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetByProps1", System.getProperty(PROP1));
		assertEquals("SetByProps1", System.getProperty(PROP2));
		assertEquals(null, System.getProperty(PROP3));
		assertEquals("SetByProps1", System.getProperty(PROP4));
		
		SystemPropertyStack.Token token2 = 
				SystemPropertyStack.addProperties(props2);
		
		SystemPropertyStack.removeProperties(token1);
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetByProps2", System.getProperty(PROP1));
		assertEquals("SetByProps2", System.getProperty(PROP2));
		assertEquals("SetByProps2", System.getProperty(PROP3));
		assertEquals(null, System.getProperty(PROP4));
		
		SystemPropertyStack.removeProperties(token2);
		
		assertEquals("SetBySystem", System.getProperty(PROP0));
		assertEquals("SetBySystem", System.getProperty(PROP1));
		assertEquals(null, System.getProperty(PROP2));
		assertEquals(null, System.getProperty(PROP3));		
		assertEquals(null, System.getProperty(PROP4));
	}
}
