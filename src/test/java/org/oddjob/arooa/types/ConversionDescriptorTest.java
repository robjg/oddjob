package org.oddjob.arooa.types;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.state.ParentState;

import java.io.File;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ConversionDescriptorTest {

    @Test
    public void testConversionDescriptorExample() {

        File file = new File(Objects.requireNonNull(getClass()
                .getResource("ReflectionConversionMain.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        assertThat(new OddjobLookup(oddjob).lookup("example/thing.myGremlin.name"),
                is("Gizmo"));

        oddjob.destroy();
    }
}
