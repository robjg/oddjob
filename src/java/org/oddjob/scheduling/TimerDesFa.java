/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.scheduling;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.designer.components.BaseDC;

/**
 *
 */
public class TimerDesFa implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new TimerDesign(element, parentContext);
	}
}

class TimerDesign extends BaseDC {
	
	private final SimpleDesignProperty schedule;

	private final SimpleTextAttribute timeZone;
	
	private final SimpleTextAttribute haltOnFailure;
	
	private final SimpleTextAttribute skipMissedRuns;
	
	private final SimpleTextAttribute clock;
	
	private final SimpleDesignProperty job;
	
	public TimerDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		schedule = new SimpleDesignProperty(
				"schedule", this);

		timeZone = new SimpleTextAttribute("timeZone", this);
		
		haltOnFailure = new SimpleTextAttribute("haltOnFailure", this);
		
		skipMissedRuns = new SimpleTextAttribute("skipMissedRuns", this);
		
		clock = new SimpleTextAttribute("clock", this);
		
		job = new SimpleDesignProperty(
				"job", this);
	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] { name, 
				schedule, timeZone, haltOnFailure, 
				skipMissedRuns, clock, job };
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())	
			.addFormItem(
					new BorderedGroup("Timer Details")
					.add(schedule.view().setTitle("Schedule"))
					.add(timeZone.view().setTitle("Time Zone"))
					.add(haltOnFailure.view().setTitle("Halt On Failure"))
					.add(skipMissedRuns.view().setTitle("Skip Missed Runs"))
					.add(clock.view().setTitle("Clock"))
					.add(job.view().setTitle("Job"))
			);
	}
	
}

