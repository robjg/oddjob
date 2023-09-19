package org.oddjob.jmx.server;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.remote.RemoteException;

public class ServerAllOperationsHandlerTest extends OjTestCase {

    interface Fruit {

        String getColour();
    }

    static class MyFruit implements Fruit {

        public String getColour() {
            return "pink";
        }
    }

    @Test
    public void testInvoke() throws RemoteException {

        ServerAllOperationsHandler<Fruit> test =
                new ServerAllOperationsHandler<>(
                        Fruit.class, new MyFruit(), 1L);

        Object result = test.invoke(new MBeanOperation(
                "getColour", new String[0]), new Object[0]);

        assertEquals("pink", result);
    }
}
