/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.view;

import org.oddjob.arooa.design.screem.TextInput;

public class TextInputDummy implements DummyItemView {
	
	private TextInput textInput;
	
	public TextInputDummy(TextInput textInput) {
		this.textInput = textInput;
	}

	public void inline(DummyDialogue form) {
		form.addField(new TextWidget() {
			public String getName() {
				return textInput.getTitle();
			}
			public String getText() {
				return textInput.getText();
			}
			public void setText(String text) {
				textInput.setText(text);
			}
		});
	}
	
}
