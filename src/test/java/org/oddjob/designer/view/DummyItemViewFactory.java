package org.oddjob.designer.view;


import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.FieldSelection;
import org.oddjob.arooa.design.screem.FormItem;
import org.oddjob.arooa.design.screem.MultiTypeTable;
import org.oddjob.arooa.design.screem.SingleTypeSelection;
import org.oddjob.arooa.design.screem.TextField;

public class DummyItemViewFactory {

	public static DummyItemView create(FormItem item) {
		
		if (item instanceof BorderedGroup) {

			return new FieldGroupDummy(
					(BorderedGroup) item);
		}
		else if (item instanceof SingleTypeSelection) {
		
			return new TypeSelectionDummy(
					(SingleTypeSelection) item);
		}
		else if (item instanceof MultiTypeTable) {

			return new MultiTypeTableDummy(
					(MultiTypeTable) item);			
		}
		else if (item instanceof TextField) {
			
			return new TextFieldDummy(
					(TextField) item);			
		}
		else if (item instanceof FieldSelection) {
			
			return new FieldSelectionDummy(
					(FieldSelection) item);			
		}
		
		throw new RuntimeException("Unexpected Item " + item); 		
	}
}
