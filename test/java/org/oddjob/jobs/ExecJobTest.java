/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.jobs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.ConverterHelper;
import org.oddjob.Helper;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OurDirs;
import org.oddjob.Stateful;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.utils.ArooaTokenizer;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.io.BufferType;
import org.oddjob.io.FilesType;
import org.oddjob.logging.ConsoleOwner;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogLevel;
import org.oddjob.logging.LogListener;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.structural.ChildHelper;

/**
 * test for exec job.
 */
public class ExecJobTest extends TestCase {
	private static final Logger logger = Logger.getLogger(ExecJobTest.class);
	
	String catCmd;
	String echoCmd;
	String[] setFruitCmd;
	
	protected void setUp() {
		logger.debug("================== " + getName() + " ===================");
		
		if (System.getProperty("os.name").startsWith("Windows")) {
			catCmd = "cmd /c more";
			echoCmd = "cmd /c echo";
			setFruitCmd = new String[] { "cmd", "/c", "set", "FRUIT=APPLES" };
		}
		else {
			catCmd = "cat";
			echoCmd = "echo";
			setFruitCmd = new String[] { "sh", "-c", "set", "FRUIT=APPLES" };
		}
	}
	
	public void testCreate() {
		
		String xml =  
			"<oddjob>" +
			" <job>" +
			"  <exec name=\"A Test Exec\">" +
			"   <args>" +
			"    <list>" +
			"     <values>" + 
			"      <value value='java'/>" +
			"      <value value='-version'/>" +
			"     </values>" +
			"    </list>" +
			"   </args>" + 
			"  </exec>" +
			" </job>" +
			"</oddjob>";

		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
		
		Object[] children = ChildHelper.getChildren(oj);
		assertNotNull("created", children[0]);
		assertEquals("state", JobState.COMPLETE, Helper.getJobState(
				(Stateful) children[0]));
	}
	
	public void testCommand() {
		ExecJob job = new ExecJob();
		job.setCommand("java -version");
		
		job.run();
		assertEquals("Success", JobState.COMPLETE, job.lastStateEvent().getState());
		
	}

	public void testStop() throws Exception {
		OurDirs dirs = new OurDirs();
		
		BufferType buf = new BufferType();
		buf.configured();
		
		final ExecJob job = new ExecJob();

		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		FilesType files = new FilesType();
		files.setFiles(new File(dirs.base(), "lib/*.jar").toString());
		
		File[] cpFiles = files.toFiles();

		String classPath = converter.convert(cpFiles, String.class);
		
		job.setCommand("java -cp " + classPath + " org.oddjob.Main -f " +
				new File(dirs.base(), "test/conf/wait.xml"));

		job.setStdout(buf.toOutputStream());
		job.setRedirectStderr(true);
		
		Thread t = new Thread(new Runnable() {
			public void run() {
				try { 
					job.run();
				} catch (Throwable t) {
					t.printStackTrace();
					fail();
				}
			}
		});
		t.start();
		
		String[] lines = null; 
		while (true) {
			lines = buf.getLines();
			if (lines.length > 0) {
				break;
			}
			else {
				Thread.sleep(500);
			}
		}
		
		job.stop();
		t.join();

		logger.debug("" + buf.getText());
		
		assertEquals(1, lines.length);
		
		assertEquals(JobState.INCOMPLETE, job.lastStateEvent().getState());
		
	}
	
	public void testFailure() {
		ExecJob job = new ExecJob();
		job.setCommand("java rubbish");
		
		job.run();
		assertEquals(JobState.INCOMPLETE, job.lastStateEvent().getState());		
	}

	public void testOutput() throws IOException {
		
		ExecJob ej = new ExecJob();
		ej.setCommand(echoCmd + " hello");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ej.setStdout(os);
		ej.run();
		byte[] bytes = os.toByteArray();
		assertEquals("hello" + System.getProperty("line.separator"), new String(bytes));
	}
	
	public void testConsole1() throws IOException {
		
		ExecJob ej = new ExecJob();
		ej.setCommand(echoCmd + " hello");
		ej.run();
				
		LL ll = new LL();
		ej.consoleLog().addListener(ll, LogLevel.DEBUG, -1, 1000);
		String result = ll.getLines()[0];
		logger.debug(result);
		assertEquals("hello" + System.getProperty("line.separator"), result);
	}

	public void testChained() throws IOException {
		
		String xml =
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <variables id='v'>" +
			"     <buff>" +
			"      <buffer/>" +
			"     </buff>" +
			"    </variables>" +
			"    <exec name='One' id='one'>" +
			echoCmd + " hello" +
			"     <stdout>" +
			"      <value value='${v.buff}'/>" +
			"     </stdout>" +
			"    </exec>" +
			"    <exec name='Two' id='two'>" +
			catCmd + 
			"     <stdin>" +
			"      <value value='${v.buff}'/>" +
			"     </stdin>" +
			"    </exec>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("TEST", xml));
		oj.run();
		
//		OddjobExplorer explorer = new OddjobExplorer();
//		explorer.setRoot(oj);
//		explorer.run();
//		
		assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
		
		// check console output for second job
		ConsoleOwner ca = (ConsoleOwner) new OddjobLookup(
				oj).lookup("two");
		
		LL ll = new LL();
		ca.consoleLog().addListener(ll, LogLevel.DEBUG, -1, 1000);
		String result = ll.getLines()[0];
		System.out.println(result);
		assertEquals("hello" + System.getProperty("line.separator"), result);
		
	}
	
	class LL implements LogListener {
		List<String> list = new ArrayList<String>();
		public void logEvent(LogEvent logEvent) {
			list.add(logEvent.getMessage());
		}
		String[] getLines() {
			return (String[]) list.toArray(new String[0]);
		}
	}

	/**
	 * Checking if the environment is of the process or the subprocess - of course
	 * it's of the parent - as there's no way to get the environment of a child
	 * in Unix and java isn't magic.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void testEnvironment() throws IOException, InterruptedException {
		
		ProcessBuilder processBuilder = new ProcessBuilder(setFruitCmd);
		
		Process process = processBuilder.start();
		
		int returned = process.waitFor();

		assertEquals(0, returned);
		
		Map<String, String> env = processBuilder.environment();
		
		assertNull(env.get("FRUIT"));
	}
	
	public void testSplitCommand() throws ParseException {
		
		ArooaTokenizer tokenizer = new ExecJob().commandTokenizer();
		
		String[] result;
		
		result = tokenizer.parse("Hello World");
		assertArray( new String[] { "Hello", "World" } ,result);
		
		result = tokenizer.parse("\"Hello\" \"World\"");
		assertArray( new String[] { "Hello", "World" } ,result);
		
		result = tokenizer.parse("\"Hello World\"");
		assertArray( new String[] { "Hello World" } ,result);
		
		result = tokenizer.parse("\"Hello\"\n\"World\"");
		assertArray( new String[] { "Hello", "World" } ,result);
	}
	
	private void assertArray(String[] expected, String[] result) {
		if (expected.length != result.length) {
			throw new RuntimeException("" + expected.length + 
					"!=" + result.length);
		}
		for (int i = 0; i < expected.length; ++i) {
			if (!expected[i].equals(result[i])) {
				throw new RuntimeException("Expected " + expected[i] + 
						",  was " + result[i]);
			}
		}
	}
	
	public void testEnvironmentInOddjob() throws Exception {
		
		String envCommand;
		
		String os = System.getProperty("os.name").toLowerCase();
		if (os.matches(".*windows.*")) {
			envCommand = "cmd /c set";
		}
		else {
			envCommand = "sh -c set";
		}

		String xml=
			"<oddjob>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <variables id='v'>" +
			"     <buf>" +
			"      <buffer/>" +
			"     </buf>" +
			"    </variables>" +
			"    <exec>" +
			envCommand +
			"     <stdout>" +
			"      <value value='${v.buf}'/>" +
			"     </stdout>" +
			"     <environment>" +
			"      <properties>" +
			"       <values>" +
			"        <value key='FRUIT' value='apples'/>" +
			"       </values>" +
			"      </properties>" +
			"     </environment>" +
			"    </exec>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";
		
		Oddjob oj = new Oddjob();
		oj.setConfiguration(new XMLConfiguration("XML", xml));
		oj.run();
				
		String output = new OddjobLookup(oj).lookup(
				"v.buf", String.class);

		logger.debug(output);
		
		assertTrue(output.contains("FRUIT=apples"));
	}
	
	public void testSerialize() throws IOException, ClassNotFoundException {
		
		ExecJob test = new ExecJob();
		test.setCommand("echo hello");
		
		ExecJob copy = Helper.copy(test);
		
		assertNotNull(copy);
	}
	
}
