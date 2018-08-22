package org.oddjob;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

public class OjTestCase extends Assert {

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
	

}
