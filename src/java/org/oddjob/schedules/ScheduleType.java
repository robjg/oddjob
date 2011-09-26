package org.oddjob.schedules;

import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.life.ArooaLifeAware;
import org.oddjob.arooa.utils.DateHelper;

/**
 * 
 * @oddjob.description Applies a schedule to a given date to provide a calculated date. 
 * If the date is not provide the current date is used. This type will most often be 
 * used to calculate the current date or the current time, or a next business date.
 * <p>
 * The {@link ScheduleResult} is also available and this can be used to calculate 
 * recurring schedules as in the example below.
 * <p>
 * 
 * @oddjob.example
 * 
 * Display the time now. Note the date variable is passed into Oddjob during 
 * the testing of this example so the time can be fixed, but run as is it
 * will be null and so the current time will be displayed.
 *
 * {@oddjob.xml.resource org/oddjob/schedules/ScheduleTypeExample.xml}
 * 
 * @oddjob.example
 * 
 * Use a schedule with a time zone. This example displays when tomorrow starts
 * in Hong Kong in the local time. 
 *
 * {@oddjob.xml.resource org/oddjob/schedules/ScheduleTypeWithTimeZone.xml}
 * 
 * Note that to display the current time in Hong Kong
 * we would use a Time Zone on the format type, not on the now schedule because
 * dates internally use UTC (Coordinated Universal Time) so now will always be 
 * the same regardless of time zone.
 * 
 * @oddjob.example
 * 
 * Calculate the next business date. Two schedule types are used, the first calculates 
 * the next day, the next takes this and applies it to a schedule that defines the 
 * business days. The result will be the next business day.
 *
 * {@oddjob.xml.resource org/oddjob/schedules/ScheduleTypeNextBusinessDay.xml}
 * 
 * @oddjob.example
 * 
 * Display the due dates for a recurring schedule. This would be useful for 
 * experimenting with schedules.
 *
 * {@oddjob.xml.resource org/oddjob/schedules/ScheduleTypeForEach.xml}
 * 
 * @author Rob
 */
public class ScheduleType implements ArooaValue, ArooaLifeAware {

	private static final Logger logger = Logger.getLogger(ScheduleType.class);
	
	public static class Conversions implements ConversionProvider {
		public void registerWith(ConversionRegistry registry) {
			registry.register(ScheduleType.class, Interval.class, new Convertlet<ScheduleType, Interval>() {
				public Interval convert(ScheduleType from) throws ConvertletException {
					return from.result;
				}
			});
			registry.register(ScheduleType.class, Date.class, new Convertlet<ScheduleType, Date>() {
				public Date convert(ScheduleType from) throws ConvertletException {
					Interval interval = from.result;
					if (interval == null) {
						return null;
					}
					else {
						return interval.getFromDate();
					}
				}
			});
		}
	}
	
	/**
	 * @oddjob.property
	 * @oddjob.description The Date to use. 
	 * @oddjob.required. No. Defaults to the current time.
	 */
	private Date date;

	/**
	 * @oddjob.property
	 * @oddjob.description The time zone to apply the schedule for. 
	 * @oddjob.required. No.
	 */
	private TimeZone timeZone;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The schedule to use. 
	 * @oddjob.required. Yes.
	 */
	private Schedule schedule;
	
	/**
	 * @oddjob.property
	 * @oddjob.description The result of applying the schedule which is a 
	 * ScheduleResult bean that has the properties fromDate, toDate and 
	 * useNext. 
	 * @oddjob.required. Read Only.
	 */
	private volatile ScheduleResult result;
	
    public ScheduleResult getResult() {
		return result;
	}

	public Schedule getSchedule() {
		return schedule;
	}

	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date clock) {
		this.date = clock;
	}

	/**
	 * Get the time zone id to use in this schedule.
	 * 
	 * @return The time zone id being used.
	 */
	public String getTimeZone() {
		if (timeZone == null) {
			return null;
		}
		return timeZone.getID();
	}

	/**
	 * Set the time zone.
	 * 
	 * @param timeZoneId the timeZoneId.
 	 */
	public void setTimeZone(String timeZoneId) {
		if (timeZoneId == null) {
			this.timeZone = null; 
		} else {
			this.timeZone = TimeZone.getTimeZone(timeZoneId);
		}
	}
			
	@Override
	public void initialised() {
	}
	
	@Override
	public void configured() {
		if (schedule == null) {
			throw new IllegalStateException("No Schedule!");
		}
		
		Date date = this.date;
		if (date == null) {
			date = new Date();
		}
		
		ScheduleContext context = new ScheduleContext(
				date, timeZone);
		
		result = schedule.nextDue(context);
		
		logger.info("Calculated interval " + result + 
				" from date " + date);
		
	}
	
	@Override
	public void destroy() {
		result = null;
	}
	
	@Override
	public String toString() {
		String interval = "";
		if (result != null) {
			interval = " date is " + DateHelper.formatDateTimeInteligently(
					result.getFromDate());
		}
			
		return getClass().getSimpleName() + interval;
	}
}
