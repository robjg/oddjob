/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.view;

import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignListener;
import org.oddjob.arooa.design.DesignStructureEvent;
import org.oddjob.arooa.design.InstanceSupport;
import org.oddjob.arooa.design.screem.SingleTypeSelection;
import org.oddjob.arooa.design.view.DesignViewException;
import org.oddjob.arooa.parsing.QTag;

public class TypeSelectionDummy implements DummyItemView {

	SingleTypeSelection typeSelection;
	
	DesignInstance instance;
	
	public TypeSelectionDummy(SingleTypeSelection typeSelection) {
		
		this.typeSelection = typeSelection;
		
		typeSelection.getDesignElementProperty().addDesignListener(
				new DesignListener() {
			public void childAdded(DesignStructureEvent event) {
				if (event.getIndex() != 0) {
					throw new RuntimeException("Unexpected.");
				}
				instance = event.getChild();
			}
			public void childRemoved(DesignStructureEvent event) {
				if (event.getIndex() != 0) {
					throw new RuntimeException("Unexpected.");
				}
				instance = null;
			}
		});
	}
		
	public void inline(DummyDialogue form) {
		form.addField(new SelectionWidget() {
			public String getName() {
				return typeSelection.getTitle();
			}
		
			public DesignInstance getSelected() {
				return instance;
			}
			
			public DesignInstance setSelected(QTag tag) {
				
				InstanceSupport support = new InstanceSupport(
						typeSelection.getDesignElementProperty());
				
				if (instance != null) {
					support.removeInstance(instance);
				}
				
				try {
					support.insertTag(0, tag);
				} catch (ArooaParseException e) {
					throw new DesignViewException(e);
				}
			
				return instance;
			}
			
		});
		
	}
	
}
