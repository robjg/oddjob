package org.oddjob.designer.view;

import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.parsing.QTag;

public interface SelectionWidget extends DummyWidget {

	public DesignInstance getSelected();
	
	public DesignInstance setSelected(QTag tag);
	
}
