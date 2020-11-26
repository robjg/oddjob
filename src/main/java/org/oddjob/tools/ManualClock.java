/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.tools;

import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.util.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

/**
 * A Clock useful for testing.
 *
 * @author Rob Gordon.
 */
public class ManualClock extends java.time.Clock implements Clock {

    private static final Logger logger = LoggerFactory.getLogger(ManualClock.class);
    
    private ZoneId zone;

    private Instant instant;

    public ManualClock(String time) {
    	setDateText(time);
    }
    
    public ManualClock() {
    }

    protected ManualClock(Instant instant, ZoneId zone) {
        this.instant = instant;
        this.zone = zone;
    }

    public void setDateText(String time) {
        logger.debug("Setting date [" + time + "]");
        try {
        	setDate(DateHelper.parseDateTime(time));
        }
        catch (ParseException e) {
        	throw new RuntimeException(e);
        }
    }

    public void setDate(Date date) {
    	this.instant = date.toInstant();
    }
    
    public Date getDate() {
        return Date.from(instant());
    }
    
    @Override
    public String toString() {
    	return getClass().getSimpleName() + " "
                + ZonedDateTime.ofInstant(instant(), getZone());
    }

    public void setZone(ZoneId zone) {
        this.zone = zone;
    }

    public Instant getInstant() {
        return instant;
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Override
    public ZoneId getZone() {
        return Optional.ofNullable(this.zone).orElse(ZoneId.systemDefault());
    }

    @Override
    public java.time.Clock withZone(ZoneId zone) {
        return ManualClock.fromInstant(this.instant).andZoneId(zone);
    }

    @Override
    public Instant instant() {
        return Optional.ofNullable(this.instant).orElse(Instant.now());
    }

    public static class Builder {
        private final Instant instant;

        public Builder(Instant instant) {
            this.instant = instant;
        }

        public ManualClock andSystemZone() {
            return andZoneId(null);
        }

        public ManualClock andZoneId(ZoneId zoneId) {
            return new ManualClock(instant, zoneId);
        }
    }

    public static Builder fromInstant(Instant instant) {
        return new Builder(instant);
    }
}
