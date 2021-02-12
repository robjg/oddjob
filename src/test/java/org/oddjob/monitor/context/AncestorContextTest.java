package org.oddjob.monitor.context;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AncestorContextTest {

    @Test
    public void testFindAncestorOfType() {

        class SomeContext<T> implements AncestorContext {

            private final AncestorContext parent;

            private final T component;

            SomeContext(AncestorContext parent, T component) {
                this.parent = parent;
                this.component = component;
            }

            @Override
            public T getThisComponent() {
                return component;
            }

            @Override
            public AncestorContext getParent() {
                return parent;
            }
        }

        SomeContext<String> a = new SomeContext<>(null, "Hello");
        SomeContext<Integer> b = new SomeContext<>(a, 2);
        SomeContext<Integer> c = new SomeContext<>(b, 3);

        //noinspection OptionalGetWithoutIsPresent
        assertThat(c.findAncestorOfType(String.class).get(), is("Hello"));
    }

}