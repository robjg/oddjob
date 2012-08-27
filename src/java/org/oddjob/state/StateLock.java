package org.oddjob.state;

import org.oddjob.util.OddjobLockTimeoutException;
import org.oddjob.util.OddjobLockedException;

/**
 * A mechanism for allowing actions to be performed synchronously
 * with respect to state change.
 * 
 * @author rob
 *
 */
public interface StateLock {

	/**
	 * Try to acquire the lock, and then do something when the 
	 * condition is true. Both
	 * the condition evaluation and the performing of the action
	 * are done in the context of the same state lock. If the
	 * lock can not be acquired an {@link OddjobLockedException}
	 * is thrown.
	 * 
	 * @param when The condition.
	 * @param runnable The action.
	 * 
	 * @return true if the condition
	 * was true and the action executed. False otherwise.
	 * 
	 * @throws OddjobLockedException If the lock can not be acquired.
	 */
	public boolean tryToWhen(StateCondition when, Runnable runnable)
	throws OddjobLockedException;

	/**
	 * Wait to do something when the condition is true. Both
	 * the condition evaluation and the performing of the action
	 * are done in the context of the same state lock. If the
	 * lock can not be acquired then the lock is waited for.
	 * 
	 * @param when The condition.
	 * @param runnable The action.
	 * 
	 * @return true if the condition was true and the action
	 * executed.
	 * 
	 * @throws InterruptedException If the wait is interrupted.
	 * @throws OddjobLockTimeoutException If the lock can not be acquired
	 * within the timeout period.
	 */
	public boolean waitToWhen(StateCondition when, Runnable runnable)
	throws InterruptedException, OddjobLockTimeoutException; 

}
