/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.IndexedDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class IfDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new IfDesign(element, parentContext);
	}
		
}

class IfDesign extends BaseDC {

	private final SimpleTextAttribute state;
	
	private final IndexedDesignProperty jobs;

	public IfDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		state = new SimpleTextAttribute("state", this);
		
		jobs = new IndexedDesignProperty("jobs", this);		
	}
	
	
	public Form detail() {		
		return new StandardForm(this)
				.addFormItem(basePanel())
				.addFormItem(new BorderedGroup("Properties")
					.add(state.view().setTitle("State"))
					.add(jobs.view().setTitle("Jobs"))
				);
	}
		
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { 
				name, state, jobs };
	}
		
}
