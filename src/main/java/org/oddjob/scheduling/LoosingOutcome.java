package org.oddjob.scheduling;

import org.oddjob.Stateful;

/**
 * An {link Outcome} for the looser. A loosing Outcome provides state 
 * notifications that will notify any listeners when the winner has 
 * completed the work.
 * 
 * @author rob
 *
 */
public interface LoosingOutcome extends Outcome, Stateful {

}
