package org.oddjob.util;


import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ProgressTest {

    @Test
    public void percentagesOfBigValues() {

        Progress progress = new Progress(3_764_327_425L);

        assertThat(progress.toString(), is("0 bytes/3 GB (0%)"));

        progress.accept(846_992_341L);

        assertThat(progress.toString(), is("807 MB/3 GB (23%)"));

        progress.accept(3_764_327_425L);

        assertThat(progress.toString(), is("3 GB/3 GB (100%)"));
    }

}