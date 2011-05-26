package org.oddjob.designer.view;

import org.oddjob.arooa.design.screem.TextPsudoForm;

public class TextPsudoFormDummy implements DummyFormView {

	private TextPsudoForm textForm;
	
	public TextPsudoFormDummy(TextPsudoForm textForm) {
		this.textForm = textForm;
	}
	
	public DummyDialogue dialogue() {
		return new DummyDialogue() {
			public void addField(DummyWidget widget) {
				throw new UnsupportedOperationException();
			}
			
			public DummyWidget get(String title) {
				return new TextWidget() {
					public String getName() {
						throw new UnsupportedOperationException();
					}
					
					public String getText() {
						return textForm.getAttribute().attribute();
					}
					
					public void setText(String text) {
						textForm.getAttribute().attribute(text);
					}
				};
			}
		};
	}
}
