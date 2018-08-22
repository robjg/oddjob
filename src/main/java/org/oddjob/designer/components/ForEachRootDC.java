package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignComponentBase;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.SerializableDesignFactory;
import org.oddjob.jobs.structural.ForEachJob;

public class ForEachRootDC implements SerializableDesignFactory {
	private static final long serialVersionUID = 201109082013120800L;
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new ForEachRootDesign(element, parentContext);
	}
}

class ForEachRootDesign extends DesignComponentBase {

	private final SimpleDesignProperty job;

	public ForEachRootDesign(ArooaElement element, ArooaContext context) {
		super(element, 
				new SimpleArooaClass(ForEachJob.LocalBean.class),
				context);
		
		job = new SimpleDesignProperty(
				"job", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(
					new BorderedGroup("Properties")
					.add(job.view().setTitle("Job")));
	}
	
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { job };
	}

	public String toString() {
		return "ForEach";
	}

}


