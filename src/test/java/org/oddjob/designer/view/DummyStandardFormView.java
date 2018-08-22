package org.oddjob.designer.view;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.oddjob.arooa.design.screem.StandardForm;

public class DummyStandardFormView implements DummyFormView {

	private final Map<String, DummyWidget> widgets = 
		new LinkedHashMap<String, DummyWidget>();
	
	private DummyStandardDialog dialogue = new DummyStandardDialog();
	
	public DummyStandardFormView(StandardForm form) {
		for (int i = 0; i < form.size(); ++i) {
			DummyItemView itemView = DummyItemViewFactory.create(
					form.getFormItem(i));
			itemView.inline(dialogue);
		}		
	}
	
	private String options() {
		String options = "";
		for (Iterator<String> it = widgets.keySet().iterator(); it.hasNext(); ) {
			options = options + "[" + it.next() + "]";
		}
		return options;
	}

	public DummyDialogue dialogue() {
		return dialogue;
	}
	
	class DummyStandardDialog implements DummyDialogue {
		
		public void addField(DummyWidget widget) {
			widgets.put(widget.getName(), widget);
		}
		
		public DummyWidget get(String title) {
			DummyWidget child = widgets.get(title); 
			if (child == null) {
				throw new IllegalArgumentException("No [" + title + "], options are: "
						+ options());
			}
			return child;
		}		
	}
}
