/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.schedules;

import java.text.SimpleDateFormat;

import junit.framework.TestCase;

import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.xml.XMLConfiguration;

/**
 * Now a test that we don't need a specific ScheduleElement.
 * @author rob
 *
 */
public class ScheduleElementTest extends TestCase {

	
	public static class OurJob implements Runnable {
		
		Schedule schedule;
		
		@ArooaAttribute
		public void setSchedule(Schedule schedule) {
			this.schedule = schedule;
		}
		
		public Schedule getSchedule() {
			return schedule;
		}
		
		public void run() {
			// TODO Auto-generated method stub
			
		}
	}
	
	public void testParse() throws Exception {
		
		String xml = 
			"<oddjob xmlns:s='http://rgordon.co.uk/oddjob/schedules'>" +
			" <job>" +
			"  <sequential>" +
			"   <jobs>" +
			"    <variables id='vars'>" +
			"     <sched>" +
			"        <s:weekly on='2'/>" +
			"     </sched>" +
			"    </variables>" +
			"    <bean id='test' class='" + OurJob.class.getName() + "'" +
			"           schedule='${vars.sched}'/>" +
			"   </jobs>" +
			"  </sequential>" +
			" </job>" +
			"</oddjob>";

		Oddjob oddjob = new Oddjob();
		oddjob.setConfiguration(new XMLConfiguration("XML", xml));
		
		oddjob.run();
		
		Schedule schedule = (Schedule) new OddjobLookup(
				oddjob).lookup("test.schedule");
		
		ScheduleContext context = new ScheduleContext(	
			new SimpleDateFormat("dd-MMM-yyyy").parse("2-jun-2008"));
		
		Interval nextDue = schedule.nextDue(context);
				
		assertEquals(new SimpleDateFormat("dd-MMM-yyyy").parse("3-jun-2008"),
				nextDue.getFromDate());

	}
}
