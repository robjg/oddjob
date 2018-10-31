package org.oddjob.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.oddjob.util.Restore;


public class AllEvents<T> implements EventOperator<T>{

    @Override
	public Restore start( List<T> previous,
			List<? extends EventSource<? extends T>> nodes, 
			Consumer<? super List<T>> results) throws Exception {
	
		final Object[] values;
		
		final AtomicBoolean fired = new AtomicBoolean();
		
		int number = nodes.size();
	
		if (previous != null) {
			values = previous.toArray();
			if (values.length != number) {
				throw new IllegalStateException(
				        "Previous event size different to current event size - has config changed? - then delete state!");
			}
		}
		else {
			values = new Object[number];
		}
		
		final List<Restore> closes = new ArrayList<>();
		
		for (int i = 0; i < number; ++i) {
			closes.add(
					nodes.get(i).start(
							new InputConsumer<>(i,
									(T[]) values, 
									fired, 
									results)));
		}
		
		return () -> closes.forEach(Restore::close);
	}
	
	static class InputConsumer<T> implements Consumer<T> {
		
		private final T[] values;
		
		private final AtomicBoolean fired;
		
		private final Consumer<? super List<T>> results;

		private final int index;

		public InputConsumer(int index, T[] values, AtomicBoolean fired, 
				Consumer<? super List<T>> results) {
			this.index = index;
			this.values = values;
			this.fired = fired;
			this.results = results;
		}
		
		@Override
		public void accept(T t) {
			List<T> copy = null;
			synchronized(values) {
				values[index] =  t;
				if (isFired()) {
					copy = new ArrayList<>(Arrays.asList(values));
				}
			}
			if (copy != null) {
				results.accept(copy);
			}
		}
		
		protected boolean isFired() {
			if (fired.get()) {
				return true;
			}
			for (T value : values) {
				if (value == null) {
					return false;
				}
			}
			fired.set(true);
			return true;
		}
			
	}
	
}
