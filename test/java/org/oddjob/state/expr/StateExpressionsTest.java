package org.oddjob.state.expr;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.utils.Try;
import org.oddjob.state.FlagState;
import org.oddjob.state.StateConditions;
import org.oddjob.util.Restore;

public class StateExpressionsTest {

	@Test
	public void testEquals() throws Exception {
	
		FlagState job = new FlagState();
		
		BeanRegistry registry = mock(BeanRegistry.class);
		when(registry.lookup("MyJob", Stateful.class)).thenReturn(job);
		
		ArooaSession session = mock(ArooaSession.class);
		when(session.getBeanRegistry()).thenReturn(registry);
	
		
		StateExpression test = new StateExpressions.Equals("MyJob", StateConditions.COMPLETE);		
		
		AtomicReference<Try<Boolean>> result = new AtomicReference<>();

		Restore restore = test.evaluate(session, r -> result.set(r));
		
		assertThat(result.get().orElseThrow(), is(false));
		
		job.run();

		assertThat(result.get().orElseThrow(), is(true));
		
		job.hardReset();
		
		assertThat(result.get().orElseThrow(), is(false));
		
		restore.close();
		
		job.run();

		assertThat(result.get().orElseThrow(), is(false));
}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testOr() throws Exception {
		
		ArooaSession session = mock(ArooaSession.class);

		AtomicReference<Consumer<Try<Boolean>>> lhsConsumer = new AtomicReference<>();
		AtomicReference<Consumer<Try<Boolean>>> rhsConsumer = new AtomicReference<>();
		
		Restore lhsRestore = mock(Restore.class);
		Restore rhsRestore = mock(Restore.class);
		
		StateExpression lhs = mock(StateExpression.class);
		when(lhs.evaluate(Mockito.any(ArooaSession.class), Mockito.any(Consumer.class)))
			.thenAnswer(new Answer<Restore>() {
				@Override
				public Restore answer(InvocationOnMock invocation) throws Throwable {
					lhsConsumer.set((Consumer<Try<Boolean>>) invocation.getArguments()[1]);
					return lhsRestore;
				}
			});
		
		StateExpression rhs = mock(StateExpression.class);
		when(rhs.evaluate(Mockito.any(ArooaSession.class), Mockito.any(Consumer.class)))
			.thenAnswer(new Answer<Restore>() {
				@Override
				public Restore answer(InvocationOnMock invocation) throws Throwable {
					rhsConsumer.set((Consumer<Try<Boolean>>) invocation.getArguments()[1]);
					return rhsRestore;
				}
			});

		AtomicReference<Try<Boolean>> result = new AtomicReference<>();

		StateExpression test = new StateExpressions.Or(lhs, rhs);
		
		Restore restore = test.evaluate(session, r -> result.set(r));

		assertThat(lhsConsumer.get(), notNullValue());
		assertThat(rhsConsumer.get(), notNullValue());
		
		assertThat(result.get(), nullValue());

		lhsConsumer.get().accept(Try.of(false));
		
		assertThat(result.get(), nullValue());

		rhsConsumer.get().accept(Try.of(false));

		assertThat(result.get().orElseThrow(), is(false));
		
		lhsConsumer.get().accept(Try.of(true));
		
		assertThat(result.get().orElseThrow(), is(true));
		
		lhsConsumer.get().accept(Try.of(false));
		
		assertThat(result.get().orElseThrow(), is(false));
		
		rhsConsumer.get().accept(Try.of(true));
		
		assertThat(result.get().orElseThrow(), is(true));
		
		lhsConsumer.get().accept(Try.of(true));
		
		assertThat(result.get().orElseThrow(), is(true));
		
		lhsConsumer.get().accept(Try.fail(new Exception("Doh!")));

		try {
			result.get().orElseThrow();
			fail("Should throw an Exception");
		}
		catch (Exception e) {
			assertThat(e.getMessage(), is("Doh!"));
		}
		
		lhsConsumer.get().accept(Try.of(true));
		rhsConsumer.get().accept(Try.fail(new Exception("Doh!")));
		
		try {
			result.get().orElseThrow();
			fail("Should throw an Exception");
		}
		catch (Exception e) {
			assertThat(e.getMessage(), is("Doh!"));
		}
		
		restore.close();
		
		Mockito.verify(lhsRestore).close();
		Mockito.verify(rhsRestore).close();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testAnd() throws Exception {
		
		ArooaSession session = mock(ArooaSession.class);

		AtomicReference<Consumer<Try<Boolean>>> lhsConsumer = new AtomicReference<>();
		AtomicReference<Consumer<Try<Boolean>>> rhsConsumer = new AtomicReference<>();
		
		Restore lhsRestore = mock(Restore.class);
		Restore rhsRestore = mock(Restore.class);
		
		StateExpression lhs = mock(StateExpression.class);
		when(lhs.evaluate(Mockito.any(ArooaSession.class), Mockito.any(Consumer.class)))
			.thenAnswer(new Answer<Restore>() {
				@Override
				public Restore answer(InvocationOnMock invocation) throws Throwable {
					lhsConsumer.set((Consumer<Try<Boolean>>) invocation.getArguments()[1]);
					return lhsRestore;
				}
			});
		
		StateExpression rhs = mock(StateExpression.class);
		when(rhs.evaluate(Mockito.any(ArooaSession.class), Mockito.any(Consumer.class)))
			.thenAnswer(new Answer<Restore>() {
				@Override
				public Restore answer(InvocationOnMock invocation) throws Throwable {
					rhsConsumer.set((Consumer<Try<Boolean>>) invocation.getArguments()[1]);
					return rhsRestore;
				}
			});

		AtomicReference<Try<Boolean>> result = new AtomicReference<>();

		StateExpression test = new StateExpressions.And(lhs, rhs);
		
		Restore restore = test.evaluate(session, r -> result.set(r));

		assertThat(lhsConsumer.get(), notNullValue());
		assertThat(rhsConsumer.get(), notNullValue());
		
		assertThat(result.get(), nullValue());
		
		lhsConsumer.get().accept(Try.of(false));
		
		assertThat(result.get(), nullValue());

		rhsConsumer.get().accept(Try.of(false));
		
		assertThat(result.get().orElseThrow(), is(false));
		
		lhsConsumer.get().accept(Try.of(true));
		
		assertThat(result.get().orElseThrow(), is(false));
		
		lhsConsumer.get().accept(Try.of(false));
		rhsConsumer.get().accept(Try.of(true));
		
		assertThat(result.get().orElseThrow(), is(false));
		
		lhsConsumer.get().accept(Try.of(true));
		
		assertThat(result.get().orElseThrow(), is(true));
		
		rhsConsumer.get().accept(Try.of(false));
		
		assertThat(result.get().orElseThrow(), is(false));
		
		lhsConsumer.get().accept(Try.fail(new Exception("Doh!")));

		try {
			result.get().orElseThrow();
			fail("Should throw an Exception");
		}
		catch (Exception e) {
			assertThat(e.getMessage(), is("Doh!"));
		}
		
		lhsConsumer.get().accept(Try.of(true));
		rhsConsumer.get().accept(Try.fail(new Exception("Doh!")));
		
		try {
			result.get().orElseThrow();
			fail("Should throw an Exception");
		}
		catch (Exception e) {
			assertThat(e.getMessage(), is("Doh!"));
		}
		
		restore.close();
		
		Mockito.verify(lhsRestore).close();
		Mockito.verify(rhsRestore).close();
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testNot() throws Exception {
		
		// Given
		
		ArooaSession session = mock(ArooaSession.class);

		AtomicReference<Consumer<Try<Boolean>>> exprConsumer = new AtomicReference<>();
		
		Restore exprRestore = mock(Restore.class);

		StateExpression expr = mock(StateExpression.class);
		when(expr.evaluate(Mockito.any(ArooaSession.class), Mockito.any(Consumer.class)))
			.thenAnswer(new Answer<Restore>() {
				@Override
				public Restore answer(InvocationOnMock invocation) throws Throwable {
					exprConsumer.set((Consumer<Try<Boolean>>) invocation.getArguments()[1]);
					return exprRestore;
				}
			});
		
		AtomicReference<Try<Boolean>> result = new AtomicReference<>();

		StateExpression test = new StateExpressions.Not(expr);

		// When
		
		Restore restore = test.evaluate(session, r -> result.set(r));

		// Then
		
		assertThat(exprConsumer.get(), notNullValue());
		
		assertThat(result.get(), nullValue());
		
		exprConsumer.get().accept(Try.of(false));
		
		assertThat(result.get().orElseThrow(), is(true));
				
		exprConsumer.get().accept(Try.of(true));
		
		assertThat(result.get().orElseThrow(), is(false));
		
		exprConsumer.get().accept(Try.of(false));
		
		assertThat(result.get().orElseThrow(), is(true));
				
		exprConsumer.get().accept(Try.fail(new Exception("Doh!")));

		try {
			result.get().orElseThrow();
			fail("Should throw an Exception");
		}
		catch (Exception e) {
			assertThat(e.getMessage(), is("Doh!"));
		}
		
		restore.close();
		
		Mockito.verify(exprRestore).close();
	}

}
