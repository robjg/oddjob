package org.oddjob.script;

import org.junit.jupiter.api.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ScriptExamplesTest {

    @Test
    void testInvokeScriptFunction() throws ArooaPropertyException, ArooaConversionException {

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
    void applyScriptFunctionAsJavaFunctionExample() throws ArooaPropertyException {

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

    @Test
    void extendRunnable() throws ArooaPropertyException {

        File file = new File(Objects.requireNonNull(
                getClass().getResource("ScriptExtendRunnable.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close ignored = console.captureConsole()) {

            oddjob.run();
        }

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        List<String> lines = console.getAsList();

        List<String> expected = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(
                        getClass().getResourceAsStream("ScriptExtendRunnableOut.txt"))))
                .lines().toList();

        assertThat(lines, is(expected));

        oddjob.destroy();
    }
}
