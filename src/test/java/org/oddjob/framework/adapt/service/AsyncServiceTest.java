package org.oddjob.framework.adapt.service;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Stateful;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.framework.adapt.Start;
import org.oddjob.framework.adapt.Stop;
import org.oddjob.state.ServiceState;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class AsyncServiceTest {

    public interface Completable {
        void complete();
    }

    public static class MyService implements Completable {
        boolean stopped;

        CompletableFuture<Void> cf;

        @Start
        public CompletableFuture<Void> start() {
            cf = new CompletableFuture<>();
            return cf;
        }

        @Stop
        public void stop() {
            stopped = true;
        }

        public boolean isStopped() {
            return stopped;
        }

        @Override
        public void complete() {
            cf.complete(null);
        }
    }

    @Test
    public void testInOddjob() throws Exception {

        String xml = "<oddjob>" +
                " <job>" +
                "  <bean class='" + MyService.class.getName() + "' id='s' />" +
                " </job>" +
                "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        Object test = new OddjobLookup(oddjob).lookup("s");
        assertThat(((Stateful) test).lastStateEvent().getState(), is(ServiceState.STARTING));

        ((Completable) test).complete();

        assertThat(((Stateful) test).lastStateEvent().getState(), is(ServiceState.STARTED));

        oddjob.stop();

        assertThat(((Stateful) test).lastStateEvent().getState(), is(ServiceState.STOPPED));

        assertThat(PropertyUtils.getProperty(test, "stopped"), is(true));

        oddjob.destroy();
    }
}
