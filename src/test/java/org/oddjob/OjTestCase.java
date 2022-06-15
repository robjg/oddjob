package org.oddjob;

import org.hamcrest.MatcherAssert;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.*;

public class OjTestCase extends MatcherAssert {

	@Rule public TestName name = new TestName();

	public String getName() {
        return name.getMethodName();
    }
	
	public static String logConfig() {
		
		ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
		String className = loggerFactory.getClass().getName();
		switch (className) {
		case "org.slf4j.impl.Log4jLoggerFactory":
			return "test/launch/log4j.properties";
		case "ch.qos.logback.classic.LoggerContext":
			return "test/launch/logback.xml";
		default:
			throw new IllegalStateException("No config for " + className);
		}
	}

	public static void assertSame(Object expected, Object actual) {

		assertThat(actual, sameInstance(expected));
	}

	public static void assertSame(String message, Object expected, Object actual) {

		assertThat(message, actual, sameInstance(expected));
	}


	public static void assertEquals(Object expected, Object actual) {

		assertThat(actual, is(expected));
	}

	public static void assertEquals(String message, Object expected, Object actual) {

		assertThat(message, actual, is(expected));
	}

	public static void assertEquals(long expected, long actual) {

		assertThat(actual, is(expected));
	}

	public static void assertEquals(String message, long expected, long actual) {

		assertThat(message, actual, is(expected));
	}
	public static void assertEquals(double expected, double actual, double tolerance) {

		assertThat(actual, closeTo(expected, tolerance));
	}

	public static void assertTrue(boolean actual) {

		assertThat("Expected true", actual);
	}

	public static void assertTrue(String message, boolean actual) {

		assertThat(message, actual);
	}
	public static void assertFalse(boolean actual) {

		assertThat("Expected false", !actual);
	}

	public static void assertFalse(String message, boolean actual) {

		assertThat(message, !actual);
	}

	public static void assertNull(Object actual) {
		assertThat(actual, nullValue());
	}

	public static void assertNull(String message, Object actual) {
		assertThat(message, actual, nullValue());
	}

	public static void assertNotNull(Object actual) {
		assertThat(actual, notNullValue());
	}

	public static void assertNotNull(String message, Object actual) {
		assertThat(message, actual, notNullValue());
	}

	public static void fail() {

		//noinspection ConstantConditions
		assertThat("Fail!", false);
	}

	public static void fail(String message) {

		//noinspection ConstantConditions
		assertThat(message, false);
	}
}
