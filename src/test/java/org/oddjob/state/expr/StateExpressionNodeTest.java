package org.oddjob.state.expr;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.events.EventOf;
import org.oddjob.state.FlagState;
import org.oddjob.util.Restore;

public class StateExpressionNodeTest {

	@Test
	public void testFoo() throws Exception {
		
		FlagState job = new FlagState();
		
		ArooaSession session = new StandardArooaSession();
		session.getBeanRegistry().register("flag", job);
		
		StateExpressionNode test = new StateExpressionNode();
		test.setArooaSession(session);
		test.setExpression("flag is success");
		
		List<EventOf<Boolean>> results = new ArrayList<>();
		
		Restore restore = test.start(results::add);

		assertThat(results.size(), is(0));		

		job.run();
		
		assertThat(results.size(), is(1));		
		assertThat(results.get(0).getOf(), is(Boolean.TRUE));

		restore.close();
		
	}
	
}
