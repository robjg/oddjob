package org.oddjob.values.types;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.oddjob.arooa.logging.Appender;
import org.oddjob.arooa.logging.LoggerAdapter;
import org.oddjob.arooa.logging.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ExceptionListener;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

class ExceptionHandlersTest {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlersTest.class);

    @BeforeEach
    public void setUp(TestInfo testInfo) {
        // Ensure the logger is initialised before the tests.
        logger.info("-------------------  {}  --------------", testInfo.getDisplayName());
    }

    private static class Results implements Appender {

        List<Object> messages = new ArrayList<>();

        @Override
        public void append(LoggingEvent event) {
            messages.add(event.getMessage());
        }
    }

    String logName = ExceptionHandlers.class.getName();

    @Test
    void logInfo() {

        Results results = new Results();

        LoggerAdapter.appenderAdapterFor(logName)
                .addAppender(results, LoggerAdapter.layoutFor("%p: %m"));

        ExceptionHandlers test = new ExceptionHandlers();
        test.setHandler(ExceptionHandlers.Handler.LOG_INFO);

        ExceptionListener listener = test.toExceptionListener();

        listener.exceptionThrown(new RuntimeException("Test"));

        LoggerAdapter.appenderAdapterFor(logName).removeAppender(results);

        assertThat(results.messages, contains("INFO: java.lang.RuntimeException: Test"));
    }

}