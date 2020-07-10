package org.oddjob.jmx;

import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.GenericState;
import org.oddjob.state.JobState;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;

import static org.hamcrest.Matchers.is;

public class StatefulTest extends OjTestCase {

    class Result implements StateListener {
        StateEvent event;

        public void jobStateChange(StateEvent event) {
            this.event = event;
            synchronized (this) {
                notifyAll();
            }
        }
    }


    @Test
    public void testState() throws ArooaConversionException, InterruptedException {

        String xml =
                "<oddjob xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <rmireg />" +
                        "    <jmx:server id='server1'" +
                        "            url='service:jmx:rmi://ignored/jndi/rmi://localhost/StatefulTest_testState'" +
                        "            root='${fruit}' />" +
                        " 	 <echo id='fruit'>apples</echo>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        String address = new OddjobLookup(
                oddjob).lookup("server1.address", String.class);

        JMXClientJob client = new JMXClientJob();
        client.setArooaSession(new StandardArooaSession());
        client.setConnection(address);

        client.run();

        Stateful fruit = new OddjobLookup(client).lookup("fruit", Stateful.class);

        assertNotNull(fruit);

        Result result = new Result();

        fruit.addStateListener(result);

        assertThat(GenericState.statesEquivalent(JobState.COMPLETE, result.event.getState()),
                is(true));

        Resetable resetable = (Resetable) fruit;

        resetable.hardReset();

        synchronized (result) {
            result.wait(5000);
        }
        assertThat(GenericState.statesEquivalent(JobState.READY, result.event.getState()),
                is(true));

        client.destroy();

        oddjob.destroy();
    }
}
