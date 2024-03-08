package org.oddjob.jobs.structural;

import org.junit.jupiter.api.Test;
import org.oddjob.Oddjob;
import org.oddjob.state.ParentState;
import org.oddjob.structural.OddjobChildException;
import org.oddjob.tools.ConsoleCapture;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class SwitchJobTest {

    @Test
    void switchesExample() throws URISyntaxException {

        Path config = Paths.get(Objects.requireNonNull(
                getClass().getResource("SwitchJobSwitchesExample.xml")).toURI());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(config.toFile());

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();
        }

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        assertThat(console.getAsList(), is(List.of("Green")));

        oddjob.destroy();
    }

    @Test
    void whenDefault() throws URISyntaxException {

        Properties properties = new Properties();
        properties.setProperty("some.prop", "magenta");


        Path config = Paths.get(Objects.requireNonNull(
                getClass().getResource("SwitchJobSwitchesExample.xml")).toURI());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(config.toFile());
        oddjob.setProperties(properties);

        ConsoleCapture console = new ConsoleCapture();
        try (ConsoleCapture.Close close = console.captureConsole()) {

            oddjob.run();
        }

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        assertThat(console.getAsList(), is(List.of("Default for magenta")));

        oddjob.destroy();
    }

    @Test
    void whenNoDefault() throws URISyntaxException {

        Path config = Paths.get(Objects.requireNonNull(
                getClass().getResource("SwitchJobNoDefaultExample.xml")).toURI());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(config.toFile());

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.EXCEPTION));

        OddjobChildException oddjobChildException = (OddjobChildException) oddjob.lastStateEvent().getException();
        assertThat(oddjobChildException.getCause().getMessage(), containsString("No child job at index 1"));

        oddjob.destroy();
    }

}