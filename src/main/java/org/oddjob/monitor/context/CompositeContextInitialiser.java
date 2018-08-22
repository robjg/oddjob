package org.oddjob.monitor.context;

/**
 * A {@link ContextInitialiser} that is lots of little
 * initialisers.
 * 
 * @author rob
 *
 */
public class CompositeContextInitialiser implements ContextInitialiser {

	/** Initialisers. */
	private final ContextInitialiser[] initialisers;
	
	/**
	 * Constructor.
	 * 
	 * @param initialisers The initialisers. Must not be null.
	 */
	public CompositeContextInitialiser(ContextInitialiser[] initialisers) {
		this.initialisers = initialisers;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.monitor.context.ContextInitialiser#initialise(org.oddjob.monitor.context.ExplorerContext)
	 */
	public void initialise(ExplorerContext context) {
		for (ContextInitialiser initialiser : initialisers) {
			initialiser.initialise(context);
		}
	}
	
}
