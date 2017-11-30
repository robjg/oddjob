package org.oddjob.tools;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.junit.Test;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CosoleCaptureTest {

	private static final Logger logger = LoggerFactory.getLogger(CosoleCaptureTest.class);
	
	@Test
	public void testLogFilter() {
		
		Pattern pattern = ConsoleCapture.LOG_PATTERN;
		
		assertThat(pattern.matcher("DEBUG [foo] - A message\n").matches(), is(true));
		assertThat(pattern.matcher(" INFO [foo] - A message\n").matches(), is(true));
		
		ConsoleCapture test = new ConsoleCapture();
		
		Predicate<String> filter = test.getFilter();
				
		assertThat(filter.test("Some text"), is(true));
		assertThat(filter.test("TRACE [foo] - A message\r\n"), is(false));
		assertThat(filter.test("DEBUG [foo] - A message\r\n"), is(false));
		assertThat(filter.test(" INFO [foo] - A message\r\n"), is(false));
		assertThat(filter.test(" WARN [foo] - A message\r\n"), is(false));
		assertThat(filter.test("ERROR [foo] - A message\r\n"), is(false));
		assertThat(filter.test("Some DEBUG text"), is(true));
	}
	
	@Test
	public void testLoggingNotCapture() {
		
		ConsoleCapture test = new ConsoleCapture();
		try (ConsoleCapture.Close close = test.captureConsole()) {
			
			logger.info("You won't see this!");
			System.out.println("You will only see this!");
		}
		
		test.dump();
		String[] lines = test.getLines();
		
		assertThat(lines.length, is(1));
		assertThat(lines[0], is("You will only see this!"));
		
	}
	
	@Test
	public void testLastNewlineStripped() {
				
		ConsoleCapture.Console console = new ConsoleCapture.Console();
		
		LogEvent event = new LogEvent("org.foo", 0, LogLevel.DEBUG, "  Some\ntext\r\n");
		
		console.logEvent(event);
		
		assertThat(console.lines.get(0), is("  Some\ntext"));
	}
}
