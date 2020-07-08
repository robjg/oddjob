/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.jmx;

import org.apache.commons.beanutils.PropertyUtils;
import org.junit.Before;
import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.ArooaConfiguration;
import org.oddjob.arooa.convert.ConversionFailedException;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.convert.NoConversionAvailableException;
import org.oddjob.arooa.parsing.ConfigurationOwner;
import org.oddjob.arooa.parsing.ConfigurationSession;
import org.oddjob.arooa.registry.BeanDirectoryOwner;
import org.oddjob.arooa.types.XMLConfigurationType;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.jmx.client.DirectInvocationBean;
import org.oddjob.logging.ConsoleArchiver;
import org.oddjob.logging.LogEvent;
import org.oddjob.logging.LogListener;
import org.oddjob.scheduling.DefaultExecutors;
import org.oddjob.scheduling.TrackingServices;
import org.oddjob.state.ParentState;
import org.oddjob.tools.WaitHelper;
import org.oddjob.values.VariablesJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;

/**
 * Test for both client and server together.
 */
public class TogetherTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(TogetherTest.class);

    @Before
    public void setUp() {
        logger.debug("================= " + getName() + "==================");
    }

    String EOL = System.getProperty("line.separator");

    /**
     * Test that a value can be retrieved across multiple client/servers.
     * Also test logging.
     *
     * @throws ConversionFailedException
     * @throws NoConversionAvailableException
     * @throws Exception
     */
    @Test
    public void testMultipleClientServers() throws NoConversionAvailableException, ConversionFailedException, Exception {

        DefaultExecutors services = new DefaultExecutors();

        Oddjob oj = new Oddjob();
        oj.setOddjobExecutors(services);
        oj.setConfiguration(new XMLConfiguration("Resource",
                this.getClass().getResourceAsStream("together1.xml")));

        logger.info("** Before Running ** ");

        oj.run();

        OddjobLookup lookup = new OddjobLookup(oj);

        assertEquals("apples", lookup.lookup("result.fruit", String.class));

        // test logging.

        class LL implements LogListener {
            final List<String> messages = new ArrayList<>();

            public void logEvent(LogEvent logEvent) {
                messages.add(logEvent.getMessage().trim());
            }
        }

        ConsoleArchiver archiver1 = (ConsoleArchiver) new OddjobLookup(
                oj).lookup("client1");

        Object fruit1 = new OddjobLookup(oj).lookup("client1/fruit");

        LL results1 = new LL();

        archiver1.addConsoleListener(results1, fruit1, -1, 1);

        assertThat(results1.messages.contains("apples"), is(true));

        archiver1.removeConsoleListener(results1, fruit1);

        ConsoleArchiver archiver2 = (ConsoleArchiver) new OddjobLookup(
                oj).lookup("client2");

        Object fruit2 = new OddjobLookup(oj).lookup("client2/client1/fruit");

        LL results2 = new LL();

        archiver2.addConsoleListener(results2, fruit2, -1, 1);

        assertThat(results2.messages.contains("apples"), is(true));

        archiver2.removeConsoleListener(results2, fruit2);

        // stop

        Runnable stopAll = (Runnable) new OddjobLookup(oj).lookup("stopAll");

        stopAll.run();

        logger.info("** After Running ** ");
        oj.destroy();

        services.stop();
    }

    /**
     * Test that a value can be retrieved and set across multiple
     * servers.
     *
     * @throws Exception
     */
    @Test
    public void testValueCanBeRetrievedAccrossMultipleServers() throws Exception {

        File file = new File(getClass().getResource("together2.xml").getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);

        oddjob.run();

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(
                oddjob);

        assertEquals("apples", lookup.lookup("result.echo", String.class));

        oddjob.destroy();
    }

    /**
     * Test a serving a nested oddjob.
     */
    @Test
    public void testServingNestedOddjob() throws Exception {

        Oddjob oj = new Oddjob();
        oj.setConfiguration(
                new XMLConfiguration("org/oddjob/jmx/together3.xml",
                        getClass().getClassLoader()));
        XMLConfigurationType configType = new XMLConfigurationType();
        configType.setResource("org/oddjob/jmx/together3a.xml");

        oj.setExport("child-config", configType);
        oj.run();

        VariablesJob result = (VariablesJob) new OddjobLookup(
                oj).lookup("oj/result");
        assertNotNull(result);

        Object o = new DefaultConverter().convert(
                result.get("echo"), Object.class);
        assertEquals("apples", o);
    }

    // test a client and server pair that share each other.
    @Test
    public void testClientServerLoopback() throws Exception {

        TrackingServices services = new TrackingServices(3);

        final Oddjob oddjob = new Oddjob();
        oddjob.setOddjobExecutors(services);
        oddjob.setConfiguration(new XMLConfiguration("Resource",
                this.getClass().getResourceAsStream("together4.xml")));

//		OddjobExplorer e = new OddjobExplorer();
//		e.setOddjob(oddjob);
//		Thread t = new Thread(e);
//		t.start();

        oddjob.run();

        WaitHelper waitHelper = new WaitHelper() {

            @Override
            public boolean condition() {
                return !oddjob.lastStateEvent().getState().isStoppable();
            }
        };

        waitHelper.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

//		t.join();

        Object test1 = new OddjobLookup(oddjob).lookup("test1");
        assertEquals("oranges", PropertyUtils.getProperty(test1, "text"));

        Object test2 = new OddjobLookup(oddjob).lookup("test2");
        assertEquals("apples", PropertyUtils.getProperty(test2, "text"));

        services.stop();

        oddjob.destroy();

    }

    public interface Foo {
        String foo();
    }

    public static class FooImpl implements Foo {
        public String foo() {
            return "Apples";
        }
    }

    @Test
    public void testAnyInterface() {

        String serverXml =
                "<oddjob xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
                        " <job>" +
                        "  <sequential>" +
                        "   <jobs>" +
                        "    <folder>" +
                        "     <jobs>" +
                        "      <bean id='foo' class='" + FooImpl.class.getName() + "'/>" +
                        "     </jobs>" +
                        "    </folder>" +
                        "    <rmireg/>" +
                        "    <jmx:server id='server' url='service:jmx:rmi://ignored/jndi/rmi://localhost/server1' root='${foo}'>" +
                        "     <handlerFactories>" +
                        "      <bean class='org.oddjob.jmx.server.HandlerFactoryBean'>" +
                        "       <handlerFactories>" +
                        "		 <list>" +
                        "         <values>" +
                        "          <bean class='" + VanillaInterfaceHandler.class.getName() + "' className='" + Foo.class.getName() + "'/>" +
                        "         </values>" +
                        "        </list>" +
                        "       </handlerFactories>" +
                        "      </bean>" +
                        "     </handlerFactories>" +
                        "    </jmx:server>" +
                        "   </jobs>" +
                        "  </sequential>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob serverOddjob = new Oddjob();
        serverOddjob.setConfiguration(
                new XMLConfiguration("XML", serverXml));

        serverOddjob.run();

        assertEquals(ParentState.STARTED, serverOddjob.lastStateEvent().getState());

        String serverAddress = (String) new OddjobLookup(
                serverOddjob).lookup("server.address");

        String clientXml =
                "<oddjob id='this' xmlns:jmx='http://rgordon.co.uk/oddjob/jmx'>" +
                        " <job>" +
                        "    <jmx:client id='client' name='Client' connection='${this.args[0]}'>" +
                        "     <handlerFactories>" +
                        "      <bean class='org.oddjob.jmx.client.HandlerFactoryBean'>" +
                        "       <handlerFactories>" +
                        "		 <list>" +
                        "         <values>" +
                        "          <bean class='" + DirectInvocationBean.class.getName() + "' className='" + Foo.class.getName() + "'/>" +
                        "         </values>" +
                        "        </list>" +
                        "       </handlerFactories>" +
                        "      </bean>" +
                        "     </handlerFactories>" +
                        "    </jmx:client>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob clientOddjob = new Oddjob();
        clientOddjob.setConfiguration(
                new XMLConfiguration("XML", clientXml));
        clientOddjob.setArgs(new String[]{serverAddress});

        clientOddjob.run();

        Foo foo = (Foo) new OddjobLookup(
                clientOddjob).lookup("client/foo");

        String result = foo.foo();

        assertEquals("Apples", result);

        clientOddjob.destroy();

        serverOddjob.destroy();
    }

    @Test
    public void testForEachConfigurationOwner() throws FailedToStopException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/jmx/TogetherForEachTestMain.xml",
                getClass().getClassLoader()));

        oddjob.run();

        assertEquals(ParentState.STARTED, oddjob.lastStateEvent().getState());

        Object client = new OddjobLookup(oddjob).lookup("client");

        Object foreach = new OddjobLookup(
                (BeanDirectoryOwner) client).lookup("oj/foreach");

        assertTrue(foreach instanceof ConfigurationOwner);

        ConfigurationSession configurationSession = ((ConfigurationOwner) foreach).provideConfigurationSession();

        assertNotNull(configurationSession);

        ArooaConfiguration config = configurationSession.dragPointFor(foreach);

        assertNotNull(config);

        ((Stoppable) client).stop();

        oddjob.stop();


        oddjob.destroy();
    }
}
