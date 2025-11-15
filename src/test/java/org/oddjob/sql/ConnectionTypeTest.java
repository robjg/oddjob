package org.oddjob.sql;


import org.junit.jupiter.api.Test;
import org.oddjob.OddjobSessionFactory;
import org.oddjob.arooa.ArooaBeanDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.life.SimpleArooaClass;
import org.oddjob.tools.OddjobTestHelper;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionTypeTest {

    @Test
    void connectionWithDriver() throws SQLException {

        ConnectionType test = new ConnectionType();
        test.setDriver("org.hsqldb.jdbcDriver");
        test.setUrl("jdbc:hsqldb:mem:test");
        test.setUsername("sa");
        test.setPassword("");

        try (Connection connection = test.toValue()) {
            assertFalse(connection.isClosed());
        } catch (ArooaConversionException e) {
            assertTrue(e.getMessage().startsWith(
                    "No connection available for"));
        }
    }

    @Test
    void connectionWithoutDriver() throws SQLException {

        ConnectionType test = new ConnectionType();
        test.setUrl("jdbc:hsqldb:mem:test");
        test.setUsername("sa");
        test.setPassword("");

        try (Connection connection = test.toValue()) {
            assertFalse(connection.isClosed());
        } catch (ArooaConversionException e) {
            assertTrue(e.getMessage().startsWith(
                    "No connection available for"));
        }
    }

    @Test
    public void testSerialize() throws Exception {
        ConnectionType test = new ConnectionType();
        test.setUrl("x:/y/z");

        ConnectionType copy = OddjobTestHelper.copy(test);

        assertEquals("x:/y/z", copy.getUrl());
    }

    @Test
    public void testIsClassLoaderAuto() {

        ArooaSession session = new OddjobSessionFactory().createSession();

        ArooaBeanDescriptor descriptor =
                session.getArooaDescriptor().getBeanDescriptor(
                        new SimpleArooaClass(ConnectionType.class),
                        session.getTools().getPropertyAccessor());

        assertTrue(descriptor.isAuto("classLoader"));
    }

    @Test
    public void testBadUrl() throws SQLException {

        ConnectionType test = new ConnectionType();
        test.setDriver("org.hsqldb.jdbcDriver");
        test.setUrl("jdbc.url=jdbc:hsqldb:mem:test");
        test.setUsername("sa");
        test.setPassword("");

        try (Connection ignored = test.toValue()) {
            fail("Should fail.");
        } catch (ArooaConversionException e) {
            assertTrue(e.getMessage().startsWith(
                    "No connection available for"));
        }
    }

    @Test
    public void testBadUrl2() throws SQLException {

        ConnectionType test = new ConnectionType();
        test.setDriver("org.hsqldb.jdbcDriver");
        test.setUrl("jdbc.url=");
        test.setUsername("sa");
        test.setPassword("");

        try (Connection ignored = test.toValue()) {
            fail("Should fail.");
        } catch (ArooaConversionException e) {
            assertTrue(e.getMessage().startsWith(
                    "No connection available for"));
        }
    }
}
