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
public class ExistsDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new ExistsDesign(element, parentContext);
	}
		
}

class ExistsDesign extends BaseDC {

	private final FileAttribute file; 
	
	public ExistsDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		file = new FileAttribute("file", this);
	}
		
	
	public DesignProperty[] children() {
		return new DesignProperty[] { name, file };
	}
	
	public Form detail() {
			return new StandardForm(this)
				.addFormItem(basePanel())	
				.addFormItem(
						new BorderedGroup("File Details")
						.add(new FileSelection("File", file)));
	}
		
}
