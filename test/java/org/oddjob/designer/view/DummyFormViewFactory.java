/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.view;

import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.design.screem.TextPseudoForm;

/**
 * 
 */
public class DummyFormViewFactory {
		
	public static DummyFormView create(Form form) {

		if (form instanceof StandardForm) {
			return new DummyStandardFormView(
					(StandardForm) form);
		}
		else if (form instanceof TextPseudoForm) {
			return new TextPseudoFormDummy(
					(TextPseudoForm) form);
		}
		
		throw new RuntimeException("Unexpected: " + form);
	}
}
