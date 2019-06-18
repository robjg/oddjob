package org.oddjob.arooa.runtime;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ScriptAttributeExamplesTest {

    private static final Logger logger = LoggerFactory.getLogger(
            ScriptAttributeExamplesTest.class);

    public static class SomeBean implements Runnable {

        private List<Integer> list = Arrays.asList(1,2,3,4,5);

        private List<Integer> out;

        @Override
        public void run() {
            logger.info("Result is " + out);
        }

        public List<Integer> getList() {
            return list;
        }

        @ArooaAttribute
        public void setOut(List<Integer> out) {
            this.out = out;
        }

        public List<Integer> getOut() {
            return out;
        }
    }

    @Test
    public void testPropertyAndMethods() {

        File config = new File(getClass().getResource("ScriptAttributesExamplesTest.xml").getFile());
        Oddjob oddjob = new Oddjob();
        oddjob.setFile(config);

        oddjob.run();

        assertThat(new OddjobLookup(oddjob).lookup("aBean.out"),
                is(Arrays.asList(1, 2, 3)));
    }
}
