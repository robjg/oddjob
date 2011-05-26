/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.values.types;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;
import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;

/**
 * @oddjob.description A type which can either format a
 * number or a date into the given text format.
 * <p>
 * Form more information on the number format see {@link DecimalFormat}
 * <p>
 * For more information on the date format see {@link SimpleDateFormat}
 * 
 * @oddjob.example
 * 
 * Formatting a date and number to create a file name.
 * 
 * {@oddjob.xml.resource org/oddjob/values/types/FormatTypeExample.xml}
 * 
 * @author Rob Gordon.
 */
public class FormatType implements ArooaValue, Serializable {
	private static final long serialVersionUID = 20070312;

	public static class Conversions implements ConversionProvider {
		
		public void registerWith(ConversionRegistry registry) {
	    	registry.register(FormatType.class, String.class, 
	    			new Convertlet<FormatType, String>() {
	    		public String convert(FormatType from) throws ConvertletException {
	    			return from.toFormattedString();
	    		}
	    	});
		}
	}
	
    /**
     * @oddjob.property 
     * @oddjob.description The format.
     * @oddjob.required Yes.
     */
    private String format;
    
    /**
     * @oddjob.property timeZone
     * @oddjob.description The time zone to use for a date format.
     * @oddjob.required No.
     */
    private TimeZone timeZone;
    
    /**
     * @oddjob.property
     * @oddjob.description A date to format.
     * @oddjob.required Yes if number isn't supplied.
     */
    private Date date;
    
    /**
     * @oddjob.property
     * @oddjob.description A number to format.
     * @oddjob.required Yes if date isn't supplied.
     */
    private Number number;

    String toFormattedString() {
    	if (format == null) {
    		return null;
    	}
        if (date != null) {
            DateFormat dateFormat = new SimpleDateFormat(format);
            if (timeZone != null) {
            	dateFormat.setTimeZone(timeZone);
            }
            return dateFormat.format(date);
        }
        if (number != null) {
            NumberFormat numberFormat = new DecimalFormat(format);
            return numberFormat.format(number);
        }
		return null;
    }
    
    @ArooaAttribute
    public void setDate(Date date) {
    	if (date == null) {
    		this.date = null;
    	}
    	else {
    		this.date = new Date(date.getTime());
    	}
    }
    
    public void setFormat(String format) {
        this.format = format;
    }

    public void setTimeZone(String timeZoneId) {
    	this.timeZone = TimeZone.getTimeZone(timeZoneId);
    }
    
    public void setNumber(Number number) {
        this.number = number;
    }
    
    public String toString() {
    	StringBuilder string = new StringBuilder();
    	if (format == null) {
    		string.append("No Format of ");
    	}
    	else {
    		string.append("Format " + format.toString() + " of ");
    	}
    	if (date != null) {
    		string.append(date.toString());
    	}
    	else if (number != null) {
    		string.append(number.toString());
    	}
    	else {
    		string.append("null");
    	}
    	return string.toString();
    }
}
