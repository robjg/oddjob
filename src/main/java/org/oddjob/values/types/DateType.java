/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.values.types;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.types.ValueType;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.util.Clock;
import org.oddjob.util.DateProvider;
import org.oddjob.util.DateShortcuts;
import org.oddjob.util.DefaultClock;

/**
 * @oddjob.description Define a Date.
 * <p>
 * Oddjob's inbuilt conversion allows a date to be specified as text in
 * any of these formats:</p>
 * <dl>
 * <dt>yyyy-MM-dd</dt><dd>Just the date.</dd>
 * <dt>yyyy-MM-dd HH:mm</dt><dd>The date, hours and minutes.</dd>
 * <dt>yyyy-MM-dd HH:mm:ss</dt><dd>The date, hours, minutes and seconds.</dd>
 * <dt>yyyy-MM-dd HH:mm:ss.SSS</dt><dd>The date, hours, minutes, seconds 
 * and milliseconds.</dd>
 * </dl>
 * <p>Because of this a date property of a job can be specified perfectly
 * easily as a {@link ValueType} or a property. However there are two situations
 * when this is inadequate:</p>
 * <ul>
 * <li>The text format of the date is not in one of the formats above.</li>
 * <li>The date must be specified in a different time zone.</li>
 * </ul>
 * <p>In either or both of these cases the date type can be used.</p>
 * <p>
 * This date type can also be used to specify a java Calendar property which
 * Oddjob's inbuilt conversion will currently not do from text.
 * <p>
 * Since v1.3 The date can also be specified using one of these shortcuts:
 * <dl>
 * <dt>NOW</dt><dd>The date and time now.</dd>
 * <dt>TODAY</dt><dd>The date as of midnight.</dd>
 * <dt>YESTERDAY</dt><dd>The date yesterday at midnight.</dd>
 * <dt>TOMORROW</dt><dd>The date tomorrow at midnight.</dd>
 * </dl>
 * 
 * @oddjob.example A simple example of specifying a date.
 *
 * {@oddjob.xml.resource org/oddjob/values/types/SimpleDateExample.xml}
 *
 * @oddjob.example Specifying a date in a different format.
 * 
 * {@oddjob.xml.resource org/oddjob/values/types/DateFormatExample.xml}
 *
 * @oddjob.example Adjusting a date by Time Zone.
 *
 * {@oddjob.xml.resource org/oddjob/values/types/DateWithTimeZoneExample.xml#sequential}
 *
 * @oddjob.example Date shortcuts.
 *
 * {@oddjob.xml.resource org/oddjob/values/types/DateShortcutsExample.xml}
 *
 * @author Rob Gordon.
 */
public class DateType implements ArooaValue, Serializable {
	private static final long serialVersionUID = 20070312;
	
	public static class Conversions implements ConversionProvider {
		public void registerWith(ConversionRegistry registry) {
	    	registry.register(DateType.class, Date.class, 
	    			new Convertlet<DateType, Date>() {
	    		public Date convert(DateType from) throws ConvertletException {
	    			try {
	    				return from.toDate();
	    			} catch (ParseException e) {
	    				throw new ConvertletException(e);
	    			}
	       		}
	    	});
	    	
	    	registry.register(DateType.class, Calendar.class, 
	    			new Convertlet<DateType, Calendar>() {
	    		public Calendar convert(DateType from) throws ConvertletException {
	    			try {
	    				return from.toCalandar();
	    			} catch (ParseException e) {
	    				throw new ConvertletException(e);
	    			}

	    		}
	    	});
	    	
		}
	}
	
	
    /**
     * @oddjob.property
     * @oddjob.description A date in text, if a format is specified it is
     * expected to be in the format provide, otherwise it is expected
     * to be in the default format..
     * @oddjob.required Yes.
     */
    private String date;

    /**
     * @oddjob.property
     * @oddjob.description The format the date is in.
     * @oddjob.required No.
     */
    private String format;

    /**
     * @oddjob.property
     * @oddjob.description The time zone the date is for.
     * @oddjob.required No. 
     */
    private String timeZone;
	    
    
    /**
     * @oddjob.property
     * @oddjob.description The clock to use if a date shortcut is
     * specified. This is mainly here for tests.
     * @oddjob.required No, defaults to the current time clock.  
     */
    private Clock clock;
    
    public Calendar toCalandar() throws ParseException {
		Date date = toDate();
		
		if (date == null) {
			return null;
		}
		
		TimeZone tz = TimeZone.getDefault(); 
		if (timeZone != null) {
			tz = TimeZone.getTimeZone(timeZone);
		}
		Calendar cal =Calendar.getInstance(tz);
		cal.setTime(date);
		return cal;
    }
    
    /**
     * Convert this type to a date.
     * 
     * @return A date. May be null if the date property is null.
     * 
     * @throws ParseException
     */
    public Date toDate() throws ParseException {
    	
    	if (date == null) {
    		return null;
    	} 
    	
    	TimeZone theTimeZone = null;
    	if (timeZone != null) {
    		theTimeZone = TimeZone.getTimeZone(timeZone);
    	}
    			
    	if (format == null) {
    		DateProvider provider = DateShortcuts.getShortcut(date);
    		if (provider != null) {
    			return provider.dateFor(getClock(), theTimeZone);
    		}
    		else {
    			return DateHelper.parseDateTime(date, theTimeZone);
    		}
    	}
    	else {
	    	SimpleDateFormat sdf = new SimpleDateFormat(format);
	    	if (theTimeZone != null) {
	    		sdf.setTimeZone(theTimeZone);
	    	}
	    	return sdf.parse(date);
    	}
    }
    
    public void setDate(String date) {
    	this.date = date;
    }
    
    public String getDate() {
    	return date;
    }
    
    public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setTimeZone(String timeZoneId) {
        this.timeZone = timeZoneId;
    }
    
    public String getTimeZone() {
    	return timeZone;
    }
    
	public Clock getClock() {
		if (clock == null) {
			return new DefaultClock();
		}
		else {
			return clock;
		}
	}

	public void setClock(Clock clock) {
		this.clock = clock;
	}
	
    public String toString() {
    	return "DateType: " + date;
    }

}
