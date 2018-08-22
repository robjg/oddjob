package org.oddjob.monitor.context;

/**
 * Implementations are used to initialise an {@link ExplorerContext}
 * when it is first created.
 * 
 * @author rob
 *
 */
public interface ContextInitialiser {

	/**
	 * Initialise the {@link ExplorerContext}.
	 * 
	 * @param context The context. It will contain the 
	 * component it is the context for.
	 */
	public void initialise(ExplorerContext context);
}
