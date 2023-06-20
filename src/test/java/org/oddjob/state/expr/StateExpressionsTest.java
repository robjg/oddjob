package org.oddjob.state.expr;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.utils.Try;
import org.oddjob.events.InstantEvent;
import org.oddjob.state.*;
import org.oddjob.util.Restore;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StateExpressionsTest {

    @Test
    public void testEquals() throws Exception {

        FlagState job = new FlagState();

        BeanRegistry registry = mock(BeanRegistry.class);
        when(registry.lookup("MyJob", Stateful.class)).thenReturn(job);

        ArooaSession session = mock(ArooaSession.class);
        when(session.getBeanRegistry()).thenReturn(registry);


        StateExpression test = new StateExpressions.Equals(
                "MyJob", StateConditions.COMPLETE);

        AtomicReference<Try<InstantEvent<Boolean>>> result = new AtomicReference<>();

        Restore restore = test.evaluate(session, result::set);

        assertThat(result.get().orElseThrow().getOf(),
                   is(Boolean.FALSE));

        job.run();

        assertThat(result.get().orElseThrow().getOf(),
                   is(true));

        StateEvent stateEvent = job.lastStateEvent();
        assertThat(result.get().orElseThrow().getTime(),
                   is(stateEvent.getInstant()));
        assertThat(((StateEvaluation) result.get().orElseThrow()).getState(),
                   is(stateEvent.getState()));

        job.hardReset();

        assertThat(result.get().orElseThrow().getOf(),
                   is(Boolean.FALSE));

        restore.close();

        job.run();

        assertThat(result.get().orElseThrow().getOf(),
                   is(Boolean.FALSE));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOr() {

        ArooaSession session = mock(ArooaSession.class);

        AtomicReference<Consumer<Try<InstantEvent<Boolean>>>> lhsConsumer =
                new AtomicReference<>();
        AtomicReference<Consumer<Try<InstantEvent<Boolean>>>> rhsConsumer =
                new AtomicReference<>();

        Restore lhsRestore = mock(Restore.class);
        Restore rhsRestore = mock(Restore.class);

        StateExpression lhs = mock(StateExpression.class);
        when(lhs.evaluate(Mockito.any(ArooaSession.class),
                          Mockito.any(Consumer.class)))
                .thenAnswer((Answer<Restore>) invocation -> {
                    lhsConsumer.set((Consumer<Try<InstantEvent<Boolean>>>)
                                            invocation.getArguments()[1]);
                    return lhsRestore;
                });

        StateExpression rhs = mock(StateExpression.class);
        when(rhs.evaluate(Mockito.any(ArooaSession.class),
                          Mockito.any(Consumer.class)))
                .thenAnswer((Answer<Restore>) invocation -> {
                    rhsConsumer.set((Consumer<Try<InstantEvent<Boolean>>>)
                                            invocation.getArguments()[1]);
                    return rhsRestore;
                });

        AtomicReference<Try<InstantEvent<Boolean>>> result =
                new AtomicReference<>();

        StateExpression test = new StateExpressions.Or(lhs, rhs);

        Restore restore = test.evaluate(session, result::set);

        assertThat(lhsConsumer.get(), notNullValue());
        assertThat(rhsConsumer.get(), notNullValue());

        assertThat(result.get(), nullValue());

        StateInstant stateInstant1 = StateInstant.parse("2023-06-19T08:55:00Z");
        StateInstant stateInstant2 = StateInstant.parse("2023-06-19T08:55:01Z");

        StateEvent lhsEvent = StateEvent.atInstant(mock(Stateful.class),
                                             JobState.READY,
                                             stateInstant1);

        StateEvent rhsEvent = StateEvent.atInstant(mock(Stateful.class),
                                             JobState.READY,
                                             stateInstant2);


        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, lhsEvent)));

        assertThat(result.get(), nullValue());

        rhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, rhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(Boolean.FALSE));
        assertThat(result.get().orElseThrow().getTime(),
                is(stateInstant2.getInstant()));

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(true, lhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(true));

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, lhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(Boolean.FALSE));

        rhsConsumer.get().accept(Try.of(
                new StateEvaluation(true, rhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(true));

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(true, lhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(true));

        lhsConsumer.get().accept(Try.fail(new Exception("Doh!")));

        try {
            result.get().orElseThrow(Function.identity());
            fail("Should throw an Exception");
        } catch (Throwable e) {
            assertThat(e.getMessage(), is("Doh!"));
        }

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(true, lhsEvent)));
        rhsConsumer.get().accept(Try.fail(new Exception("Doh!")));

        try {
            result.get().orElseThrow(Function.identity());
            fail("Should throw an Exception");
        } catch (Throwable e) {
            assertThat(e.getMessage(), is("Doh!"));
        }

        restore.close();

        Mockito.verify(lhsRestore).close();
        Mockito.verify(rhsRestore).close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAnd() {

        ArooaSession session = mock(ArooaSession.class);

        AtomicReference<Consumer<Try<InstantEvent<Boolean>>>> lhsConsumer =
                new AtomicReference<>();
        AtomicReference<Consumer<Try<InstantEvent<Boolean>>>> rhsConsumer =
                new AtomicReference<>();

        Restore lhsRestore = mock(Restore.class);
        Restore rhsRestore = mock(Restore.class);

        StateExpression lhs = mock(StateExpression.class);
        when(lhs.evaluate(Mockito.any(ArooaSession.class),
                          Mockito.any(Consumer.class)))
                .thenAnswer((Answer<Restore>) invocation -> {
                    lhsConsumer.set((Consumer<Try<InstantEvent<Boolean>>>)
                                            invocation.getArguments()[1]);
                    return lhsRestore;
                });

        StateExpression rhs = mock(StateExpression.class);
        when(rhs.evaluate(Mockito.any(ArooaSession.class),
                          Mockito.any(Consumer.class)))
                .thenAnswer((Answer<Restore>) invocation -> {
                    rhsConsumer.set((Consumer<Try<InstantEvent<Boolean>>>)
                                            invocation.getArguments()[1]);
                    return rhsRestore;
                });

        AtomicReference<Try<InstantEvent<Boolean>>> result =
                new AtomicReference<>();

        StateExpression test = new StateExpressions.And(lhs, rhs);

        Restore restore = test.evaluate(session, result::set);

        assertThat(lhsConsumer.get(), notNullValue());
        assertThat(rhsConsumer.get(), notNullValue());

        assertThat(result.get(), nullValue());

        StateInstant stateInstant1 = StateInstant.parse("2023-06-19T08:55:00Z");
        StateInstant stateInstant2 = StateInstant.parse("2023-06-19T08:55:01Z");

        StateEvent lhsEvent = StateEvent.atInstant(mock(Stateful.class),
                                             JobState.READY,
                                             stateInstant1);

        StateEvent rhsEvent = StateEvent.atInstant(mock(Stateful.class),
                                             JobState.READY,
                                             stateInstant2);

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, lhsEvent)));

        assertThat(result.get(), nullValue());

        rhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, rhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(Boolean.FALSE));

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(true, lhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(false));

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, lhsEvent)));

        rhsConsumer.get().accept(Try.of(
                new StateEvaluation(true, rhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(false));

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(true, lhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(true));

        rhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, rhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(false));

        lhsConsumer.get().accept(Try.fail(new Exception("Doh!")));

        try {
            result.get().orElseThrow(Function.identity());
            fail("Should throw an Exception");
        } catch (Throwable e) {
            assertThat(e.getMessage(), is("Doh!"));
        }

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(true, lhsEvent)));

        rhsConsumer.get().accept(Try.fail(new Exception("Doh!")));

        try {
            result.get().orElseThrow(Function.identity());
            fail("Should throw an Exception");
        } catch (Throwable e) {
            assertThat(e.getMessage(), is("Doh!"));
        }

        restore.close();

        Mockito.verify(lhsRestore).close();
        Mockito.verify(rhsRestore).close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNot() {

        // Given

        ArooaSession session = mock(ArooaSession.class);

        AtomicReference<Consumer<Try<InstantEvent<Boolean>>>> exprConsumer =
                new AtomicReference<>();

        Restore exprRestore = mock(Restore.class);

        StateExpression expr = mock(StateExpression.class);
        when(expr.evaluate(Mockito.any(ArooaSession.class),
                           Mockito.any(Consumer.class)))
                .thenAnswer((Answer<Restore>) invocation -> {
                    exprConsumer.set((Consumer<Try<InstantEvent<Boolean>>>)
                                             invocation.getArguments()[1]);
                    return exprRestore;
                });

        AtomicReference<Try<InstantEvent<Boolean>>> result =
                new AtomicReference<>();

        StateExpression test = new StateExpressions.Not(expr);

        // When

        Restore restore = test.evaluate(session, result::set);

        // Then

        assertThat(exprConsumer.get(), notNullValue());

        assertThat(result.get(), nullValue());

        StateEvent stateEvent = StateEvent.now(mock(Stateful.class),
                                             JobState.READY);

        exprConsumer.get().accept(Try.of(
                new StateEvaluation(false, stateEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(true));

        exprConsumer.get().accept(Try.of(
                new StateEvaluation(true, stateEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(Boolean.FALSE));

        exprConsumer.get().accept(Try.of(
                new StateEvaluation(false, stateEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(true));

        exprConsumer.get().accept(Try.fail(new Exception("Doh!")));

        try {
            result.get().orElseThrow(Function.identity());
            fail("Should throw an Exception");
        } catch (Throwable e) {
            assertThat(e.getMessage(), is("Doh!"));
        }

        restore.close();

        Mockito.verify(exprRestore).close();
    }

}
