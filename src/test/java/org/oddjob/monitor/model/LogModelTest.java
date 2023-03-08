/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor.model;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.LogEvent;

import java.util.Observable;
import java.util.Observer;

/**
 *
 */
public class LogModelTest extends OjTestCase implements LogEventProcessor {

    String message;

    @Test
    public void testMessage() {
        class MyOb implements Observer {
            public void update(Observable o, Object arg) {
                ((LogAction) arg).accept(LogModelTest.this);
            }
        }
        MyOb ob = new MyOb();

        LogModel test = new LogModel();
        test.addObserver(ob);
        test.logEvent(new LogEvent("foo", 2, LogLevel.DEBUG, "Hello"));
        assertEquals("Hello", message);
    }

    /* (non-Javadoc)
     * @see org.oddjob.monitor.model.LogEventProcessor#onClear()
     */
    public void onClear() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.oddjob.monitor.model.LogEventProcessor#onEvent(java.lang.String, org.oddjob.logging.LogLevel)
     */
    public void onEvent(String text, LogLevel level) {
        message = text;
    }

    /* (non-Javadoc)
     * @see org.oddjob.monitor.model.LogEventProcessor#onUnavailable()
     */
    public void onUnavailable() {
        // TODO Auto-generated method stub

    }
}

