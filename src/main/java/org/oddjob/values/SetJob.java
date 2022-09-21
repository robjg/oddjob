package org.oddjob.values;

import org.apache.commons.beanutils.expression.DefaultResolver;
import org.apache.commons.beanutils.expression.Resolver;
import org.oddjob.arooa.ArooaException;
import org.oddjob.arooa.ArooaValue;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.reflect.PropertyAccessor;
import org.oddjob.framework.extend.SimpleJob;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @oddjob.description A job which sets properties in other
 * jobs when it executes.
 * @oddjob.example Setting lots of properties.
 * <p>
 * {@oddjob.xml.resource org/oddjob/values/set-test.xml}
 * <p>
 * This is the configuration for one
 * of the tests.
 */

public class SetJob extends SimpleJob {


    private final Map<String, ArooaValue> values =
            new LinkedHashMap<>();

    /**
     * Add a name value pair.
     *
     * @param name  The name of the variable.
     * @param value The runtime configurable for the value.
     * @oddjob.property values
     * @oddjob.description The thing to set on the property that is given by
     * the key of this mapped property.
     * @oddjob.required No, but pointless if not provided.
     */
    public void setValues(String name, ArooaValue value) {
        values.put(name, value);
    }

    /*
     *  (non-Javadoc)
     * @see org.oddjob.jobs.AbstractJob#execute()
     */
    protected int execute() throws Exception {

        for (Map.Entry<String, ArooaValue> entry : values.entrySet()) {
            String name = entry.getKey();
            ArooaValue value = entry.getValue();
            logger().info("Setting [" + name + "] = [" + value + "]");
            setProperty(name, value);
        }

        return 0;
    }


    /**
     * Set a property on a Component in the registry. This method
     * will resolve the valueFor before setting the property. This might
     * not be the desired result for things like variables. The
     * way round this would be to create a dummy type ProxyType which
     * the DynaClass of VariablesJob, SetJob etc would return and which
     * valueFor methods would recognize and would return themselves as the
     * valueFor - but that's a lot of work and propbably isn't requied
     * much.
     * <p>
     * This method should really be a lot better at resolving the types of
     * nested beans for property conversion. This method only goes as far as
     * resolving past component id so the correct type of component is used.
     */
    private void setProperty(
            String property, ArooaValue value)
            throws ArooaPropertyException {

        Resolver resolver = new DefaultResolver();

        String compName = resolver.next(property);

        String propertyExpression = resolver.remove(property);

        if (propertyExpression == null) {
            throw new ArooaException("No thing specified to set property on.");
        }

        Object component = getArooaSession().getBeanRegistry(
        ).lookup(compName);

        if (component == null) {
            throw new ArooaException("No component [" +
                    compName + "]");
        }

        PropertyAccessor propertyAccessor =
                getArooaSession().getTools().getPropertyAccessor();

        propertyAccessor = propertyAccessor.accessorWithConversions(
                getArooaSession().getTools().getArooaConverter());

        propertyAccessor.setProperty(component, propertyExpression, value);
    }
}

