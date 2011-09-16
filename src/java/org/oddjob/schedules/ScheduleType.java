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

/**
 * 
 * @oddjob.description The date now.
 * <p>
 * @oddjob.example
 * 
 * Display the time now. Note the clock variable is passed into oddjob during 
 * the testing of this example so the time can be fixed, but run as is it
 * will be null and so the current time will be displayed.
 *
 * {@oddjob.xml.resource org/oddjob/values/types/NowExample.xml}
 * 
 * @author Rob
 */
public class ScheduleType implements ArooaValue, ArooaLifeAware {

	private static final Logger logger = Logger.getLogger(ScheduleType.class);
	
	public static class Conversions implements ConversionProvider {
		public void registerWith(ConversionRegistry registry) {
			registry.register(ScheduleType.class, Interval.class, new Convertlet<ScheduleType, Interval>() {
				public Interval convert(ScheduleType from) throws ConvertletException {
					return from.current;
				}
			});
			registry.register(ScheduleType.class, Date.class, new Convertlet<ScheduleType, Date>() {
				public Date convert(ScheduleType from) throws ConvertletException {
					Interval interval = from.current;
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
	 * @oddjob.description The clock to use. 
	 * @oddjob.required. No. Defaults to the system clock.
	 */
	private Date date;

	private TimeZone timeZone;
	
	private Schedule schedule;
	
	private volatile Interval current;
	
    public Interval getCurrent() {
		return current;
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
		
		current = schedule.nextDue(context);
		
		logger.info("Calculated interval " + current + 
				" from date " + date);
		
	}
	
	@Override
	public void destroy() {
		current = null;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
