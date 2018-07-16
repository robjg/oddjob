package org.oddjob.events;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.util.Restore;

public class ForEventsTest {

	public static class SubscribeInts implements SubscribeNode<Integer> {
		@Override
		public Restore start(Consumer<? super Integer> consumer) {
			IntStream.of(1, 2, 3).forEach(consumer::accept);;
			return () -> {};
		}
	}

	@Test
	public void givenHappyPathThenWorks() throws Exception {

		String xml = "<events>" + 
					"<job>"
					+ "<bean class='"	+ SubscribeInts.class.getName() + "'/>" +
					"</job>" +
				"</events>";
				
		
		ForEvents<Integer> test = new ForEvents<>();
		test.setConfiguration(new XMLConfiguration("XML", xml));
		test.setValues(Stream.of(10,20));
		test.setArooaSession(new StandardArooaSession());
		
		List<List<Integer>> results = new ArrayList<>();

		test.start(results::add);
		
		assertThat(results.size(), is(3));
		assertThat(results.get(0), is(Arrays.asList(3, 1)));
		assertThat(results.get(1), is(Arrays.asList(3, 2)));
		assertThat(results.get(2), is(Arrays.asList(3, 3)));
		
		
	}
	
	
}
