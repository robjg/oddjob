/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.IndexedDesignProperty;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class FolderDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new FolderDesign(element, parentContext);
	}
		
}

class FolderDesign extends BaseDC {

	private final IndexedDesignProperty jobs;
	
	public FolderDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		jobs = new IndexedDesignProperty("jobs", this);
	}
	
	public Form detail() {
		return new StandardForm(this)
				.addFormItem(basePanel())
				.addFormItem(new BorderedGroup("Jobs")
					.add(jobs.view().setTitle("")));
	}

	
	
//	public SimpleHierarchy<ComponentAction> availableActions() {
//		SimpleHierarchy<ComponentAction> childActions = 
//			new CreateActions().childActions(this, "", designSession);
//		
//		return new SimpleHierarchy<ComponentAction>(ComponentAction.class)
//			.addHierarchy(childActions).setName("Add Job");
//	}
//
	
	/* (non-Javadoc)
	 * @see org.oddjob.designer.model.StructuralDesignComponent#transferActions(java.lang.String)
	 */
//	public ComponentAction[] transferActions(String xml) {
//		return new ComponentAction[] { 
//				new TransferComponentAction(this, xml, "Add Job", "")
//		};
//	}
	
	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { name, jobs };
	}

}
