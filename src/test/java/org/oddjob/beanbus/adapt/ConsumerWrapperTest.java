package org.oddjob.beanbus.adapt;

import org.junit.Test;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.framework.Service;
import org.oddjob.framework.adapt.service.ServiceAdaptor;
import org.oddjob.framework.adapt.service.ServiceStrategies;

import java.util.function.Consumer;

import static org.mockito.Mockito.*;

public class ConsumerWrapperTest {

    interface ServiceConsumer extends Consumer<Object>, Service {
    }

    @Test
    public void testServiceMethodsCalled() throws Exception {

        ServiceConsumer wrapped = mock(ServiceConsumer.class);
        Object proxy = mock(Object.class);

        ArooaSession session = new StandardArooaSession();

        ServiceAdaptor serviceAdaptor = new ServiceStrategies()
                .adapt(wrapped, session)
                .orElseThrow(() -> new RuntimeException("Unexpected"));


        ConsumerWrapper<Object> test = new ConsumerWrapper<>(serviceAdaptor, wrapped, proxy);

        test.run();

        verify(wrapped, times(1)).start();

        test.stop();

        verify(wrapped, times(1)).stop();
    }
}