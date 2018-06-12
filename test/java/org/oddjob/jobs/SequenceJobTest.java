/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jobs;
import org.junit.Before;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.oddjob.OjTestCase;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.FailedToStopException;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.framework.adapt.job.RunnableProxyGenerator;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.OurDirs;

public class SequenceJobTest extends OjTestCase {

	private static final Logger logger = LoggerFactory.getLogger(SequenceJobTest.class);
	
    @Before
    public void setUp() throws Exception {
		logger.debug("------------------ " + getName() + " -----------------");
	}

   @Test
	public void testSerialize() throws Exception {
		SequenceJob test = new SequenceJob();
		test.setFrom(22);
		
		test.run();
		
		assertEquals(new Integer(22), test.getCurrent());
	
		SequenceJob copy = (SequenceJob) OddjobTestHelper.copy(test);
		
		assertEquals(new Integer(22), copy.getCurrent());
	}
	
   @Test
	public void testSerializedByWrapper() throws Exception {
		SequenceJob test = new SequenceJob();
		test.setFrom(22);
		
        Runnable proxy = (Runnable) new RunnableProxyGenerator().generate(
    			(Runnable) test,
    			getClass().getClassLoader());  
		
		proxy.run();
		
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(proxy));
			
		Object copy = OddjobTestHelper.copy(proxy);
		
		assertEquals(JobState.COMPLETE, OddjobTestHelper.getJobState(copy));
	}
	
   @Test
	public void testDescribe() {
		
		SequenceJob test = new SequenceJob();
				
		test.run();
		
		Map<String, String> m = new UniversalDescriber(
				new StandardArooaSession()).describe(test);
		
		String current = (String)m.get("current");
		assertEquals("0", current);
	}
	
   @Test
	public void testSequenceExample() throws IOException, ArooaPropertyException, ArooaConversionException, InterruptedException, FailedToStopException {
		
		OurDirs dirs = new OurDirs();
		
		File workDir = dirs.relative("work/sequence");
		
		if (workDir.exists()) {
			FileUtils.forceDelete(workDir);
		}
		workDir.mkdir();
				
		Properties properties = new Properties();
		properties.setProperty("work.dir", workDir.getPath());
		
		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration(
				"org/oddjob/jobs/SequenceInFileNameExample.xml", 
				getClass().getClassLoader()));
		oddjob.setProperties(properties);
				
		oddjob.run();

		Date now = new Date(new Date().getTime() + 1);
		while (true) {
			Date next = (Date) new OddjobLookup(oddjob).lookup(
					"daily.nextDue", Date.class);
			
			if (next.after(now)) {
				break;
			}
			
			logger.info("Waiting for daily to move forward.");
			
			Thread.sleep(100);
		}
		
		oddjob.stop();
		
		assertEquals(ParentState.READY,
				oddjob.lastStateEvent().getState());
		
		assertTrue(new File(workDir, "sequence0009.txt").exists());
		
		oddjob.destroy();
	}
	

	
}
