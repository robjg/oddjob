package org.oddjob.schedules.schedules;

import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

import org.oddjob.OjTestCase;

import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.schedules.Interval;
import org.oddjob.schedules.IntervalTo;
import org.oddjob.schedules.Schedule;
import org.oddjob.schedules.ScheduleContext;
import org.oddjob.schedules.ScheduleResult;
import org.oddjob.schedules.SimpleScheduleResult;

public class ParentChildScheduleTest extends OjTestCase {

   @Test
	public void testParentNoChild() throws ParseException {
		
		final ScheduleResult parentResult = new IntervalTo(
				DateHelper.parseDate("2011-09-19"),
				DateHelper.parseDate("2011-09-24"));

		class ParentSchedule implements Schedule {
			
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				return parentResult;
			}
		}
		
		
		ParentChildSchedule test = new ParentChildSchedule(
				new ParentSchedule(), null);
		
		ScheduleResult result = test.nextDue(new ScheduleContext(
				new Date()));
		
		assertEquals(parentResult, result);
	}
	
   @Test
	public void testParentAndChildChildNotLimited() throws ParseException {
		
		final ScheduleResult parentResult = new IntervalTo(
				DateHelper.parseDate("2011-09-19"),
				DateHelper.parseDate("2011-09-24"));

		class ParentSchedule implements Schedule {
			
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				return parentResult;
			}
		}
		
		final ScheduleResult childResult = new IntervalTo(
				DateHelper.parseDate("2011-09-01"),
				DateHelper.parseDate("2011-09-02"));

		class ChildSchedule implements Schedule {
			
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				return childResult;
			}
		}
		
		ParentChildSchedule test = new ParentChildSchedule(
				new ParentSchedule(), new ChildSchedule());
		
		ScheduleResult result = test.nextDue(new ScheduleContext(
				new Date()));
		
		assertEquals(childResult, result);
	}
		
   @Test
	public void testParentNoChildWithLimits() throws ParseException {
		
		final ScheduleResult parentResult = new IntervalTo(
				DateHelper.parseDate("2011-09-01"),
				DateHelper.parseDate("2011-09-02"));

		class ParentSchedule implements Schedule {
			
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				return parentResult;
			}
		}
		
		ParentChildSchedule test = new ParentChildSchedule(
				new ParentSchedule(), null);
		
		ScheduleContext context = new ScheduleContext(
				new Date());
		
		Interval limit = new IntervalTo(
				DateHelper.parseDate("2011-09-19"),
				DateHelper.parseDate("2011-09-24"));

		context = context.spawn(limit);
		
		ScheduleResult result = test.nextDue(context);
		
		assertEquals(null, result);
	}
	
   @Test
	public void testParentWhenChildReturnsNull() throws ParseException {
		
		final ScheduleResult parentResult = new IntervalTo(
				DateHelper.parseDate("2011-09-19"),
				DateHelper.parseDate("2011-09-24"));

		class ParentSchedule implements Schedule {
			
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				return parentResult;
			}
		}
		
		class ChildSchedule implements Schedule {
			
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				return null;
			}
		}
		
		ParentChildSchedule test = new ParentChildSchedule(
				new ParentSchedule(), new ChildSchedule());
		
		ScheduleResult result = test.nextDue(new ScheduleContext(
				new Date()));
		
		assertEquals(null, result);
	}
	
   @Test
	public void testParentAndChildWhenChildUseNextNull() throws ParseException {
		
		final ScheduleResult parentResult = new IntervalTo(
				DateHelper.parseDate("2011-09-19"),
				DateHelper.parseDate("2011-09-24"));

		class ParentSchedule implements Schedule {
			
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				return parentResult;
			}
		}
		
		final ScheduleResult childResult = new SimpleScheduleResult(
				new IntervalTo(
					DateHelper.parseDate("2011-09-01"),
					DateHelper.parseDate("2011-09-02")),
				null);

		class ChildSchedule implements Schedule {
			
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				return childResult;
			}
		}
		
		ParentChildSchedule test = new ParentChildSchedule(
				new ParentSchedule(), new ChildSchedule());
		
		ScheduleResult result = test.nextDue(new ScheduleContext(
				new Date()));
		
		ScheduleResult expected = new SimpleScheduleResult(
				new IntervalTo(
					DateHelper.parseDate("2011-09-01"),
					DateHelper.parseDate("2011-09-02")),
				childResult.getToDate());
		
		assertEquals(expected, result);
	}
	
   @Test
	public void testParentAndChildWhenChildUseNextNullInParentNextInterval() throws ParseException {
		
		final ScheduleResult parentResult1 = new IntervalTo(
				DateHelper.parseDate("2011-09-19"),
				DateHelper.parseDate("2011-09-24"));

		final ScheduleResult parentResult2 = new IntervalTo(
				DateHelper.parseDate("2011-10-19"),
				DateHelper.parseDate("2011-10-24"));
		
		final Date firstDate = DateHelper.parseDate("2011-09-20");
		
		class ParentSchedule implements Schedule {
			
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				if (context.getDate().equals(firstDate)) {
					return parentResult1;
				}
				else {
					return parentResult2;
				}
			}
		}
		
		final ScheduleResult childResult = new SimpleScheduleResult(
				new IntervalTo(
					DateHelper.parseDate("2011-11-01"),
					DateHelper.parseDate("2011-11-02")),
				null);

		
		class ChildSchedule implements Schedule {
			
			@Override
			public ScheduleResult nextDue(ScheduleContext context) {
				if (context.getDate().equals(firstDate)) {
					return null;
				}
				else {
					return childResult;
				}
			}
		}
		
		ParentChildSchedule test = new ParentChildSchedule(
				new ParentSchedule(), new ChildSchedule());
		
		ScheduleResult result = test.nextDue(new ScheduleContext(
				firstDate));
		
		ScheduleResult expected = new SimpleScheduleResult(
				new IntervalTo(
					DateHelper.parseDate("2011-11-01"),
					DateHelper.parseDate("2011-11-02")),
				childResult.getToDate());
		
		assertEquals(expected, result);
	}
}
