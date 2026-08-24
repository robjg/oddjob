package org.oddjob.values.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ExceptionListener;
import java.util.Objects;

/**
 * @oddjob.description Provides a way to configure a Handler for Exceptions.
 * Current Handlers are:
 * <ul>
 *     <li>LOG_INFO: Log an exception as an informational message.</li>
 *     <li>LOG_WARN: Log an exception as a warning message.</li>
 *     <li>LOG_ERROR: Log an exception as an error message.</li>
 *     <li>LOG_FULL_ERROR: Log an exception as an error message with stack trace.</li>
 * </ul>
 */
public class ExceptionHandlers {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

    /**
     * @oddjob.description The name of the Exception Handler.
     * @oddjob.required Yes.
     */
    private Handler handler;

    /**
     * Encapsulate {@link ExceptionListener}s as an enum.
     */
    public enum Handler implements ExceptionListener {

        LOG_INFO() {
            @Override
            public void exceptionThrown(Exception e) {
                logger.info(e.toString());
            }
        },
        LOG_WARN() {
            @Override
            public void exceptionThrown(Exception e) {
                logger.warn(e.toString());
            }
        },
        LOG_ERROR() {
            @Override
            public void exceptionThrown(Exception e) {
                logger.error(e.toString());
            }
        },
        LOG_FULL_ERROR() {
            @Override
            public void exceptionThrown(Exception e) {
                logger.error(e.toString(), e);
            }
        },
        ;
    }

    public ExceptionListener toExceptionListener() {
        return Objects.requireNonNull(handler, "No Handler Specified.");
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[handler=" + handler + ']';
    }
}
