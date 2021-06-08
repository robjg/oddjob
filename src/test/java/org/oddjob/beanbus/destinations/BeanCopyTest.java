package org.oddjob.beanbus.destinations;

import org.apache.commons.beanutils.DynaBean;
import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.types.ArooaObject;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.ParentState;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class BeanCopyTest extends OjTestCase {

    public static class Fruit {

        String fruit;
        BigInteger quantity;
        BigDecimal price;

        public Fruit(String type, int quantity, double price) {
            this.fruit = type;
            this.quantity = BigInteger.valueOf(quantity);
            this.price = BigDecimal.valueOf(price);
        }

        public String getFruit() {
            return fruit;
        }

        public BigInteger getQuantity() {
            return quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

    }

    @Test
    public void testSimpleCopy() throws ArooaPropertyException, ArooaConversionException {

        List<Fruit> in = Arrays.asList(
                new Fruit("apple", 5, 2.45),
                new Fruit("pear", 2, 1.25));

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/BeanCopyNoClass.xml", getClass()
                .getClassLoader()));
        oddjob.setExport("iterable", new ArooaObject(in));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> results = lookup.lookup(
                "bus.to.to.beans", List.class);

        DynaBean bean1 = (DynaBean) results.get(0);
        assertEquals("apple", bean1.get("snack"));
        assertEquals(BigInteger.valueOf(5), bean1.get("number"));
        assertEquals(BigDecimal.valueOf(2.45), bean1.get("COST"));

        DynaBean bean2 = (DynaBean) results.get(1);
        assertEquals("pear", bean2.get("snack"));
        assertEquals(BigInteger.valueOf(2), bean2.get("number"));
        assertEquals(BigDecimal.valueOf(1.25), bean2.get("COST"));

        assertEquals(2, results.size());

        oddjob.destroy();
    }

    @Test
    public void testCopyWithMagicClass() throws ArooaPropertyException, ArooaConversionException {

        List<Fruit> in = Arrays.asList(
                new Fruit("apple", 5, 2.45),
                new Fruit("pear", 2, 1.25));

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/BeanCopyMagicClass.xml", getClass()
                .getClassLoader()));
        oddjob.setExport("iterable", new ArooaObject(in));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> results = lookup.lookup(
                "bus.to.to.beans", List.class);

        DynaBean bean1 = (DynaBean) results.get(0);
        assertEquals("apple", bean1.get("snack"));
        assertEquals(5, bean1.get("number"));
        assertEquals(2.45, bean1.get("COST"));

        DynaBean bean2 = (DynaBean) results.get(1);
        assertEquals("pear", bean2.get("snack"));
        assertEquals(2, bean2.get("number"));
        assertEquals(1.25, bean2.get("COST"));

        assertEquals(2, results.size());

        oddjob.destroy();
    }

    public static class BeanTo {

        private String snack;
        private int number;
        private double COST;

        public void setSnack(String snack) {
            this.snack = snack;
        }

        public void setNumber(int number) {
            this.number = number;
        }

        public void setCOST(double cOST) {
            COST = cOST;
        }
    }


    @Test
    public void testCopyWithJavaClass() throws ArooaPropertyException, ArooaConversionException {

        List<Fruit> in = Arrays.asList(
                new Fruit("apple", 5, 2.45),
                new Fruit("pear", 2, 1.25));

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/BeanCopyJavaClass.xml", getClass()
                .getClassLoader()));
        oddjob.setExport("iterable", new ArooaObject(in));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> results = lookup.lookup(
                "bus.to.to.beans", List.class);

        BeanTo bean1 = (BeanTo) results.get(0);
        assertEquals("apple", bean1.snack);
        assertEquals(5, bean1.number);
        assertEquals(2.45, bean1.COST, 0.01);

        BeanTo bean2 = (BeanTo) results.get(1);
        assertEquals("pear", bean2.snack);
        assertEquals(2, bean2.number);
        assertEquals(1.25, bean2.COST, 0.01);

        assertEquals(2, results.size());

        oddjob.destroy();
    }

    public static class Meal {

        private final Fruit fruit;

        public Meal(Fruit fruit) {
            this.fruit = fruit;
        }

        public Fruit getFruit() {
            return fruit;
        }

    }

    public static class Snack {
        private final BeanTo bean = new BeanTo();

        public BeanTo getBean() {
            return bean;
        }
    }

    @Test
    public void testCopyWithNestedBeans() throws ArooaPropertyException, ArooaConversionException {

        List<Meal> in = Arrays.asList(
                new Meal(new Fruit("apple", 5, 2.45)),
                new Meal(new Fruit("pear", 2, 1.25)));

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/BeanCopyNestedBeans.xml", getClass()
                .getClassLoader()));
        oddjob.setExport("list-of-beans", new ArooaObject(in));

        oddjob.run();

        assertEquals(ParentState.COMPLETE,
                oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<?> results = lookup.lookup(
                "results.beans", List.class);

        Snack snack1 = (Snack) results.get(0);

        BeanTo bean1 = snack1.getBean();

        assertEquals("apple", bean1.snack);
        assertEquals(5, bean1.number);
        assertEquals(2.45, bean1.COST, 0.01);

        Snack snack2 = (Snack) results.get(1);

        BeanTo bean2 = snack2.getBean();

        assertEquals("pear", bean2.snack);
        assertEquals(2, bean2.number);
        assertEquals(1.25, bean2.COST, 0.01);

        assertEquals(2, results.size());

        oddjob.destroy();
    }
}
