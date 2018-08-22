package org.oddjob.designer.view;

import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.parsing.QTag;

public interface TableWidget extends DummyWidget {

	public DesignInstance getInstanceAt(int index);
	
	public void setInstanceAt(int index, QTag tag);
}
