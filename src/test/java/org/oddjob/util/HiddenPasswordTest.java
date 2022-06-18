package org.oddjob.util;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.oddjob.Describable;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.state.ParentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HiddenPasswordTest {

    private static final Logger logger = LoggerFactory.getLogger(HiddenPasswordTest.class);

    public static class JobWithPassword implements Runnable {

        private byte[] password;

        @Override
        public void run() {
            logger.info("Using password {}", password);
        }

        public byte[] getPassword() {
            return password;
        }

        @ArooaAttribute
        public void setPassword(byte[] password) {
            this.password = password;
        }
    }

    @Test
    public void testHiddenPassword() throws ArooaConversionException {

        File file = new File(Objects.requireNonNull(
                getClass().getResource("HiddenPasswordExample.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(file);

        oddjob.run();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.COMPLETE));

        OddjobLookup lookup = new OddjobLookup(oddjob);

        Describable describable = lookup.lookup("test", Describable.class);

        Map<String, String> described = describable.describe();
        assertThat(described.get("password"), Matchers.startsWith("[B"));
    }

}