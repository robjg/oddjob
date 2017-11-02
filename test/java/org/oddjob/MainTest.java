/*
 * (c) Rob Gordon 2005
 */
package org.oddjob;
import org.junit.Before;
import org.junit.After;

import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import org.oddjob.OjTestCase;

import org.apache.log4j.Logger;
import org.oddjob.oddballs.OddballsDirDescriptorFactory;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OurDirs;
import org.oddjob.tools.StateSteps;

/**
 * 
 */
public class MainTest extends OjTestCase {

	private static final Logger logger = Logger.getLogger(MainTest.class);
	
	String oddjobHome;
	
    @Before
    public void setUp() throws Exception {
		logger.debug("------------------ " + getName() + " -----------------");
		logger.debug(System.getProperty("ant.file"));
		
		oddjobHome = System.getProperty("oddjob.home");
		System.getProperties().remove("oddjob.home");
	}
	
    @After
   public void tearDown() throws Exception {
		if (oddjobHome == null) {
			System.getProperties().remove("oddjob.home");
		}
		else {
			System.setProperty("oddjob.home", oddjobHome);
		}
	}
	
	// test oddjob args past through
   @Test
	public void testInit() throws IOException {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { 
				"-f", dirs.base() + "/oddjob.xml",  "x" } );
		
		assertEquals(1, oj.getArgs().length);
		assertEquals("x", oj.getArgs()[0]);
	}
	
   @Test
	public void testBadArg() throws IOException {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { "-f", dirs.base() + "/oddjob.xml", 
				"x", "-f", "something-else.xml" });
		
		assertEquals(3, oj.getArgs().length);
		assertEquals("x", oj.getArgs()[0]);
		assertEquals("-f", oj.getArgs()[1]);
		assertEquals("something-else.xml", oj.getArgs()[2]);
	}
		
   @Test
	public void testPassArgs() throws IOException {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { 
				"-f", dirs.base() + "/oddjob.xml", "--", 
				"-f", "something-else.xml" } );
		
		assertEquals(2, oj.getArgs().length);
		assertEquals("-f", oj.getArgs()[0]);
		assertEquals("something-else.xml", oj.getArgs()[1]);
	}
		
   @Test
	public void testOddjobName() throws IOException {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { 
				"-n", "Test Jobs", "-f", dirs.base() + 
				"/oddjob.xml"} );
		
		assertEquals("Test Jobs", oj.toString());
	}
		
   @Test
	public void testUsage() throws IOException {
		
		Main.main(new String[] { "-h" });
	}
	
   @Test
	public void testVersion() throws IOException {
		
		Main.main(new String[] { "-version" });
	}
	
   @Test
	public void testInitNoBalls() throws IOException {

		OurDirs dirs = new OurDirs();
		
		Main test = new Main();

		Oddjob oddjob = test.init(new String[] { 
			"-nb", 
			"-f", dirs.base() + "/oddjob.xml" });
		
		assertNull(oddjob.getDescriptorFactory());
	}
	
   @Test
	public void testDefaultBalls() throws IOException {

		OurDirs dirs = new OurDirs();
		
		Main test = new Main();

		System.setProperty("oddjob.home", 
				dirs.base().getCanonicalPath());
		
		Oddjob oddjob = test.init(new String[] { });
		
		OddballsDirDescriptorFactory result = (OddballsDirDescriptorFactory)
			oddjob.getDescriptorFactory();
		
		assertEquals(new File(dirs.base(), "oddballs").getCanonicalPath(), 
				result.getBaseDir().getCanonicalPath());
	}
	
   @Test
	public void testWithBalls() throws IOException {

		OurDirs dirs = new OurDirs();
		Main test = new Main();

		Oddjob oddjob = test.init(new String[] { 
			"-nb", "-oddballs", "someballs", 
			"-f", dirs.base() + "/oddjob.xml"});
		
		OddballsDirDescriptorFactory result = (OddballsDirDescriptorFactory)
			oddjob.getDescriptorFactory();
		
		assertEquals(new File("someballs"), result.getBaseDir());
	}	
	
   @Test
	public void testBadFile() {

		Main test = new Main();

		try {
			test.init(new String[] { 
				"-file", "iDontExist.xml" });
			fail("Should fail.");
		} catch (IOException e) {
			assertTrue("File name in message", e.getMessage().contains("iDontExist.xml"));
		}
		
	}
	
   @Test
	public void testUserProperties() throws IOException {
				
		File userProperties = new File(System.getProperty("user.home"), 
				Main.USER_PROPERTIES);
		
		File renamedFile = new File(userProperties.getPath() + "_testInProgress");

		// if an old test failed the _testInProgress file are the properties
		// we want to keep.
		if (!renamedFile.exists() && userProperties.exists()) {
			assertTrue(userProperties.renameTo(renamedFile));
		}
		
		PrintStream out = new PrintStream(new FileOutputStream(userProperties));
		
		out.println("test.fruit=apples");
		out.println("test.snack=fruit");
		out.println("test.empty=");
		
		out.close();
		
		Main test = new Main();
		
		Properties props = test.processUserProperties();
		
		assertEquals("apples", props.getProperty("test.fruit"));
		assertEquals("fruit", props.getProperty("test.snack"));
		assertEquals("", props.getProperty("test.empty"));
		
		
		assertTrue(userProperties.delete());		
		
		if (renamedFile.exists()) {
			assertTrue(renamedFile.renameTo(userProperties));
		}		
	}
	
   @Test
	public void testOddjobDestroyOnComplete() throws IOException {
		
		File f = new OurDirs().relative("test/conf/simple-echo.xml");
	
		Main test = new Main();
		
		Oddjob oddjob = test.init(new String[] { 
				"-f", f.toString() });
	
		oddjob.run();

		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());		
	}
	
   @Test
	public void testOddjobDestroyOnComleteWithServices() throws IOException, InterruptedException {
		
		File f = new OurDirs().relative("test/conf/testflow2.xml");
	
		Main test = new Main();
		
		Oddjob oddjob = test.init(new String[] { 
				"-f", f.toString() });
	
		StateSteps state = new StateSteps(oddjob);
		state.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.COMPLETE);
		
		oddjob.run();

		state.checkWait();		
	}
	
}
