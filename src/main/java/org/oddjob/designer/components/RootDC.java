package org.oddjob.designer.components;

import org.oddjob.Oddjob;
import org.oddjob.arooa.design.*;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.SerializableDesignFactory;

/**
 * Oddjob's {@link DesignFactory}.
 * 
 * @author rob
 *
 */
public class RootDC implements SerializableDesignFactory {
	private static final long serialVersionUID = 201109082013120800L;
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new RootDesign(element, parentContext);
	}
}

class RootDesign extends DesignComponentBase {

	private final SimpleDesignProperty job;

	public RootDesign(ArooaElement element, ArooaContext context) {
		super(element, 
				new SimpleArooaClass(Oddjob.OddjobRoot.class),
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
		return "Oddjob";
	}

}


