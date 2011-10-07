/*
 * (c) Rob Gordon 2005
 */
package org.oddjob;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.oddjob.oddballs.OddballsDirDescriptorFactory;
import org.oddjob.state.ParentState;

/**
 * 
 */
public class MainTest extends TestCase {

	private static final Logger logger = Logger.getLogger(MainTest.class);
	
	@Override
	protected void setUp() throws Exception {
		logger.debug("------------------ " + getName() + " -----------------");
		logger.debug(System.getProperty("ant.file"));
	}
	
	// test oddjob args past through
	public void testInit() throws IOException {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { 
				"-f", dirs.base() + "/oddjob.xml",  "x" } ).getOddjob();
		
		assertEquals(1, oj.getArgs().length);
		assertEquals("x", oj.getArgs()[0]);
	}
	
	public void testBadArg() throws IOException {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { "-f", dirs.base() + "/oddjob.xml", 
				"x", "-f", "something-else.xml" }).getOddjob();
		
		assertEquals(3, oj.getArgs().length);
		assertEquals("x", oj.getArgs()[0]);
		assertEquals("-f", oj.getArgs()[1]);
		assertEquals("something-else.xml", oj.getArgs()[2]);
	}
		
	public void testPassArgs() throws IOException {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { 
				"-f", dirs.base() + "/oddjob.xml", "--", 
				"-f", "something-else.xml" } ).getOddjob();
		
		assertEquals(2, oj.getArgs().length);
		assertEquals("-f", oj.getArgs()[0]);
		assertEquals("something-else.xml", oj.getArgs()[1]);
	}
		
	public void testOddjobName() throws IOException {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { 
				"-n", "Test Jobs", "-f", dirs.base() + 
				"/oddjob.xml"} ).getOddjob();
		
		assertEquals("Test Jobs", oj.toString());
	}
		
	public void testUsage() throws IOException {
		
		Main.main(new String[] { "-h" });
	}
	
	public void testVersion() throws IOException {
		
		Main.main(new String[] { "-version" });
	}
	
	public void testInitNoBalls() throws IOException {

		OurDirs dirs = new OurDirs();
		
		Main test = new Main();

		Oddjob oddjob = test.init(new String[] { 
			"-oddballs", "someballs", "-nb", 
			"-f", dirs.base() + "/oddjob.xml" }).getOddjob();
		
		assertNull(oddjob.getDescriptorFactory());
	}
	
	public void testDefaultBalls() throws IOException {

		OurDirs dirs = new OurDirs();
		
		Main test = new Main();

		System.setProperty("oddjob.home", 
				dirs.base().getCanonicalPath());
		
		Oddjob oddjob = test.init(new String[] { }).getOddjob();
		
		OddballsDirDescriptorFactory result = (OddballsDirDescriptorFactory)
			oddjob.getDescriptorFactory();
		
		assertEquals(new File(dirs.base(), "oddballs").getCanonicalPath(), 
				result.getBaseDir().getCanonicalPath());
	}
	
	public void testWithBalls() throws IOException {

		OurDirs dirs = new OurDirs();
		Main test = new Main();

		Oddjob oddjob = test.init(new String[] { 
			"-nb", "-oddballs", "someballs", 
			"-f", dirs.base() + "/oddjob.xml"}).getOddjob();
		
		OddballsDirDescriptorFactory result = (OddballsDirDescriptorFactory)
			oddjob.getDescriptorFactory();
		
		assertEquals(new File("someballs"), result.getBaseDir());
	}	
	
	public void testBadFile() {

		Main test = new Main();

		try {
			test.init(new String[] { 
				"-file", "iDontExist.xml" });
			fail("Should fail.");
		} catch (IOException e) {
			// expected
		}
		
	}
	
	public void testUserProperties() throws IOException {
		
		System.setProperty("test.snack", "pizza");
		
		System.getProperties().remove("test.fruit");
		assertEquals(null, System.getProperty("test.fruit"));
		
		File userProperties = new File(System.getProperty("user.home"), 
				Main.USER_PROPERTIES);
		
		File renamedFile = new File(userProperties.getPath() + "_testInProgress");
		// Delete old failed test.
		if (renamedFile.exists()) {
			assertTrue(renamedFile.delete());			
		}
		if (userProperties.exists()) {
			assertTrue(userProperties.renameTo(renamedFile));
		}
		
		PrintStream out = new PrintStream(new FileOutputStream(userProperties));
		
		out.println("test.fruit=apples");
		out.println("test.snack=fruit");
		out.println("test.empty=");
		
		out.close();
		
		Main test = new Main();
		
		test.processUserProperties();
		
		assertEquals("apples", System.getProperty("test.fruit"));
		assertEquals("pizza", System.getProperty("test.snack"));
		assertEquals("", System.getProperty("test.empty"));
		
		
		assertTrue(userProperties.delete());		
		
		if (renamedFile.exists()) {
			assertTrue(renamedFile.renameTo(userProperties));
		}
		
		System.getProperties().remove("test.snack");
		System.getProperties().remove("test.fruit");
		System.getProperties().remove("test.empty");
		
	}
	
	public void testOddjobDestroyOnComplete() throws IOException {
		
		File f = new OurDirs().relative("test/conf/simple-echo.xml");
	
		Main test = new Main();
		
		Oddjob oddjob = test.init(new String[] { 
				"-f", f.toString() }).getOddjob();
	
		oddjob.run();

		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());		
	}
	
	public void testOddjobDestroyOnComleteWithServices() throws IOException, InterruptedException {
		
		File f = new OurDirs().relative("test/conf/testflow2.xml");
	
		Main test = new Main();
		
		Oddjob oddjob = test.init(new String[] { 
				"-f", f.toString() }).getOddjob();
	
		StateSteps state = new StateSteps(oddjob);
		state.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.COMPLETE);
		
		oddjob.run();

		state.checkWait();		
	}
}
