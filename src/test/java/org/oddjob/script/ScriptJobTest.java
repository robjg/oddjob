/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.script;

import org.junit.jupiter.api.Test;
import org.oddjob.Oddjob;
import org.oddjob.OddjobLookup;
import org.oddjob.Resettable;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.convert.DefaultConverter;
import org.oddjob.arooa.parsing.DragPoint;
import org.oddjob.arooa.parsing.DragTransaction;
import org.oddjob.arooa.reflect.ArooaPropertyException;
import org.oddjob.arooa.registry.ChangeHow;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.arooa.xml.XMLConfiguration;
import org.oddjob.state.JobState;
import org.oddjob.state.ParentState;
import org.oddjob.tools.ConsoleCapture;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.values.VariablesJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 *
 */
public class ScriptJobTest {
    private static final Logger logger = LoggerFactory.getLogger(ScriptJobTest.class);

    @Test
    public void testHelloWorld() throws ArooaConversionException {

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration(
                "org/oddjob/script/ScriptHelloWorld.xml",
                getClass().getClassLoader()));

        oj.load();
        ScriptJob scriptJob = new OddjobLookup(oj)
                .lookup("script", ScriptJob.class);

        ConsoleCapture capture = new ConsoleCapture();
        try (ConsoleCapture.Close ignored = capture.capture(scriptJob.consoleLog()) ){
            oj.run();
        }

        assertThat(OddjobTestHelper.getJobState(oj).isComplete(), is(true));

        assertThat(capture.getAsList(), contains("Hello, World!"));
    }

    @Test
    public void testVariableFromAndToJava() throws ArooaPropertyException, ArooaConversionException {

        Oddjob oj = new Oddjob();
        oj.setConfiguration(new XMLConfiguration(
                "org/oddjob/script/VariablesFromAndToOddjob.xml",
                getClass().getClassLoader()));
        oj.run();

        assertEquals(ParentState.COMPLETE, OddjobTestHelper.getJobState(oj));

        String echoText = new OddjobLookup(oj).lookup("echo.text",
                String.class);

        assertThat(echoText, is("snack=Apple\nfruit=Apple"));
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
                (Map<?, ?>) sc.getBind("results");

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
        logger.info("Formatted: {}", formatted);
    }

    @Test
    public void useSessionBindings() throws ArooaConversionException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration(
                "org/oddjob/script/ScriptUseSessionBindings.xml",
                getClass().getClassLoader()));
        oddjob.run();

        assertEquals(ParentState.COMPLETE, oddjob.lastStateEvent().getState());

        OddjobLookup lookup = new OddjobLookup(oddjob);

        assertThat(lookup.lookup("echoSnack.text"),
                is("${snack} is 'Apple' and #{snack} is 'Apple'"));
        assertThat(lookup.lookup("echoSum.text"),
                is("#add(2, 3) is 5.0"));

        lookup.lookup("script", Resettable.class).hardReset();

        lookup.lookup("echoSnack", Resettable.class).hardReset();
        lookup.lookup("echoSnack", Runnable.class).run();

        assertThat(lookup.lookup("echoSnack.text"),
                is("${snack} is '' and #{snack} is 'undefined'"));

        oddjob.destroy();
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

    @Test
    public void whenFunctionsWithSeveralArgs() {

        ArooaSession arooaSession = new StandardArooaSession();

        ScriptJob scriptJob = new ScriptJob();
        scriptJob.setArooaSession(arooaSession);
        scriptJob.setScript(
                "function add(x, y) { return x + y }\n" +
                        "function multiply(x, y) { return x * y }");
        scriptJob.run();

        assertThat(scriptJob.lastStateEvent().getState(), is(JobState.COMPLETE));

        Object result = scriptJob.getFunction("add").apply(new Object[] { 2, 3 } );

        assertThat(result, is(5.0));

        Object result2 = scriptJob.getFunction("multiply").apply(new Object[] { 2, 3 } );

        assertThat(result2, is(6.0));
    }

    String xml =
            "<oddjob id='oddjob'>" +
                    " <job>" +
                    "  <script id='script'>var ourNum = 3" +
                    "    <export><value key='ourNum' value='.'/></export>" +
                    "  </script>" +
                    " </job>" +
                    "</oddjob>";

    String replacement = "<script id='script'>var ourNum = 4" +
            "<export><value key='ourNum' value='.'/></export>" +
            "</script>";


    @Test
    void testCutAndPaste() throws ArooaParseException {

        Oddjob oddjob = new Oddjob();
        oddjob.setConfiguration(new XMLConfiguration("XML", xml));

        oddjob.run();

        OddjobLookup lookup = new OddjobLookup(oddjob);

        DragPoint script = oddjob.provideConfigurationSession().dragPointFor(
                lookup.lookup("script"));

        assertThat(lookup.lookup("ourNum"), is(3));

        DragTransaction trn = script.beginChange(ChangeHow.FRESH);
        script.cut();
        trn.commit();

        assertThat(lookup.lookup("ourNum"), is(nullValue()));

        DragPoint paste = oddjob.provideConfigurationSession().dragPointFor(
                new OddjobLookup(oddjob).lookup("oddjob"));

        DragTransaction trn2 = paste.beginChange(ChangeHow.FRESH);
        paste.paste(0, replacement);
        trn2.commit();

        oddjob.run();

        assertThat(lookup.lookup("ourNum"), is(4));

        oddjob.destroy();
    }

}
