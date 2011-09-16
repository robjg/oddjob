package org.oddjob.schedules;

import java.util.Date;

public interface ScheduleResult extends Interval {

	public Date getUseNext();
}
