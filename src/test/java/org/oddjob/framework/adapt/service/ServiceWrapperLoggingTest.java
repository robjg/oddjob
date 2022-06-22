package org.oddjob.framework.adapt.service;

import org.junit.Test;
import org.oddjob.FailedToStopException;
import org.oddjob.OddjobComponentResolver;
import org.oddjob.Stoppable;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.arooa.standard.StandardArooaSession;
import org.oddjob.logging.appender.AppenderArchiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ServiceWrapperLoggingTest {

    private static final Logger logger =
            LoggerFactory.getLogger("AnyLogger");

    public static class SomeService {

        public void start() {
            logger.info("Starting " + getClass().getSimpleName());
        }

        public void stop() {
            logger.info("Stopping " + getClass().getSimpleName());
        }
    }


    @Test
    public void testStartStopLoggingCaptured() throws FailedToStopException {

        ArooaSession session = new StandardArooaSession();
        Object proxy = new OddjobComponentResolver()
                .resolve(new SomeService(), session);

        AppenderArchiver archiver = new AppenderArchiver(proxy, "%m%n");

        List<String> logLines = new ArrayList<>();

        archiver.addLogListener(
                message -> logLines.add(message.getMessage().trim()),
                proxy, LogLevel.DEBUG, -1, 0);

        ((Runnable) proxy).run();
        ((Stoppable) proxy).stop();

        logger.info("LogLines:\n" + logLines);

        assertThat(logLines.contains(
                "Starting " + SomeService.class.getSimpleName()),
                   is(true));

        assertThat(logLines.contains(
                "Stopping " + SomeService.class.getSimpleName()),
                   is(true));
    }

    public interface AnInterface {

        void doSomething();
    }

    public static class ServiceWithMethod implements AnInterface {

        public void start() {
        }

        public void stop() {
        }

        @Override
        public void doSomething() {
            logger.info("Doing something.");
        }
    }


    @Test
    public void testInterfaceLoggingCaptured() {

        ArooaSession session = new StandardArooaSession();
        Object proxy = new OddjobComponentResolver()
                .resolve(new ServiceWithMethod(), session);

        AppenderArchiver archiver = new AppenderArchiver(proxy, "%m%n");

        List<String> logLines = new ArrayList<>();

        archiver.addLogListener(
                message -> logLines.add(message.getMessage().trim()),
                proxy, LogLevel.DEBUG, -1, 0);

        ((AnInterface) proxy).doSomething();

        logger.info("LogLines:\n" + logLines);

        assertThat(logLines.size(),
                   is(1));

        assertThat(logLines.contains(
                "Doing something."),
                   is(true));
    }
}

