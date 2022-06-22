package org.oddjob.state.expr;

import org.junit.Test;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.utils.Try;
import org.oddjob.events.InstantEvent;
import org.oddjob.state.FlagState;
import org.oddjob.util.Restore;

import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class StateExpressionParserTest {

	@Test
	public void testParseSimpleExpression() throws Exception {
	
		ArooaSession session = new StandardArooaSession();
		
		FlagState job1 = new FlagState();
		
		session.getBeanRegistry().register("job1", job1);
		
		StateExpressionParser<StateExpression> test = 
				new StateExpressionParser<>(CaptureToExpression::new);
		
		StateExpression expression = test.parse("job1 is success");
		
		AtomicReference<Try<InstantEvent<Boolean>>> result =
				new AtomicReference<>();
		
		try (Restore restore = expression.evaluate(session, result::set)) {
			
			assertThat(result.get().orElseThrow().getOf(),
                       is(false));

			job1.run();
			
			assertThat(result.get().orElseThrow().getOf(),
                       is(true));

			job1.hardReset();
			
			assertThat(result.get().orElseThrow().getOf(),
                       is(false));
		}	
	}

	@Test
	public void testExpressionWithAndsAndOrs() throws Exception {
	
		ArooaSession session = new StandardArooaSession();
		
		FlagState job1 = new FlagState();
		FlagState job2 = new FlagState();
		FlagState job3 = new FlagState();
		
		session.getBeanRegistry().register("job1", job1);
		session.getBeanRegistry().register("job2", job2);
		session.getBeanRegistry().register("job3", job3);
		
		StateExpressionParser<StateExpression> test = new StateExpressionParser<>(
				CaptureToExpression::new);
		
		StateExpression expression = test.parse(
		        "job1 is success or ( job2 is success and job3 is success)");
		
		AtomicReference<Try<InstantEvent<Boolean>>> result =
                new AtomicReference<>();
		
		try (Restore restore = expression.evaluate(session, result::set)) {
			
			assertThat(result.get().orElseThrow().getOf(),
                       is(false));

			job2.run();
			
			assertThat(result.get().orElseThrow().getOf(),
                       is(false));
			
			job3.run();
			
			assertThat(result.get().orElseThrow().getOf(),
                       is(true));
		}		
	}

	@Test
	public void testBadGrammer() {
	
		StateExpressionParser<StateExpression> test = 
				new StateExpressionParser<>(CaptureToExpression::new);
		
		try {
			test.parse("This wont work");
			fail("Should fail");
		}
		catch (Exception e) {
			assertThat(e.getMessage(), notNullValue());
		}
	}

}
