/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.script;

import org.junit.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.OjTestCase;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.values.VariablesJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

import static org.hamcrest.Matchers.is;

/**
 *
 */
public class ScriptJobTest extends OjTestCase {
    private static final Logger logger = LoggerFactory.getLogger(ScriptJobTest.class);

    @Test
    public void testHelloWorld() {

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration(
                "org/oddjob/script/ScriptHelloWorld.xml",
                getClass().getClassLoader()));
        oj.run();

        assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(oj));
    }

    @Test
    public void testVariableFromAndToJava() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration(
                "org/oddjob/script/VariablesFromAndToOddjob.xml",
                getClass().getClassLoader()));
        oj.run();

        assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(oj));

        String snack = new OddjobLookup(oj).lookup("e.text",
                String.class);

        assertEquals("apple", snack);
    }

    @Test
    public void testSettingOutput() {

        String xml =
                "<oddjob>" +
                        " <job>" +
                        "  <script id='s' language='JavaScript'>" +
                        "   <input>" +
                        "    <buffer>" +
// works in Groovy, not JavaScript.
//			"results.fruit = \"apple\"" +
                        "results.put('fruit', \"apple\");" +
                        "    </buffer>" +
                        "   </input>" +
                        "   <beans>" +
                        "    <bean key='results' class='java.util.HashMap'/>" +
                        "   </beans>" +
                        "  </script>" +
                        " </job>" +
                        "</oddjob>";

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration("XML", xml));
        oj.run();

        ScriptJob sc = (ScriptJob) new OddjobLookup(oj).lookup("s");
        Map<?, ?> results =
                (Map<?, ?>) sc.getBeans("results");

        assertEquals("apple", results.get("fruit"));
    }

    @Test
    public void testResult() {

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration(
                "org/oddjob/script/ScriptResult.xml",
                getClass().getClassLoader()));
        oj.run();

        assertEquals(ParentState.INCOMPLETE, oj.lastStateEvent().getState());
    }

    @Test
    public void testSettingVariables() throws Exception {

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration(
                "org/oddjob/script/ScriptSettingProperty.xml",
                getClass().getClassLoader()));
        oj.run();

        assertEquals(ParentState.COMPLETE, oj.lastStateEvent().getState());
        VariablesJob v = (VariablesJob) new OddjobLookup(oj).lookup("v");


        Object result = new DefaultConverter().convert(
                v.get("today"), Object.class);

        assertNotNull(result);
        assertEquals(Date.class, result.getClass());

        Object formatted = v.get("formattedToday");
        assertNotNull(formatted);
        logger.info("Formatted: " + formatted);
    }

	@Test
	public void whenFunctionsThenCallable() {

		ArooaSession arooaSession = new StandardArooaSession();

		ScriptJob scriptJob = new ScriptJob();
		scriptJob.setArooaSession(arooaSession);
		scriptJob.setScript(
				"function addTwo(x) { return new java.lang.Integer(x + 2)}\n" +
				"function multiplyByTwo(x) { return new java.lang.Integer(x * 2)}");
		scriptJob.run();

		assertThat(scriptJob.lastStateEvent().getState(), is(JobState.COMPLETE));

		Object result = scriptJob.getFunction("addTwo").apply(2);

		assertThat(result, is(4));

		Object result2 = scriptJob.getFunction("multiplyByTwo").apply(3);

		assertThat(result2, is(6));
	}

}
