/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.view;

import org.oddjob.arooa.design.screem.TextField;

public class TextFieldDummy implements DummyItemView {
	
	private TextField textField;
	
	public TextFieldDummy(TextField textField) {
		this.textField = textField;
	}

	public void inline(DummyDialogue form) {
		form.addField(new TextWidget() {
			public String getName() {
				return textField.getTitle();
			}
			public String getText() {
				return textField.getAttribute().attribute();
			}
			public void setText(String text) {
				textField.getAttribute().attribute(text);
			}
		});
	}
	
}
