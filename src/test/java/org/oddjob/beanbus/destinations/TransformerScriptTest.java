package org.oddjob.beanbus.destinations;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.Resettable;
import org.oddjob.arooa.beanutils.BeanUtilsPropertyAccessor;
import org.oddjob.arooa.beanutils.MagicBeanClassCreator;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaClass;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.beanbus.example.Fruit;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

public class TransformerScriptTest extends OjTestCase {

    @Test
    public void testSimpleMagicBeanTransform() throws ScriptException {

        MagicBeanClassCreator creator = new MagicBeanClassCreator("Test");
        creator.addProperty("fruit", String.class);
        creator.addProperty("quantity", Integer.class);
        ArooaClass arooaClass = creator.create();

        PropertyAccessor accessor = new BeanUtilsPropertyAccessor();

        Object bean1 = arooaClass.newInstance();
        accessor.setProperty(bean1, "fruit", "apple");
        accessor.setProperty(bean1, "quantity", 42);

        Object bean2 = arooaClass.newInstance();
        accessor.setProperty(bean2, "fruit", "orange");
        accessor.setProperty(bean2, "quantity", 2);

        TransformerScript<Object, Object> test =
                new TransformerScript<>();

        test.setScript("function apply(from) {" +
                " if (from.get('quantity') > 24) {" +
                "  return null;" +
                " }" +
                " else {" +
                "  return from" +
                " }}");

        List<Object> results = new ArrayList<>();

        test.setTo(results::add);
        test.configured();

        test.accept(bean1);
        test.accept(bean2);

        assertEquals(1, results.size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExample() throws ArooaPropertyException, ArooaConversionException, InterruptedException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/beanbus/destinations/TransformerScriptExample.xml", getClass()
                .getClassLoader()));

        StateSteps states = new StateSteps(oddjob);
        states.startCheck(ParentState.READY,
                ParentState.EXECUTING,
                ParentState.COMPLETE);

        oddjob.run();

        states.checkNow();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        List<Fruit> results = lookup.lookup(
                "results.beans", List.class);

        assertEquals("Banana", results.get(0).getType());
        assertEquals("Pear", results.get(1).getType());

        assertEquals(2, results.size());

        Object beanBus = lookup.lookup("bean-bus");

        ((Resettable) beanBus).hardReset();
        ((Runnable) beanBus).run();

        results = lookup.lookup(
                "results.beans", List.class);

        assertEquals("Banana", results.get(0).getType());
        assertEquals("Pear", results.get(1).getType());

        assertEquals(2, results.size());

        oddjob.destroy();
    }
}
