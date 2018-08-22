/*
 * (c) Rob Gordon 2005
 */
package org.oddjob;
import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.arooa.logging.Appender;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.utils.Try;
import org.oddjob.oddballs.OddballsDirDescriptorFactory;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OurDirs;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class MainTest extends OjTestCase {

	private static final Logger logger = LoggerFactory.getLogger(MainTest.class);
	
	String oddjobHome;
	
    @Before
    public void setUp() throws Exception {
		logger.debug("------------------ " + getName() + " -----------------");
		logger.debug("Ant file is " + System.getProperty("ant.file"));
		
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
	public void testInit() throws Exception {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { 
				"-f", dirs.base() + "/oddjob.xml",  "x" } )
				.get().orElseThrow();
		
		assertEquals(1, oj.getArgs().length);
		assertEquals("x", oj.getArgs()[0]);
	}
	
   @Test
	public void testBadArg() throws Exception {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { "-f", dirs.base() + "/oddjob.xml", 
				"x", "-f", "something-else.xml" })
				.get().orElseThrow();
		
		assertEquals(3, oj.getArgs().length);
		assertEquals("x", oj.getArgs()[0]);
		assertEquals("-f", oj.getArgs()[1]);
		assertEquals("something-else.xml", oj.getArgs()[2]);
	}
		
   @Test
	public void testPassArgs() throws Exception {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { 
				"-f", dirs.base() + "/oddjob.xml", "--", 
				"-f", "something-else.xml" } )
				.get().orElseThrow();
		
		assertEquals(2, oj.getArgs().length);
		assertEquals("-f", oj.getArgs()[0]);
		assertEquals("something-else.xml", oj.getArgs()[1]);
	}
		
   @Test
	public void testOddjobName() throws Exception {
		OurDirs dirs = new OurDirs();
		
		Main m = new Main();
		Oddjob oj = m.init(new String[] { 
				"-n", "Test Jobs", "-f", dirs.base() + 
				"/oddjob.xml"} )
				.get().orElseThrow();
		
		assertEquals("Test Jobs", oj.toString());
	}
		
   @Test
	public void testUsage() throws Exception {
		
		Main.main(new String[] { "-h" });
	}
	
   @Test
	public void testVersion() throws Exception {
		
		Main.main(new String[] { "-version" });
	}
	
   @Test
	public void testInitNoBalls() throws Exception {

		OurDirs dirs = new OurDirs();
		
		Main test = new Main();

		Oddjob oddjob = test.init(new String[] { 
			"-nb", 
			"-f", dirs.base() + "/oddjob.xml" })
				.get().orElseThrow();
		
		assertNull(oddjob.getDescriptorFactory());
	}
	
   @Test
	public void testDefaultBalls() throws Exception {

		OurDirs dirs = new OurDirs();
		
		Main test = new Main();

		System.setProperty("oddjob.home", 
				dirs.base().getCanonicalPath());
		
		Oddjob oddjob = test.init(new String[] { })
				.get().orElseThrow();
		
		OddballsDirDescriptorFactory result = (OddballsDirDescriptorFactory)
			oddjob.getDescriptorFactory();
		
		assertEquals(new File(dirs.base(), "oddballs").getCanonicalPath(), 
				result.getBaseDir().getCanonicalPath());
	}
	
   @Test
	public void testWithBalls() throws Exception {

		OurDirs dirs = new OurDirs();
		Main test = new Main();

		Oddjob oddjob = test.init(new String[] { 
			"-nb", "-oddballs", "someballs", 
			"-f", dirs.base() + "/oddjob.xml"})
				.get().orElseThrow();
		
		OddballsDirDescriptorFactory result = (OddballsDirDescriptorFactory)
			oddjob.getDescriptorFactory();
		
		assertEquals(new File("someballs"), result.getBaseDir());
	}	
	
   @Test
	public void testBadFile() throws Exception {

	   
	   List<String> errors = new ArrayList<>();
	
	   Appender appender = e -> { 
		   if (e.getLevel() == LogLevel.ERROR) {
			   errors.add(e.getThrowable().getMessage()); 
		   }
		   return; 
	   };
	   
	   LoggerAdapter.appenderAdapterFor(OddjobBuilder.class).addAppender(appender);
	   
	   Main test = new Main();

	   Try<Oddjob> result = test.init(new String[] { "-file", "iDontExist.xml" }).get();
	
	   LoggerAdapter.appenderAdapterFor(OddjobBuilder.class).removeAppender(appender);
		
	   try {
		   result.orElseThrow();
		   fail("Should fail");
	   }
	   catch (Exception e) {
		   assertThat(e.getMessage().contains("iDontExist.xml"), is(true));
	   }

	   assertThat(errors.size(), is(0));		
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
	public void testOddjobDestroyOnComplete() throws Exception {
		
		File f = new OurDirs().relative("test/conf/simple-echo.xml");
	
		Main test = new Main();
		
		Oddjob oddjob = test.init(new String[] { 
				"-f", f.toString() })
				.get().orElseThrow();
	
		oddjob.run();

		assertEquals(ParentState.COMPLETE, 
				oddjob.lastStateEvent().getState());		
	}
	
   @Test
	public void testOddjobDestroyOnComleteWithServices() throws Exception {
		
		File f = new OurDirs().relative("test/conf/testflow2.xml");
	
		Main test = new Main();
		
		Oddjob oddjob = test.init(new String[] { 
				"-f", f.toString() })
				.get().orElseThrow();
	
		StateSteps state = new StateSteps(oddjob);
		state.startCheck(ParentState.READY, ParentState.EXECUTING, 
				ParentState.COMPLETE);
		
		oddjob.run();

		state.checkWait();		
	}
	
}
