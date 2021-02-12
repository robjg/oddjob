package org.oddjob.remote.things;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ConfigOperationInfoBuilderTest {

    @Test
    public void testBuildsWithSupportsEverything() {

        ConfigOperationInfo test = ConfigOperationInfo.builder()
                .withSupportsConfiguration(true)
                .withSupportsCopy(true)
                .withSupportsCut(true)
                .withSupportsPaste(true)
                .build();

        assertThat(test.isConfigurationSupported(), is(true));
        assertThat(test.isCopySupported(), is(true));
        assertThat(test.isCutSupported(), is(true));
        assertThat(test.isPasteSupported(), is(true));
    }

    @Test
    public void testBuildsWithSupportsSomethings() {

        ConfigOperationInfo test = ConfigOperationInfo.builder()
                .withSupportsConfiguration(true)
                .withSupportsCopy(false)
                .withSupportsCut(true)
                .withSupportsPaste(false)
                .build();

        assertThat(test.isConfigurationSupported(), is(true));
        assertThat(test.isCopySupported(), is(false));
        assertThat(test.isCutSupported(), is(true));
        assertThat(test.isPasteSupported(), is(false));
    }

    @Test
    public void testBuildsWithSupportsNothing() {

        ConfigOperationInfo test = ConfigOperationInfo.builder()
                .withSupportsConfiguration(false)
                .withSupportsCopy(false)
                .withSupportsCut(false)
                .withSupportsPaste(false)
                .build();

        assertThat(test.isConfigurationSupported(), is(false));
        assertThat(test.isCopySupported(), is(false));
        assertThat(test.isCutSupported(), is(false));
        assertThat(test.isPasteSupported(), is(false));
    }
}