/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.elements.schedule;

import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.DesignValueBase;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 * 
 */
abstract public class ParentSchedule extends DesignValueBase {

	private final SimpleDesignProperty refinement;

	public ParentSchedule(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		refinement = new SimpleDesignProperty(
				"refinement", this);
		
	}
	
	protected DesignProperty getRefinement() {
		return refinement;
	}
}
