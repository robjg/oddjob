/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.etc.FileAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FileSelection;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;

/**
 *
 */
public class MkdirDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new MkdirDesign(element, parentContext);
	}
}

class MkdirDesign extends BaseDC {

	private final FileAttribute dir;
	
	public MkdirDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);

		dir = new FileAttribute("dir", this);
		
	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] { name, dir };
	}
	
	public Form detail() {
			return new StandardForm(this)
				.addFormItem(basePanel())	
				.addFormItem(
						new BorderedGroup("Directory Details")
						.add(new FileSelection("Directory", dir)));
	}
		
}
