package org.oddjob.script;

import java.io.File;
import java.text.ParseException;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.convert.DefaultConversionRegistry;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

public class InvokeTypeTest extends TestCase {

	private static final Logger logger = Logger.getLogger(InvokeTypeTest.class);
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		logger.info("-----------------  " + getName() + " ------------------");
	}
	
	public static class Thing {
		
		public String simpleStuff() {
			return "simple";
		}
		
		public Object complexStuff(String s) {
			return s;
		}
		
		public Object complexStuff(File f, File y) {
			return f.toString() + y.toString();
		}
		
		public Object complexStuff(int i, double d) {
			return new Double(d + i);
		}
		
		public void nothing() {
			
		}
		
		public static String staticThing(String s) {
			return s;
		}
	}
	
	public void testSimple() throws Throwable {
		
		InvokeType test = new InvokeType();
		
		test.setFunction("simpleStuff");
		test.setSource(new MethodInvoker(new Thing()));
		
		Object result = test.toValue();
		
		assertEquals("simple", result);
	}
	
	public void testStaticMethodOnObject() throws Throwable {
		
		
		InvokeType test = new InvokeType();
		test.setArooaSession(new StandardArooaSession());
		
		test.setFunction("staticThing");
		test.setSource(new MethodInvoker(new Thing()));
		test.setParameters(0, new ArooaObject("Apples"));
		
		assertEquals("Apples", test.toValue());
	}
	
	public void testClassMethodInvoke() throws Throwable {
		
		
		InvokeType test = new InvokeType();
		test.setArooaSession(new StandardArooaSession());
		
		test.setFunction("newInstance");
		test.setSource(new MethodInvoker(Thing.class));
		
		assertEquals(Thing.class, test.toValue().getClass());
	}
	
	public void testWrongParameters() throws Throwable {
		
		InvokeType test = new InvokeType();
		test.setArooaSession(new StandardArooaSession());
		
		test.setFunction("simpleStuff");
		test.setSource(new MethodInvoker(new Thing()));
		
		test.setParameters(0, new ArooaObject("Thing"));
		
		try {
			test.toValue();
			fail("Should fail.");
		}
		catch (RuntimeException e) {
			// expected.
		}
	}

	public void testNumberToString() throws Throwable {
		
		InvokeType test = new InvokeType();
		test.setArooaSession(new StandardArooaSession());
		
		test.setFunction("complexStuff");
		test.setSource(new MethodInvoker(new Thing()));
		
		test.setParameters(0, new ArooaObject(1));
		
		Object result = test.toValue();
		
		assertEquals("1", result);
	}

	public void testNumbers() throws Throwable {
		
		InvokeType test = new InvokeType();
		test.setArooaSession(new StandardArooaSession());
		
		test.setFunction("complexStuff");
		test.setSource(new MethodInvoker(new Thing()));
		
		test.setParameters(0, new ArooaObject(2));
		test.setParameters(1, new ArooaObject(2));
		
		Object result = test.toValue();
		
		assertEquals(new Double(4), result);
	}

	public void testFiles() throws Throwable {
		
		InvokeType test = new InvokeType();
		test.setArooaSession(new StandardArooaSession());
		
		test.setFunction("complexStuff");
		test.setSource(new MethodInvoker(new Thing()));
		
		test.setParameters(0, new ArooaObject("BillAnd"));
		test.setParameters(1, new ArooaObject("Ben"));
		
		Object result = test.toValue();
		
		assertEquals("BillAndBen", result);
	}
	
	public void testConversion() throws ArooaConversionException {
		
		InvokeType test = new InvokeType();
		
		test.setFunction("simpleStuff");
		test.setSource(new MethodInvoker(new Thing()));
		
		DefaultConversionRegistry conversions = 
			new DefaultConversionRegistry();

		new InvokeType.Conversions().registerWith(conversions);
		
		ArooaConverter converter = new DefaultConverter(conversions);
		
		String result = converter.convert(test, String.class);
		
		assertEquals("simple", result);
	}
	
	public void testMethodExample() throws ArooaPropertyException, ArooaConversionException, ParseException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/InvokeMethod.xml",
				getClass().getClassLoader()));
		
		oddjob.setExport("date", new ArooaObject(
				DateHelper.parseDateTime("2011-04-01 14:00")));
		
		oddjob.run();
		
		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());
		
		OddjobLookup lookup = new OddjobLookup(oddjob);
		
		String result = lookup.lookup("echo-greeting.text", String.class);
		
		assertEquals("Good Afternoon", result);
		
		oddjob.destroy();
	}
	
	public void testStaticExample() throws ArooaConversionException {
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/script/InvokeStatic.xml",
				getClass().getClassLoader()));
		
		oddjob.run();

		assertEquals("Hello John", new OddjobLookup(oddjob).lookup(
				"echo-greeting.text", String.class));
	}
	
}
