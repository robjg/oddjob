/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.designer.view;

import java.util.ArrayList;
import java.util.List;

import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.design.DesignElementProperty;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignListener;
import org.oddjob.arooa.design.DesignStructureEvent;
import org.oddjob.arooa.design.InstanceSupport;
import org.oddjob.arooa.design.screem.MultiTypeTable;
import org.oddjob.arooa.design.view.DesignViewException;
import org.oddjob.arooa.parsing.QTag;

public class MultiTypeTableDummy implements DummyItemView {

	private MultiTypeTable multiTypeTable;
	
	private List<DesignInstance> instances = 
		new ArrayList<DesignInstance>();
	
	public MultiTypeTableDummy(MultiTypeTable multiTypeTable) {
		
		this.multiTypeTable = multiTypeTable;
		DesignElementProperty de = multiTypeTable.getDesignProperty();
		
		de.addDesignListener(new DesignListener() {
			public void childAdded(DesignStructureEvent event) {
				instances.add(event.getIndex(), event.getChild());				
			}
			public void childRemoved(DesignStructureEvent event) {
				instances.remove(event.getIndex());
			}
		});		
		
	}
	
	public void inline(DummyDialogue form) {
		form.addField(new TableWidget() {
			public DesignInstance getInstanceAt(int index) {
				return instances.get(index);
			}
			
			public String getName() {
				return multiTypeTable.getTitle();
			}
			public void setInstanceAt(int index, QTag tag) {
				try {
					create(index, tag);
				} catch (ArooaParseException e) {
					throw new DesignViewException(e);
				}
			}
		});
	}
	
	private void create(int index, QTag type) throws ArooaParseException {
		if (multiTypeTable.isKeyed()) {
			throw new RuntimeException("Table is keyed! supply a name.");
		}
		
		DesignElementProperty designProperty = multiTypeTable.getDesignProperty();
		
		InstanceSupport support = new InstanceSupport(
				designProperty);
		
		if (index < instances.size()) {
			support.removeInstance(instances.get(index));
		}
		
		support.insertTag(index, type);
		
	}
	
}
