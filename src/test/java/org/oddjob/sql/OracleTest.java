package org.oddjob.sql;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.utils.DateHelper;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;

public class OracleTest extends OjTestCase {

    @Test
    public void testQueryAllTypes()
            throws ArooaPropertyException, ArooaConversionException, ParseException {

        if (System.getProperty("oracle.home") == null) {
            return;
        }

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/sql/OracleAllTypesQuery.xml",
                getClass().getClassLoader()));
        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        assertEquals("apples    ", lookup.lookup(
                "query-char-types.results.row.A_CHAR", String.class));
        assertEquals("oranges   ", lookup.lookup(
                "query-char-types.results.row.AN_NCHAR", String.class));
        assertEquals("pears", lookup.lookup(
                "query-char-types.results.row.A_VARCHAR", String.class));
        assertEquals("bananas", lookup.lookup(
                "query-char-types.results.row.AN_NVARCHAR", String.class));
        assertEquals(true, lookup.lookup(
                "query-char-types.results.row.A_CLOB") instanceof java.sql.Clob);
        assertEquals(true, lookup.lookup(
                "query-char-types.results.row.AN_NCLOB") instanceof java.sql.Clob);
        assertEquals("grapes", lookup.lookup(
                "query-char-types.results.row.A_LONG", String.class));

        assertEquals(9, (byte) lookup.lookup(
                "query-numeric-types.results.row.A_SINGLE_DIGIT_NUMBER", byte.class));
        assertEquals(99, (byte) lookup.lookup(
                "query-numeric-types.results.row.A_DOUBLE_DIGIT_NUMBER", byte.class));
        assertEquals(999, (short) lookup.lookup(
                "query-numeric-types.results.row.A_THREE_DIGIT_NUMBER", short.class));
        assertEquals(999888, (int) lookup.lookup(
                "query-numeric-types.results.row.A_SIX_DIGIT_NUMBER", int.class));
        assertEquals(9998887776L, (long) lookup.lookup(
                "query-numeric-types.results.row.A_TEN_DIGIT_NUMBER", long.class));
        assertEquals(new BigDecimal("99988877766655544433322211199988877766"),
                lookup.lookup("query-numeric-types.results.row.A_THIRTY_EIGHT_DIGIT_NUMBER",
                        BigDecimal.class));
        assertEquals(0.0099F, lookup.lookup(
                "query-numeric-types.results.row.A_SMALL_DECIMAL", float.class), 000000.1);
        assertEquals(new BigDecimal("0.99988877766655544433322211199988877766"),
                lookup.lookup("query-numeric-types.results.row.A_SMALL_PRECISE_DECIMAL",
                        BigDecimal.class));
        assertEquals(9998887.77, lookup.lookup(
                "query-numeric-types.results.row.A_MEDIUM_DECIMAL", double.class), 000000.1);
        assertEquals(new BigDecimal("999888777666555444333222111999888777.66"),
                lookup.lookup("query-numeric-types.results.row.A_LARGE_DECIMAL",
                        BigDecimal.class));
        assertEquals(new BigDecimal("900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"), lookup.lookup(
                "query-numeric-types.results.row.A_NUMBER", BigDecimal.class));
        assertEquals(new BigDecimal("99988877766655544433322211199988877766"), lookup.lookup(
                "query-numeric-types.results.row.AN_INTEGER"));
        assertEquals(Double.parseDouble("1.0E+100"), lookup.lookup(
                "query-numeric-types.results.row.A_FLOAT"));

        assertEquals(DateHelper.parseDate("2012-12-28"), lookup.lookup(
                "query-date-types.results.row.A_DATE", Date.class));
        assertEquals("2012-12-28 18:45:23.045", lookup.lookup(
                "query-date-types.results.row.A_TIMESTAMP", String.class));
        assertEquals("1000-1", lookup.lookup(
                "query-date-types.results.row.A_YEAR_INTERVAL", String.class));
        assertEquals("500 5:12:10.222", lookup.lookup(
                "query-date-types.results.row.A_DAY_INTERVAL", String.class));

        oddjob.destroy();

    }

    @Test
    public void testTypeBasedDialect()
            throws ArooaPropertyException, ArooaConversionException, ParseException {

        if (System.getProperty("oracle.home") == null) {
            return;
        }

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/sql/OracleAllTypesQuery.xml",
                getClass().getClassLoader()));
        oddjob.setExport("dialect",
                new ArooaObject(new TypeBasedDialect()));
        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        assertEquals("apples    ", lookup.lookup(
                "query-char-types.results.row.A_CHAR", String.class));
        assertEquals("oranges   ", lookup.lookup(
                "query-char-types.results.row.AN_NCHAR", String.class));
        assertEquals("pears", lookup.lookup(
                "query-char-types.results.row.A_VARCHAR", String.class));
        assertEquals("bananas", lookup.lookup(
                "query-char-types.results.row.AN_NVARCHAR", String.class));
        assertEquals(true, lookup.lookup(
                "query-char-types.results.row.A_CLOB") instanceof java.sql.Clob);
        assertEquals(true, lookup.lookup(
                "query-char-types.results.row.AN_NCLOB") instanceof java.sql.Clob);
        assertEquals("grapes", lookup.lookup(
                "query-char-types.results.row.A_LONG", String.class));

        assertEquals(9, (byte) lookup.lookup(
                "query-numeric-types.results.row.A_SINGLE_DIGIT_NUMBER", byte.class));
        assertEquals(99, (byte) lookup.lookup(
                "query-numeric-types.results.row.A_DOUBLE_DIGIT_NUMBER", byte.class));
        assertEquals(999, (short) lookup.lookup(
                "query-numeric-types.results.row.A_THREE_DIGIT_NUMBER", short.class));
        assertEquals(999888, (int) lookup.lookup(
                "query-numeric-types.results.row.A_SIX_DIGIT_NUMBER", int.class));
        assertEquals(9998887776L, (long) lookup.lookup(
                "query-numeric-types.results.row.A_TEN_DIGIT_NUMBER", long.class));
        assertEquals(new BigDecimal("99988877766655544433322211199988877766"),
                lookup.lookup("query-numeric-types.results.row.A_THIRTY_EIGHT_DIGIT_NUMBER",
                        BigDecimal.class));
        assertEquals(0.0099F, lookup.lookup(
                "query-numeric-types.results.row.A_SMALL_DECIMAL", float.class), 000000.1);
        assertEquals(new BigDecimal("0.99988877766655544433322211199988877766"),
                lookup.lookup("query-numeric-types.results.row.A_SMALL_PRECISE_DECIMAL",
                        BigDecimal.class));
        assertEquals(9998887.77, lookup.lookup(
                "query-numeric-types.results.row.A_MEDIUM_DECIMAL", double.class), 000000.1);
        assertEquals(new BigDecimal("999888777666555444333222111999888777.66"),
                lookup.lookup("query-numeric-types.results.row.A_LARGE_DECIMAL",
                        BigDecimal.class));
        assertEquals(new BigDecimal("900000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"), lookup.lookup(
                "query-numeric-types.results.row.A_NUMBER", BigDecimal.class));
        assertEquals(new BigDecimal("99988877766655544433322211199988877766"), lookup.lookup(
                "query-numeric-types.results.row.AN_INTEGER"));
        assertEquals(new BigDecimal("1.0E+100").toPlainString(), lookup.lookup(
                "query-numeric-types.results.row.A_FLOAT").toString());

        assertEquals(DateHelper.parseDate("2012-12-28"), lookup.lookup(
                "query-date-types.results.row.A_DATE", Date.class));
        assertEquals("2012-12-28 18:45:23.045", lookup.lookup(
                "query-date-types.results.row.A_TIMESTAMP", String.class));
        assertEquals("1000-1", lookup.lookup(
                "query-date-types.results.row.A_YEAR_INTERVAL", String.class));
        assertEquals("500 5:12:10.222", lookup.lookup(
                "query-date-types.results.row.A_DAY_INTERVAL", String.class));

        oddjob.destroy();

    }
}
