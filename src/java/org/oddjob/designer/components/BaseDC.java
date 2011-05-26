/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignComponent;
import org.oddjob.arooa.design.DesignComponentBase;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.TextField;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.parsing.QTag;

/**
 * The base class which provides default implementation for 
 * a standard DesignComponent.
 * <p>
 */
public abstract class BaseDC extends DesignComponentBase
implements DesignComponent {
	
	private final String toString;

	protected final SimpleTextAttribute name;

	public BaseDC(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		this.toString = new QTag(element, parentContext).toString();
		
		name = new SimpleTextAttribute("name", this);
	}
	
	public BorderedGroup basePanel() {		
		BorderedGroup fg = new BorderedGroup("General");
		fg.add(new TextField("Name", name));
		return fg;
	}

	@Override
	public String toString() {
		if (name.attribute() == null) {
			return toString;
		}
		else {
			return name.attribute();
		}
	}
}
