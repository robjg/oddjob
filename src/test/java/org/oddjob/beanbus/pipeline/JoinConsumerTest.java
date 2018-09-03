package org.oddjob.beanbus.pipeline;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class JoinConsumerTest {

    private static class IdentitySection<T> implements FlushableConsumer<T> {

        private final FlushableConsumer<T> next;

        IdentitySection(FlushableConsumer<T> next) {
            this.next = next;
        }

        @Override
        public void accept(T data) {
            next.accept(data);
        }

        @Override
        public void flush() {
            next.flush();
        }

    }

    @Test
    public void testStandalone() {

        CollectionConsumer<Integer> results = new CollectionConsumer<>();

        JoinConsumer<Integer> test = new JoinConsumer<>(results);

        FlushableConsumer<Integer> c1 = new IdentitySection<>(test.useNext());
        FlushableConsumer<Integer> c2 = new IdentitySection<>(test.useNext());
        FlushableConsumer<Integer> c3 = new IdentitySection<>(test.useNext());

        c1.accept(1);
        c2.accept(2);
        c3.accept(3);

        List<Integer> resultLists;

        resultLists = results.toCollection().stream().collect(Collectors.toList());
        assertThat( resultLists.size(), is(0));

        c1.flush();

        resultLists = results.toCollection().stream().collect(Collectors.toList());
        assertThat( resultLists.size(), is(0));

        c2.flush();

        resultLists = results.toCollection().stream().collect(Collectors.toList());
        assertThat( resultLists.size(), is(0));

        c2.flush();

        resultLists = results.toCollection().stream().collect(Collectors.toList());
        assertThat( resultLists.size(), is(3));

        assertThat(resultLists.get(0), is(1));
        assertThat( resultLists.get(1), is(2));
        assertThat( resultLists.get(2), is(3));
    }

}