package org.oddjob.state.expr;

import org.junit.Test;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.events.InstantEvent;
import org.oddjob.state.FlagState;
import org.oddjob.util.Restore;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class StateExpressionTypeTest {

	@Test
	public void whenExpectedStateThenConsumerNotified() throws Exception {
		
		FlagState job = new FlagState();
		
		ArooaSession session = new StandardArooaSession();
		session.getBeanRegistry().register("flag", job);
		
		StateExpressionType test = new StateExpressionType();
		test.setArooaSession(session);
		test.setExpression("flag is success");
		
		List<InstantEvent<Boolean>> results = new ArrayList<>();
		
		Restore restore = test.subscribe(results::add);

		assertThat(results.size(), is(0));		

		job.run();
		
		assertThat(results.size(), is(1));		
		assertThat(results.get(0).getOf(), is(Boolean.TRUE));

		restore.close();
	}
}
