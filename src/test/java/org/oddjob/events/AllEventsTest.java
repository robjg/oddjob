package org.oddjob.events;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;
import org.oddjob.util.Restore;

public class AllEventsTest {

	private class IntEvents implements EventSource<Integer> {
		
		Consumer<? super Integer> consumer;
		
		void publish(Integer i) {
			consumer.accept(i);
		}
		
		@Override
		public Restore start(Consumer<? super Integer> consumer) {
			this.consumer = consumer;
			return () -> {};
		}
	}
	
	private class DoubleEvents implements EventSource<Double> {
		
		Consumer<? super Double> consumer;
		
		void publish(Double d) {
			consumer.accept(d);
		}

		@Override
		public Restore start(Consumer<? super Double> consumer) {
			this.consumer = consumer;
			return () -> {};
		}

	}

	@Test
	public void testEverythingWorks() throws Exception {
	
		IntEvents ie = new IntEvents();
		DoubleEvents de = new DoubleEvents();
		
		List<List<Number>> results = new ArrayList<>();
		
		AllEvents<Number> test = new AllEvents<>();
		
		test.start(null, 
				Arrays.asList(ie, de), 
				results::add);

		assertThat(results.size(), is(0));		

		ie.publish(1);
		
		assertThat(results.size(), is(0));		

		de.publish(4.2);
		
		assertThat(results.size(), is(1));		
		assertThat(results.get(0), is(Arrays.asList(1, 4.2)));		
		
		de.publish(2.6);

		assertThat(results.size(), is(2));		
		assertThat(results.get(1), is(Arrays.asList(1, 2.6)));		
	}
}
