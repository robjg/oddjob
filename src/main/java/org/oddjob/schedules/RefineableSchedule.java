package org.oddjob.schedules;

/**
 * Interface for a schedule that can be refined by the addition of
 * a sub schedule. This is designed to aid applications which are
 * building a schedule from some kind of configuration file.
 */
public interface RefineableSchedule extends Schedule {

	/**
	 * Add a child schedule of the given name. The implementing class
	 * will typically use a factory to create the schedule, add it to
	 * it's list of child schedules, and return it so that it's attributes
	 * may be set by the calling application.
	 * 
	 * @param schedule The child schedule.
	 */
	public void setRefinement(Schedule refinement);
}
