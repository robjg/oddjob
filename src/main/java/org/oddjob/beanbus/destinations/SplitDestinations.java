package org.oddjob.beanbus.destinations;

import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.framework.Transient;
import org.oddjob.framework.extend.StructuralJob;
import org.oddjob.state.SequentialHelper;
import org.oddjob.state.StateOperator;
import org.oddjob.state.WorstStateOp;

import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @oddjob.description A Bean Bus Destination that splits data between child destinations depending on
 * a Strategy.
 * 
 * @oddjob.example
 * 
 * All numbers are passed to two queues for computation of the Fibonacci Sequence of the number, and the
 * Factorial.
 * 
 * {@oddjob.xml.resource org/oddjob/beanbus/destinations/SplitAll.xml}
 *
 * @author Rob Gordon
 */
public class SplitDestinations<T> extends StructuralJob<Object>
implements Transient, Consumer<T>, Flushable {
	
	private static final long serialVersionUID = 2022123100L;

	/**
	 * @oddjob.property
	 * @oddjob.description The strategy for splitting data.
	 * @oddjob.required No, default is Each.
	 */
	private Function<? super Consumer<? super T>[], ? extends Consumer<T>> strategy;


	/**
	 * Result of applying a strategy to the child consumers.
	 */
	private volatile Consumer<? super T> consumer;

	/**
	 * @oddjob.property
	 * @oddjob.description Count of items received.
	 * @oddjob.required R/O.
	 */
	private final AtomicInteger count = new AtomicInteger();


	@Override
	protected void execute() throws InterruptedException, ExecutionException {
		Function<? super Consumer<? super T>[], ? extends Consumer<T>> strategy = this.strategy;
		if (strategy == null) {
			strategy = new Each<>();
		}

		List<Consumer<? super T>> consumers = new ArrayList<>(childHelper.size());

		for (Object child : childHelper) {

			if (child instanceof Consumer) {
				//noinspection unchecked
				consumers.add((Consumer<? super T>) child);
			}
		}

		@SuppressWarnings("unchecked")
		Consumer<? super T>[] consumerArray = consumers.toArray(new Consumer[0]);

		this.consumer = strategy.apply(consumerArray);

		for (Object child : childHelper) {
			if (stop) {
				stop = false;
				break;
			}

			if (!(child instanceof Runnable)) {
				logger().info("Not Executing [" + child + "] as it is not a job.");
			}
			else {
				Runnable job = (Runnable) child;
				logger().info("Executing child [" + job + "]");

				job.run();
			}

			// Test we can still continue children.
			if (!new SequentialHelper().canContinueAfter(child)) {
				logger().info("Child [" + child + "] failed. Can't continue.");
				break;
			}
		}
	}

	@Override
	public void accept(T t) {

		this.consumer.accept(t);
	}

	@Override
	public void flush() throws IOException {
		logger().info("Flushing...");
		for (Object child : childHelper) {
			if (child instanceof Flushable) {
				((Flushable) child).flush();
			}
		}
	}

	@Override
	protected void onReset() {
		super.onReset();
		count.set(0);
	}

	/**
	 * Add a child.
	 *
	 * @oddjob.property of
	 * @oddjob.description The components of a Bus.
	 * @oddjob.required No, but pointless if missing.
	 *
	 * @param child A child
	 */
	@ArooaComponent
	public void setOf(int index, Object child) {
		if (child == null) {
			childHelper.removeChildAt(index);
		}
		else {
			childHelper.insertChild(index, child);
		}
	}

	@Override
	protected StateOperator getInitialStateOp() {
		return new WorstStateOp();
	}

	public Function<? super Consumer<? super T>[], ? extends Consumer<T>> getStrategy() {
		return strategy;
	}

	public void setStrategy(Function<? super Consumer<? super T>[], ? extends Consumer<T>> strategy) {
		this.strategy = strategy;
	}

	public AtomicInteger getCount() {
		return count;
	}

	public static class All<T> implements Function<Consumer<? super T>[], Consumer<T>> {

		@Override
		public Consumer<T> apply(Consumer<? super T>[] consumers) {
			return t -> {
				for (Consumer<? super T> consumer : consumers) {
					consumer.accept(t);
				}
			};
		}

		@Override
		public String toString() {
			return "All";
		}
	}

	public static class Each<T> implements Function<Consumer<? super T>[], Consumer<T>> {

		@Override
		public Consumer<T> apply(Consumer<? super T>[] consumers) {
			AtomicInteger count  = new AtomicInteger();
			int number = consumers.length;
			return t -> consumers[count.getAndIncrement() % number].accept(t);
		}

		@Override
		public String toString() {
			return "Each";
		}
	}

}
