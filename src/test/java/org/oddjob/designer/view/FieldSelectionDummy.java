/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.view;

import org.oddjob.arooa.design.screem.FieldSelection;
import org.oddjob.arooa.design.screem.FormItem;

public class FieldSelectionDummy implements DummyItemView {

	private final FieldSelection fieldSelection;
	
	public FieldSelectionDummy(FieldSelection fieldSelection) {
		this.fieldSelection = fieldSelection;
	}

	public void inline(DummyDialogue form) {

		for (int i = 0; i < fieldSelection.size(); ++i) {
			
			FormItem formField = fieldSelection.get(i);
			DummyItemView itemView = DummyItemViewFactory.create(formField);
			
			itemView.inline(form);
		}
	}
	
}
