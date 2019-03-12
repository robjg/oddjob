package org.oddjob.io;

import org.oddjob.events.EventOf;
import org.oddjob.util.Restore;

import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * Watch for a single file to exist or modification.
 */
public interface FileWatch {

    /**
     * Subscribe to notifications for the existence or modifications in the file.
     * <p/>
     * If the file exists already the consumer will be notified before this method returns. If the file is created
     * during the call to this method then the consumer may receive a notification for the same file twice. A
     * consumer could use the modified date to remove duplicated but then may miss file modifications that happen
     * close together. The documentation for implementations should adequately explain such behaviours.
     * <p/>
     * @param path The path of the file to watch. Must not be null.
     * @param consumer
     *
     * @return Something that will close resources.
     */
    Restore subscribe(Path path, Consumer<? super EventOf<Path>> consumer);

}
