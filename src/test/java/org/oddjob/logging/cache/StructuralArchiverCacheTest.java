/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.cache;

import org.junit.Test;
import org.oddjob.OjTestCase;
import org.oddjob.Structural;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.*;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

/**
 * Test LogArchiverCache
 */
public class StructuralArchiverCacheTest extends OjTestCase {

    public static class Thing implements LogEnabled {
        public String loggerName() {
            return "thing";
        }
    }

    static class MyLL implements LogListener {
        LogEvent lev;
        int count;

        public void logEvent(LogEvent logEvent) {
            count++;
            lev = logEvent;
        }
    }

    static class R implements ArchiveNameResolver {
        public String resolveName(Object component) {
            return LogHelper.getLogger(component);
        }
    }

    // archive something.
    @Test
    public void testAddEvent() {
        Thing thing = new Thing();
        StructuralArchiverCache test = new StructuralArchiverCache(thing, new R());
        assertEquals(-1, test.getLastMessageNumber("thing"));

        MyLL ll = new MyLL();
        test.addLogListener(ll, thing, LogLevel.DEBUG, -1, 2000);
        assertNull(ll.lev);

        test.addEvent("thing", LogLevel.DEBUG, "Hello");
        assertEquals("Hello", ll.lev.getMessage());
        assertEquals(0, ll.lev.getNumber());
    }

    // try and break log archiver
    @Test
    public void testBadAddListeners() {
        Thing thing = new Thing();
        StructuralArchiverCache test = new StructuralArchiverCache(thing, new R());
        MyLL ll = new MyLL();

        // no archive
        test.addLogListener(ll, thing, LogLevel.DEBUG, -1, 2000);
        assertNull(ll.lev);
        test.removeLogListener(ll, thing);

        // -ve max
        test.addLogListener(ll, thing, LogLevel.DEBUG, 2, -1);
        assertNull(ll.lev);
        test.removeLogListener(ll, thing);
    }

    // archive then listen.
    public void TestAddListenLater() {
        Thing t = new Thing();
        StructuralArchiverCache lai = new StructuralArchiverCache(t, new R());
        lai.addEvent("thing", LogLevel.DEBUG, "x");
        lai.addEvent("thing", LogLevel.DEBUG, "y");
        lai.addEvent("thing", LogLevel.DEBUG, "z");
        assertEquals(2, lai.getLastMessageNumber("thing"));


        MyLL ll = new MyLL();
        lai.addLogListener(ll, "thing", LogLevel.DEBUG, -1, 2);

        assertEquals("z", ll.lev.getMessage());
        assertEquals(2, ll.count);
        assertEquals(2, ll.lev.getNumber());
    }

    static class S implements Structural {
        public void addStructuralListener(StructuralListener listener) {
            listener.childAdded(new StructuralEvent(this, new Thing(), 0));
            listener.childAdded(new StructuralEvent(this, new Thing(), 1));
            listener.childRemoved(new StructuralEvent(this, new Thing(), 1));
            listener.childRemoved(new StructuralEvent(this, new Thing(), 0));
        }

        public void removeStructuralListener(StructuralListener listener) {
        }
    }

    public void TestSameArchiveName() {
        S s = new S();
        StructuralArchiverCache lai = new StructuralArchiverCache(s, new R());
        assertFalse(lai.hasArchive("thing"));
    }
}
