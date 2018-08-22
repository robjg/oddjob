package org.oddjob.monitor.model;

import org.oddjob.monitor.context.ExplorerContext;

/**
 * Creates an {@link ExplorerContext} from an {@link ExplorerModel}
 * @author rob
 *
 */
public interface ExplorerContextFactory {

	public ExplorerContext createFrom(ExplorerModel explorerModel);
	
}
