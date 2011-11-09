/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.script;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.MappedDesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.designer.components.BaseDC;

/**
 *	DesignFactory for ths Script job.
 */
public class ScriptDesFa implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new ScriptDesign(element, parentContext);
	}
}

class ScriptDesign extends BaseDC {

	private final SimpleTextAttribute language;
	
	private final SimpleTextAttribute resultVariable;
	
	private final SimpleTextAttribute resultForState;
	
	private final SimpleDesignProperty input;
	
	private final MappedDesignProperty beans;

	public ScriptDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);

		language = new SimpleTextAttribute("language", this);

		resultVariable = new SimpleTextAttribute("resultVariable", this);
		
		resultForState = new SimpleTextAttribute("resultForState", this);
		
		input = new SimpleDesignProperty(
				"input", this);
		
		beans = new MappedDesignProperty(
				"beans", this);
	}		
	
	/*
	 *  (non-Javadoc)
	 * @see org.oddjob.designer.model.DesignComponent#form()
	 */
	public Form detail() {
		return new StandardForm(this)
			.addFormItem(basePanel())
			.addFormItem(new BorderedGroup("Command Details")
					.add(language.view().setTitle("Language"))
					.add(input.view().setTitle("Input"))
					.add(beans.view().setTitle("Beans"))
					.add(resultVariable.view().setTitle("Result Variable"))
					.add(resultForState.view().setTitle("Result For State"))
				);
	}

	@Override
	public DesignProperty[] children() {
		return new DesignProperty[] { 
				name, language, input, beans, resultVariable, resultForState };
	}
}
