package org.oddjob.script;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.state.ParentState;

import java.io.File;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ScriptExamplesTest {

    @Test
    public void testInvokeScriptFunction() throws ArooaPropertyException, ArooaConversionException {

        File file = new File(Objects.requireNonNull(
                getClass().getResource("InvokeScriptFunction.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        Properties props = new OddjobLookup(oddjob).lookup(
                "properties.properties", Properties.class);

        assertThat(props.getProperty("text.after"), is("Apples"));

        oddjob.destroy();
    }

    @Test
    public void applyScriptFunctionAsJavaFunctionExample() throws ArooaPropertyException {

        File file = new File(Objects.requireNonNull(
                getClass().getResource("ScriptFunctions.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        assertThat(lookup.lookup("add.text"), is("7"));
        assertThat(lookup.lookup("multiply.text"), is("6"));

        oddjob.destroy();
    }
}
