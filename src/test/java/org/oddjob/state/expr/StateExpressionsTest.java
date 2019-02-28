package org.oddjob.state.expr;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.oddjob.Stateful;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.registry.BeanRegistry;
import org.oddjob.arooa.utils.Try;
import org.oddjob.events.EventOf;
import org.oddjob.state.FlagState;
import org.oddjob.state.JobState;
import org.oddjob.state.StateConditions;
import org.oddjob.state.StateEvent;
import org.oddjob.util.Restore;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
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

        AtomicReference<Try<EventOf<Boolean>>> result = new AtomicReference<>();

        Restore restore = test.evaluate(session, result::set);

        assertThat(result.get().orElseThrow().getOf(),
                   is(false));

        job.run();

        assertThat(result.get().orElseThrow().getOf(),
                   is(true));

        StateEvent stateEvent = job.lastStateEvent();
        assertThat(result.get().orElseThrow().getTime(),
                   is(stateEvent.getTime().toInstant()));
        assertThat(((StateEvaluation) result.get().orElseThrow()).getState(),
                   is(stateEvent.getState()));

        job.hardReset();

        assertThat(result.get().orElseThrow().getOf(),
                   is(false));

        restore.close();

        job.run();

        assertThat(result.get().orElseThrow().getOf(),
                   is(false));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOr() {

        ArooaSession session = mock(ArooaSession.class);

        AtomicReference<Consumer<Try<EventOf<Boolean>>>> lhsConsumer =
                new AtomicReference<>();
        AtomicReference<Consumer<Try<EventOf<Boolean>>>> rhsConsumer =
                new AtomicReference<>();

        Restore lhsRestore = mock(Restore.class);
        Restore rhsRestore = mock(Restore.class);

        StateExpression lhs = mock(StateExpression.class);
        when(lhs.evaluate(Mockito.any(ArooaSession.class),
                          Mockito.any(Consumer.class)))
                .thenAnswer((Answer<Restore>) invocation -> {
                    lhsConsumer.set((Consumer<Try<EventOf<Boolean>>>)
                                            invocation.getArguments()[1]);
                    return lhsRestore;
                });

        StateExpression rhs = mock(StateExpression.class);
        when(rhs.evaluate(Mockito.any(ArooaSession.class),
                          Mockito.any(Consumer.class)))
                .thenAnswer((Answer<Restore>) invocation -> {
                    rhsConsumer.set((Consumer<Try<EventOf<Boolean>>>)
                                            invocation.getArguments()[1]);
                    return rhsRestore;
                });

        AtomicReference<Try<EventOf<Boolean>>> result =
                new AtomicReference<>();

        StateExpression test = new StateExpressions.Or(lhs, rhs);

        Restore restore = test.evaluate(session, result::set);

        assertThat(lhsConsumer.get(), notNullValue());
        assertThat(rhsConsumer.get(), notNullValue());

        assertThat(result.get(), nullValue());

        StateEvent lhsEvent = new StateEvent(mock(Stateful.class),
                                             JobState.READY,
                                             new Date(2L),
                                             null);

        StateEvent rhsEvent = new StateEvent(mock(Stateful.class),
                                             JobState.READY,
                                             new Date(5L),
                                             null);


        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, lhsEvent)));

        assertThat(result.get(), nullValue());

        rhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, rhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(false));
        assertThat(result.get().orElseThrow().getTime(),
                is(Instant.ofEpochMilli(5L)));

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(true, lhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(true));

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, lhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(false));

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

        AtomicReference<Consumer<Try<EventOf<Boolean>>>> lhsConsumer =
                new AtomicReference<>();
        AtomicReference<Consumer<Try<EventOf<Boolean>>>> rhsConsumer =
                new AtomicReference<>();

        Restore lhsRestore = mock(Restore.class);
        Restore rhsRestore = mock(Restore.class);

        StateExpression lhs = mock(StateExpression.class);
        when(lhs.evaluate(Mockito.any(ArooaSession.class),
                          Mockito.any(Consumer.class)))
                .thenAnswer((Answer<Restore>) invocation -> {
                    lhsConsumer.set((Consumer<Try<EventOf<Boolean>>>)
                                            invocation.getArguments()[1]);
                    return lhsRestore;
                });

        StateExpression rhs = mock(StateExpression.class);
        when(rhs.evaluate(Mockito.any(ArooaSession.class),
                          Mockito.any(Consumer.class)))
                .thenAnswer((Answer<Restore>) invocation -> {
                    rhsConsumer.set((Consumer<Try<EventOf<Boolean>>>)
                                            invocation.getArguments()[1]);
                    return rhsRestore;
                });

        AtomicReference<Try<EventOf<Boolean>>> result =
                new AtomicReference<>();

        StateExpression test = new StateExpressions.And(lhs, rhs);

        Restore restore = test.evaluate(session, result::set);

        assertThat(lhsConsumer.get(), notNullValue());
        assertThat(rhsConsumer.get(), notNullValue());

        assertThat(result.get(), nullValue());

        StateEvent lhsEvent = new StateEvent(mock(Stateful.class),
                                             JobState.READY,
                                             new Date(2L),
                                             null);

        StateEvent rhsEvent = new StateEvent(mock(Stateful.class),
                                             JobState.READY,
                                             new Date(5L),
                                             null);

        lhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, lhsEvent)));

        assertThat(result.get(), nullValue());

        rhsConsumer.get().accept(Try.of(
                new StateEvaluation(false, rhsEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(false));

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

        AtomicReference<Consumer<Try<EventOf<Boolean>>>> exprConsumer =
                new AtomicReference<>();

        Restore exprRestore = mock(Restore.class);

        StateExpression expr = mock(StateExpression.class);
        when(expr.evaluate(Mockito.any(ArooaSession.class),
                           Mockito.any(Consumer.class)))
                .thenAnswer((Answer<Restore>) invocation -> {
                    exprConsumer.set((Consumer<Try<EventOf<Boolean>>>)
                                             invocation.getArguments()[1]);
                    return exprRestore;
                });

        AtomicReference<Try<EventOf<Boolean>>> result =
                new AtomicReference<>();

        StateExpression test = new StateExpressions.Not(expr);

        // When

        Restore restore = test.evaluate(session, result::set);

        // Then

        assertThat(exprConsumer.get(), notNullValue());

        assertThat(result.get(), nullValue());

        StateEvent stateEvent = new StateEvent(mock(Stateful.class),
                                             JobState.READY,
                                             new Date(5L),
                                             null);

        exprConsumer.get().accept(Try.of(
                new StateEvaluation(false, stateEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(true));

        exprConsumer.get().accept(Try.of(
                new StateEvaluation(true, stateEvent)));

        assertThat(result.get().orElseThrow().getOf(),
                   is(false));

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
