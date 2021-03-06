package org.oddjob.state;

import org.junit.Test;
import org.oddjob.images.IconHelper;
import org.oddjob.images.StateIcons;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;

public class ParentStateTest {

    @Test
    public void testIconsForAllStates() {

        for (ParentState jobState : ParentState.values()) {
            assertThat(jobState.name(), StateIcons.iconFor(jobState), not(IconHelper.NULL));
        }
    }
}