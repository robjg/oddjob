package org.oddjob.jmx.client;

import org.junit.jupiter.api.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentMatchers;
import org.oddjob.jmx.RemoteOperation;
import org.oddjob.jmx.server.MBeanOperation;
import org.oddjob.jmx.server.ServerInterfaceHandler;
import org.oddjob.jmx.server.ServerInterfaceManager;
import org.oddjob.remote.RemoteException;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class MockClientSideToolkitTest {

    interface SomeInterface {

        String a(Object... args);

        String b(String s, Object... args);
    }

    @Test
    void mockitoExpectationsForVarArgs() {

        SomeInterface mock = mock(SomeInterface.class);

        doAnswer(invocationOnMock -> "Called with Raw Args: "
                + Arrays.toString((Object[]) invocationOnMock.getRawArguments()[0])
                + ", Expanded Args: " + Arrays.toString(invocationOnMock.getArguments()))
                .when(mock)
                .a(AdditionalMatchers.or(ArgumentMatchers.isNull(Object[].class), any(Object[].class)));

        doAnswer(invocationOnMock -> "Called with Raw Args: ["
                + invocationOnMock.getRawArguments()[0] + ", " + Arrays.toString((Object[]) invocationOnMock.getRawArguments()[1])
                + "], Expanded Args: " + Arrays.toString(invocationOnMock.getArguments()))
                .when(mock)
                .b(any(String.class),
                        AdditionalMatchers.or(ArgumentMatchers.isNull(Object[].class), any(Object[].class)));

        assertThat(mock.a(), is("Called with Raw Args: [], Expanded Args: []"));
        assertThat(mock.a((Object) null), is("Called with Raw Args: [null], Expanded Args: [null]"));
        assertThat(mock.a((Object[]) null), is("Called with Raw Args: null, Expanded Args: [null]"));
        assertThat(mock.a("a"), is("Called with Raw Args: [a], Expanded Args: [a]"));
        assertThat(mock.a("a", "b"), is("Called with Raw Args: [a, b], Expanded Args: [a, b]"));
        assertThat(mock.a(null, null), is("Called with Raw Args: [null, null], Expanded Args: [null, null]"));

        assertThat(mock.b("Foo"), is("Called with Raw Args: [Foo, []], Expanded Args: [Foo]"));
        assertThat(mock.b("Foo", (Object) null), is("Called with Raw Args: [Foo, [null]], Expanded Args: [Foo, null]"));
        assertThat(mock.b("Foo", (Object[]) null), is("Called with Raw Args: [Foo, null], Expanded Args: [Foo, null]"));
        assertThat(mock.b("Foo", "a"), is("Called with Raw Args: [Foo, [a]], Expanded Args: [Foo, a]"));
        assertThat(mock.b("Foo", "a", "b"), is("Called with Raw Args: [Foo, [a, b]], Expanded Args: [Foo, a, b]"));
        assertThat(mock.b("Foo", null, null), is("Called with Raw Args: [Foo, [null, null]], Expanded Args: [Foo, null, null]"));

        //        System.out.println(mockingDetails(mock).printInvocations());
    }


    @Test
    void mockCallsPassedToServerInterfaceHandler() throws Throwable {

        RemoteOperation<?> a = new MBeanOperation("a",
                new String[]{Object[].class.getName()});
        RemoteOperation<?> toString = new MBeanOperation("toString",
                new String[]{});
        RemoteOperation<?> hashCode = new MBeanOperation("hashCode",
                new String[]{});
        RemoteOperation<?> equals = new MBeanOperation("equals",
                new String[]{Object.class.getName()});


        ServerInterfaceHandler serverInterfaceManager = mock(ServerInterfaceHandler.class);
        when(serverInterfaceManager.invoke(eq(a), any(Object[].class)))
                .thenReturn("Foo");
        when(serverInterfaceManager.invoke(eq(toString), isNull()))
                .thenReturn("SIM");
        when(serverInterfaceManager.invoke(eq(hashCode), isNull()))
                .thenReturn(42);
        when(serverInterfaceManager.invoke(eq(equals), any(Object[].class)))
                .thenReturn(true);

        ClientSideToolkit clientSideToolkit = MockClientSideToolkit.mockToolkit(serverInterfaceManager);

        SomeInterface someInterface = new DirectInvocationClientFactory<>(SomeInterface.class)
                .createClientHandler(null, clientSideToolkit);

        assertThat(someInterface.a(), is("Foo"));
        assertThat(someInterface.toString(), is("SIM"));
        assertThat(someInterface.hashCode(), is(42));
        //noinspection EqualsBetweenInconvertibleTypes
        assertThat(someInterface.equals("Whatever"), is(true));
    }

    @Test
    void mockCallsPassedToServerInterfaceManager() throws RemoteException {

        ServerInterfaceManager serverInterfaceManager = mock(ServerInterfaceManager.class);
        when(serverInterfaceManager.invoke(eq("a"), any(Object[].class),
                eq(new String[]{Object[].class.getName()})))
                .thenReturn("Foo");
        when(serverInterfaceManager.invoke(eq("toString"), isNull(), eq(new String[]{})))
                .thenReturn("SIM");
        when(serverInterfaceManager.invoke(eq("hashCode"), isNull(), eq(new String[]{})))
                .thenReturn(42);
        when(serverInterfaceManager.invoke(eq("equals"), any(Object[].class),
                eq(new String[] { Object.class.getName() } )))
                .thenReturn(true);

        ClientSideToolkit clientSideToolkit = MockClientSideToolkit.mockToolkit(serverInterfaceManager);

        SomeInterface someInterface = new DirectInvocationClientFactory<>(SomeInterface.class)
                .createClientHandler(null, clientSideToolkit);

        assertThat(someInterface.a(), is("Foo"));
        assertThat(someInterface.toString(), is("SIM"));
        assertThat(someInterface.hashCode(), is(42));
        //noinspection EqualsBetweenInconvertibleTypes
        assertThat(someInterface.equals("Whatever"), is(true));
    }
}
