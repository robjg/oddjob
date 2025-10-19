package org.oddjob.beanbus.destinations;

import org.junit.jupiter.api.Test;
import org.oddjob.Oddjob;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class BusCollectExamplesTest {

    @Test
    void collectToList() {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(Objects.requireNonNull(
                getClass().getResource("BusCollectDefaults.xml")).getFile()));

        ConsoleCapture capture = new ConsoleCapture();
        try (ConsoleCapture.Close ignored = capture.captureConsole()) {
            oddjob.run();
        }

        assertThat(OddjobTestHelper.getJobState(oddjob).isComplete(), is(true));

        assertThat(capture.getAsList(), contains("Index 2: Orange",
                "Size: 3",
                "As Text: [Apple, Orange, Pear]"));

        oddjob.destroy();
    }

    @Test
    void collectKeyMapper() {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(Objects.requireNonNull(
                getClass().getResource("BusCollectKeyMapper.xml")).getFile()));

        ConsoleCapture capture = new ConsoleCapture();
        try (ConsoleCapture.Close ignored = capture.captureConsole()) {
            oddjob.run();
        }

        assertThat(OddjobTestHelper.getJobState(oddjob).isComplete(), is(true));

        List<String> text = capture.getAsList();
        assertThat(text.get(0), is("Element 'O': Orange"));
        assertThat(text.get(1), is("Size: 3"));
        assertThat(text.get(2), containsString("A=Apple"));

        oddjob.destroy();
    }

    @Test
    void collectToValueMapper() {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(Objects.requireNonNull(
                getClass().getResource("BusCollectValueMapper.xml")).getFile()));

        ConsoleCapture capture = new ConsoleCapture();
        try (ConsoleCapture.Close ignored = capture.captureConsole()) {
            oddjob.run();
        }

        assertThat(OddjobTestHelper.getJobState(oddjob).isComplete(), is(true));

        List<String> text = capture.getAsList();
        assertThat(text.get(0), is("Element '2': "));
        assertThat(text.get(1), is("Element 2: 4.0"));
        assertThat(text.get(2), is("Size: 3"));
        assertThat(text.get(3), containsString("3=9.0"));

        oddjob.destroy();
    }

    @Test
    void collectToKeyValueMapper() {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(Objects.requireNonNull(
                getClass().getResource("BusCollectValueMapper.xml")).getFile()));

        ConsoleCapture capture = new ConsoleCapture();
        try (ConsoleCapture.Close ignored = capture.captureConsole()) {
            oddjob.run();
        }

        assertThat(OddjobTestHelper.getJobState(oddjob).isComplete(), is(true));

        assertThat(capture.getAsList(), contains("Element '2': ",
                "Element 2: 4.0",
                "Size: 3",
                "As Text: {1=1.0, 2=4.0, 3=9.0}"));

        oddjob.destroy();
    }

    @Test
    void collectToOutput() {

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(new File(Objects.requireNonNull(
                getClass().getResource("BusCollectToOutput.xml")).getFile()));

        ConsoleCapture capture = new ConsoleCapture();
        try (ConsoleCapture.Close ignored = capture.captureConsole()) {
            oddjob.run();
        }

        assertThat(OddjobTestHelper.getJobState(oddjob).isComplete(), is(true));

        assertThat(capture.getAsList(), contains("Apple", "Orange", "Pear", ""));

        oddjob.destroy();
    }
}

