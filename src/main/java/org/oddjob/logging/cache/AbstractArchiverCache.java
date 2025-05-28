/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.logging.cache;

import org.oddjob.Stateful;
import org.oddjob.arooa.logging.LogLevel;
import org.oddjob.logging.ArchiveNameResolver;
import org.oddjob.logging.LogArchive;
import org.oddjob.logging.LogArchiver;
import org.oddjob.logging.LogListener;
import org.oddjob.state.StateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A base implementation of a Cache of Log Messages.
 * <p>
 *
 * @author Rob Gordon
 */
abstract public class AbstractArchiverCache implements LogArchiverCache {
    private static final Logger logger = LoggerFactory.getLogger(AbstractArchiverCache.class);

    /**
     * Map of archive name to archive. Should this be concurrent?
     */
    private final Map<String, LogArchiveImpl> archives =
            new HashMap<>();

    /**
     * Keep track of children and archives so we can delete an archive.
     */
    private final SimpleCounter counter = new SimpleCounter();

    private final int maxHistory;

    private final ArchiveNameResolver resolver;

    private final StateListener stateListener = event -> {
        if (event.getState().isDestroyed()) {
            removeArchive(event.getSource());
        }
    };

    /**
     * Default constructor.
     */
    public AbstractArchiverCache(ArchiveNameResolver resolver) {
        this(resolver, LogArchiver.MAX_HISTORY);
    }

    /**
     * Construct a LogArchiver with the given amount of history.
     *
     * @param maxHistory The number of lines to store for each logger
     */
    public AbstractArchiverCache(ArchiveNameResolver resolver, int maxHistory) {
        this.resolver = resolver;
        this.maxHistory = maxHistory;
    }

    /*
     *  (non-Javadoc)
     * @see org.oddjob.logging.FunctionalLogArchiver#getMaxHistory()
     */
    @Override
    public int getMaxHistory() {
        return maxHistory;
    }

    /*
     *  (non-Javadoc)
     * @see org.oddjob.logging.FunctionalLogArchiver#getLastMessageNumber(java.lang.String)
     */
    @Override
    public long getLastMessageNumber(String archive) {
        LogArchive logArchive = archives.get(archive);
        if (logArchive == null) {
            throw new IllegalArgumentException("Archive [" + archive + "] does not exist in this LogArchiver.");
        }
        return logArchive.getLastMessageNumber();
    }

    /**
     * Add a listener.
     *
     * @param l         The listener
     * @param component The component
     * @param level     The level
     * @param last      The last message number.
     * @param history   The max messages required.
     */
    @Override
    public void addLogListener(LogListener l, Object component,
                               LogLevel level, long last, int history) {
        String archive = resolver.resolveName(component);
        LogArchive logArchive = archives.get(archive);
        if (logArchive == null) {
            l.logEvent(LogArchiver.NO_LOG_AVAILABLE);
            return;
        }
        logger.debug("Adding LogListener [{}] for [{}]", l, logArchive.getArchive());
        logArchive.addListener(l, level, last, history);
    }

    /**
     * Remove a listener.
     *
     * @param l         The listener.
     * @param component The component the archive is for.
     */
    @Override
    public void removeLogListener(LogListener l, Object component) {
        String archive = resolver.resolveName(component);
        LogArchive logArchive = archives.get(archive);
        if (logArchive == null) {
            return;
        }
        logger.debug("Removing LogListener [{}] from [{}]", l, logArchive.getArchive());
        logArchive.removeListener(l);
    }

    /**
     * Does this Archiver contain the given archive.
     *
     * @param archive The archive.
     * @return true if it does, false if it doesn't.
     */
    @Override
    public boolean hasArchive(String archive) {
        return archives.containsKey(archive);
    }

    protected boolean hasArchiveFor(Object component) {
        return hasArchive(resolver.resolveName(component));
    }

    protected ArchiveNameResolver getResolver() {
        return resolver;
    }

    /**
     * Add an archive to this Log Archiver.
     *
     * @param component The component to add and archive for.
     */
    protected void addArchive(Object component) {

        final String archiveName = resolver.resolveName(component);
        if (archiveName == null) {
            return;
        }

        logger.debug("Adding archive [{}] for [{}]", archiveName, component);

        counter.add(archiveName, () -> {
            LogArchiveImpl logArchive = new LogArchiveImpl(archiveName, getMaxHistory());
            archives.put(archiveName, logArchive);
            logger.debug("Adding archive for [{}]", archiveName);
        });

        if (component instanceof Stateful) {
            ((Stateful) component).addStateListener(stateListener);
        }
    }

    /**
     * Remove an archive from this LogArchive.
     *
     * @param component The component that the archive was added for.
     */
    protected void removeArchive(Object component) {
        final String archiveName = resolver.resolveName(component);
        if (archiveName == null) {
            return;
        }

        logger.debug("Removing log archive [{}] for [{}]", archiveName, component);

        counter.remove(archiveName, () -> {
            logger.debug("Deleting log archive [{}]", archiveName);
            archives.remove(archiveName);
        });
    }

    /**
     * Add an event to the cache.
     *
     * @param archive The archive.
     * @param level   The level.
     * @param message The message.
     */
    @Override
    public void addEvent(String archive, LogLevel level, String message) {
        LogArchiveImpl logArchive = archives.get(archive);
        if (logArchive == null) {
            throw new IllegalArgumentException("Archive [" + archive + "] does not exist in this LogArchiver.");
        }
        logArchive.addEvent(level, message);
    }

    public abstract void destroy();
}
