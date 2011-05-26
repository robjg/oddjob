/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.view;

import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FormItem;

public class FieldGroupDummy implements DummyItemView {

	private final BorderedGroup fieldGroup;
	
	public FieldGroupDummy(BorderedGroup fieldGroup) {
		this.fieldGroup = fieldGroup;
	}

	public void inline(DummyDialogue form) {

		for (int i = 0; i < fieldGroup.size(); ++i) {
			
			FormItem formField = fieldGroup.get(i);
			DummyItemView itemView = DummyItemViewFactory.create(formField);
			
			itemView.inline(form);
		}
	}
	
}
