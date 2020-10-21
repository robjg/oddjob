package org.oddjob.state;

import org.oddjob.util.OddjobLockedException;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

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
	boolean tryToWhen(StateCondition when, Runnable runnable)
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
	 */
	boolean waitToWhen(StateCondition when, Runnable runnable);

	/**
	 * Wait to acquire the lock and execute the Runnable while holding the lock.
	 *
	 * @param runnable The Runnable.
	 */
	void runLocked(Runnable runnable);

	/**
	 * Wait to acquire the lock and execute the Callable while holding the lock.
	 *
	 * @param callable The callable.
	 * @return The result of the callable.
	 * @throws Exception from the callable.
	 */
	<T> T callLocked(Callable<T> callable) throws Exception;

	/**
	 * Wait to acquire the lock and execute the Supplier while holding the lock.
	 *
	 * @param supplier The Supplier.
	 * @return The result from the Supplier.
	 */
	<T> T supplyLocked(Supplier<T> supplier);
}
