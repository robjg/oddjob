package org.oddjob.events.example;

import org.junit.Test;
import org.oddjob.*;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.state.ParentState;
import org.oddjob.tools.StateSteps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PricingWhenExampleTest {

    private static final Logger logger = LoggerFactory.getLogger(PricingWhenExampleTest.class);

    @Test
    public void testAllFilesExists() throws InterruptedException, FailedToStopException {

        File configFile = new File(Objects.requireNonNull(
                getClass().getResource("PricingWhenExample.xml")).getFile());

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(configFile);

        logger.info("** Loading.");

        oddjob.load();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.READY));

        StateSteps oddjobState = new StateSteps(oddjob);
        oddjobState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE, ParentState.STARTED);

        logger.info("** Starting.");

        oddjob.run();

        oddjobState.checkWait();

        logger.info("** Stopping.");

        oddjob.stop();

        oddjob.destroy();
    }

    @Test
    public void testWithReplay() throws InterruptedException, FailedToStopException, IOException, ArooaConversionException {

        Path workDir = OurDirs.workPathDir(getClass().getSimpleName(), true);

        File configFile = new File(Objects.requireNonNull(
                getClass().getResource("PricingWhenExample.xml")).getFile());
        File replayFile = new File(Objects.requireNonNull(
                getClass().getResource("DataReplay.xml")).getFile());
        File dataDir = new File(workDir.toFile(), "data");

        Properties properties = new Properties();
        properties.setProperty("data.dir", dataDir.getAbsolutePath());

        logger.info("** Running Setup.");

        Oddjob replay = new Oddjob();
        replay.setFile(replayFile);
        replay.setProperties(properties);

        replay.run();

        OddjobLookup replayLookup = new OddjobLookup(replay);

        logger.info("** Loading Example.");

        Oddjob oddjob = new Oddjob();
        oddjob.setFile(configFile);
        oddjob.setProperties(properties);

        oddjob.load();

        assertThat(oddjob.lastStateEvent().getState(), is(ParentState.READY));

        OddjobLookup oddjobLookup = new OddjobLookup(oddjob);

        logger.info("** Starting Checks.");

        StateSteps oddjobState = new StateSteps(oddjob);
        oddjobState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE);

        StateSteps whenBookListState = new StateSteps(
                oddjobLookup.lookup("whenBookList", Stateful.class));

        whenBookListState.startCheck(ParentState.READY, ParentState.EXECUTING,
                ParentState.ACTIVE);

        StateSteps forEachBookState = new StateSteps(
                oddjobLookup.lookup("forEachBook", Stateful.class));

        forEachBookState.startCheck(ParentState.READY);

        logger.info("** Starting Oddjob.");

        oddjob.run();

        forEachBookState.checkNow();
        whenBookListState.checkNow();
        oddjobState.checkNow();

        logger.info("** Copying Apples and Pears.");

        Runnable copyApples = replayLookup.lookup("copyAPPLES", Runnable.class);
        copyApples.run();

        Runnable copyPears = replayLookup.lookup("copyPEARS", Runnable.class);
        copyPears.run();

        logger.info("** Copying Rod and Jane.");

        Runnable copyRod = replayLookup.lookup("copyROD", Runnable.class);
        copyRod.run();

        Runnable copyJane = replayLookup.lookup("copyJANE", Runnable.class);
        copyJane.run();

        logger.info("** Copying BookList.");

        whenBookListState.startCheck(
                ParentState.ACTIVE);
        forEachBookState.startCheck(
                ParentState.READY, ParentState.EXECUTING, ParentState.ACTIVE);

        Runnable copyBookList = replayLookup.lookup("copyBookList", Runnable.class);
        copyBookList.run();

        forEachBookState.checkWait();
        whenBookListState.checkNow();

        StateSteps rodWhenCheck = new StateSteps(
                oddjobLookup.lookup("forEachBook/ROD/whenBook", Stateful.class));
        rodWhenCheck.startCheck(StateSteps.maybe(ParentState.READY),
                StateSteps.maybe(ParentState.ACTIVE),
                        StateSteps.definitely((ParentState.STARTED)));

        rodWhenCheck.checkWait();

        assertThat( oddjobLookup.lookup("forEachBook/ROD/calculate.value", Double.class),
                is( 311.7));

        StateSteps janeWhenCheck = new StateSteps(
                oddjobLookup.lookup("forEachBook/JANE/whenBook", Stateful.class));
        janeWhenCheck.startCheck(StateSteps.maybe(ParentState.READY),
                StateSteps.definitely(ParentState.ACTIVE));

        janeWhenCheck.checkWait();

        janeWhenCheck.startCheck(StateSteps.definitely(ParentState.ACTIVE),
                StateSteps.definitely((ParentState.STARTED)));

        Runnable copyOranges = replayLookup.lookup("copyORANGES", Runnable.class);
        copyOranges.run();

        janeWhenCheck.checkWait();

        StateSteps freddyWhenCheck = new StateSteps(
                oddjobLookup.lookup("forEachBook/FREDDY/whenBook", Stateful.class));
        freddyWhenCheck.startCheck(StateSteps.maybe(ParentState.READY),
                StateSteps.definitely(ParentState.ACTIVE));

        freddyWhenCheck.checkWait();

        freddyWhenCheck.startCheck(StateSteps.definitely(ParentState.ACTIVE),
                StateSteps.definitely((ParentState.STARTED)));

        Runnable copyFreddy = replayLookup.lookup("copyFREDDY", Runnable.class);
        copyFreddy.run();

        freddyWhenCheck.checkWait();

        logger.info("** Stopping.");

        oddjob.stop();

        oddjob.destroy();
        replay.destroy();
    }
}
