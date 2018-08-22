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
public class SequentialDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new SequentialDesign(element, parentContext);
	}
		
}

class SequentialDesign extends BaseDC {

	private final SimpleTextAttribute independent;

	private final SimpleTextAttribute stateOp;
	
	private final IndexedDesignProperty jobs;
	
	private final SimpleTextAttribute _transient;
	
	public SequentialDesign(ArooaElement element, ArooaContext parentContext) {
		
		super(element, parentContext);
		
		jobs = new IndexedDesignProperty("jobs", this);
		
		independent = new SimpleTextAttribute("independent", this);
		
		stateOp = new SimpleTextAttribute("stateOperator", this);
		
		_transient = new SimpleTextAttribute("transient", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
				.addFormItem(basePanel())
				.addFormItem(new BorderedGroup("Optional Behaviour")
					.add(independent.view().setTitle("Independent"))
					.add(stateOp.view().setTitle("State Operator"))
					.add(_transient.view().setTitle("Transient")))
				.addFormItem(new BorderedGroup("Jobs")
					.add(jobs.view().setTitle("")));
	}
		
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { 
				name, independent, _transient, stateOp, jobs };
	}

}
