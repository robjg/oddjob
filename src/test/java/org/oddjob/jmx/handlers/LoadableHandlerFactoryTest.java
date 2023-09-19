package org.oddjob.jmx.handlers;

import org.junit.Test;
import org.oddjob.Loadable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.jmx.VanillaInterfaceHandler;
import org.oddjob.jmx.client.ClientInterfaceHandlerFactory;
import org.oddjob.jmx.client.ClientSideToolkit;
import org.oddjob.jmx.client.DirectInvocationClientFactory;
import org.oddjob.jmx.client.MockClientSideToolkit;
import org.oddjob.jmx.server.*;
import org.oddjob.remote.Implementation;
import org.oddjob.remote.RemoteException;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LoadableHandlerFactoryTest {

    public static class MyLoadable implements Loadable {
        boolean loaded;

        @Override
        public boolean isLoadable() {
            return !loaded;
        }

        @Override
        public void load() {
            loaded = true;
        }

        @Override
        public void unload() {
            loaded = false;
        }
    }

    @Test
    public void testCreation() throws ArooaConversionException, RemoteException {

        ArooaSession session = new StandardArooaSession();

        VanillaInterfaceHandler<Loadable> vanillaInterfaceHandler = new VanillaInterfaceHandler<>();
        vanillaInterfaceHandler.setArooaSession(session);
        vanillaInterfaceHandler.setClassName(Loadable.class.getName());

        HandlerFactoryProvider provider =
                new ResourceFactoryProvider(new StandardArooaSession());

        ServerInterfaceManagerFactory managerFactory =
                new ServerInterfaceManagerFactoryImpl(
                        new ServerInterfaceHandlerFactory[]{vanillaInterfaceHandler.toValue()});

        MyLoadable loadable = new MyLoadable();

        ServerInterfaceManager manager =
                managerFactory.create(loadable, MockServerSideToolkit.mockToolkit(42L));

        Implementation<?>[] implementations = manager.allClientInfo();

        String[] implClasses = Arrays.stream(implementations).map(Implementation::getType)
                .toArray(String[]::new);

        assertThat(implClasses, is(new String[]{Loadable.class.getName()}));

        ClientInterfaceHandlerFactory<?> clientFactory =
                new DirectInvocationClientFactory<>(Loadable.class);

        ClientSideToolkit clientToolkit = MockClientSideToolkit.mockToolkit(manager);

        Loadable proxy = (Loadable)
                clientFactory.createClientHandler(null, clientToolkit);

        assertThat(proxy.isLoadable(), is(true));

        proxy.load();

        assertThat(proxy.isLoadable(), is(false));
    }
}
