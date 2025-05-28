package org.oddjob.logging;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class AbstractLoggingOutputTest {

    @Test
    void lotsOfNewLines() throws IOException {

        List<String> results = new ArrayList<>(1_000_000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        AbstractLoggingOutput test = new AbstractLoggingOutput(out) {
            @Override
            protected void dispatch(String message) {
                results.add(message);
            }
        };

        for (int i = 0; i < 1_000_000; ++i) {

            test.write((Integer.valueOf(i).toString() + "\n").getBytes());
        }

        test.close();

        String[] lines = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(out.toByteArray())))
                .lines().toArray(String[]::new);

        assertThat(lines[lines.length - 1], is("999999"));
        assertThat(results.get(lines.length - 1), is("999999\n"));
    }

    @Test
    void lotsOfNewLinesAtOnce() throws IOException {

        ByteArrayOutputStream in = new ByteArrayOutputStream();
        for (int i = 0; i < 1_000_000; ++i) {

            in.write(Integer.valueOf(i).toString().getBytes());
            in.write('\n');
        }

        List<String> results = new ArrayList<>(1_000_000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        AbstractLoggingOutput test = new AbstractLoggingOutput(out) {
            @Override
            protected void dispatch(String message) {
                results.add(message);
            }
        };

        byte[] inArray = in.toByteArray();
        MatcherAssert.assertThat(inArray.length < Integer.MAX_VALUE, is(true));

        test.write(inArray);

        test.close();

        String[] lines = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(out.toByteArray())))
                .lines().toArray(String[]::new);

        assertThat(lines[lines.length - 1], is("999999"));
        assertThat(results.get(lines.length - 1), is("999999\n"));
    }

    @Test
    void stuffAfterNewLine() throws IOException {

        List<String> results = new ArrayList<>(1_000_000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        AbstractLoggingOutput test = new AbstractLoggingOutput(out) {
            @Override
            protected void dispatch(String message) {
                results.add(message);
            }
        };

        byte[] inArray = "Line One\nLine Two".getBytes();

        test.write(inArray);

        test.close();

        String[] lines = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(out.toByteArray())))
                .lines().toArray(String[]::new);

        assertThat(lines[lines.length - 1], is("Line Two"));
        assertThat(results.get(lines.length - 1), is("Line Two"));
    }

    @Test
    void emptyInput() throws IOException {

        List<String> results = new ArrayList<>(1_000_000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        AbstractLoggingOutput test = new AbstractLoggingOutput(out) {
            @Override
            protected void dispatch(String message) {
                results.add(message);
            }
        };

        byte[] inArray = "".getBytes();

        test.write(inArray);

        test.close();

        String[] lines = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(out.toByteArray())))
                .lines().toArray(String[]::new);

        assertThat(lines.length, is(0));
    }

    @Test
    void noneZeroInitialOffsetAfterNewLine() throws IOException {

        List<String> results = new ArrayList<>(1_000_000);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        AbstractLoggingOutput test = new AbstractLoggingOutput(out) {
            @Override
            protected void dispatch(String message) {
                results.add(message);
            }
        };

        byte[] inArray = "Line One\nLine Two\nLine Three".getBytes();

        test.write(inArray, 4, inArray.length - 4);

        test.close();

        String[] lines = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(out.toByteArray())))
                .lines().toArray(String[]::new);

        assertThat(lines[0], is(" One"));
        assertThat(results.get(0), is(" One\n"));

        assertThat(lines[lines.length - 1], is("Line Three"));
        assertThat(results.get(lines.length - 1), is("Line Three"));
    }

}