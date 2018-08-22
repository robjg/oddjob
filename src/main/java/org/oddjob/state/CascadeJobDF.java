/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.state;

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
import org.oddjob.designer.components.BaseDC;

/**
 *
 */
public class CascadeJobDF implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new CascadeJobDesign(element, parentContext);
	}		
}

class CascadeJobDesign extends BaseDC {

	private final SimpleTextAttribute haltOn;

	private final SimpleTextAttribute cascadeOn;
	
	private final IndexedDesignProperty jobs;
	
	public CascadeJobDesign(ArooaElement element, ArooaContext parentContext) {
		
		super(element, parentContext);
		
		jobs = new IndexedDesignProperty("jobs", this);
		
		haltOn = new SimpleTextAttribute("haltOn", this);
		
		cascadeOn = new SimpleTextAttribute("cascadeOn", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
				.addFormItem(basePanel())
				.addFormItem(new BorderedGroup("Optional Behaviour")
					.add(haltOn.view().setTitle("Halt On"))
					.add(cascadeOn.view().setTitle("Cascade On"))
				)
				.addFormItem(new BorderedGroup("Jobs")
					.add(jobs.view().setTitle(""))
				);
	}
		
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { 
				name, haltOn, cascadeOn, jobs };
	}

}
