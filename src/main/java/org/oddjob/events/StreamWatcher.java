package org.oddjob.events;

import org.oddjob.util.Restore;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @oddjob.description Watch a stream for an instance of a string. Typically this watcher (and its trigger)
 * will be started before a job that produces some output that needs to be monitored.
 * <p>
 *     A typeical use case would be running an exec job that tails a log file, and this watcher is looking
 *     for the word "Error" or some such.
 * </p>
 */
public class StreamWatcher extends InstantEventSourceBase<String> {

    /**
     * @oddjob.property
     * @oddjob.description The text to watch for.
     * @oddjob.required Yes.
     */
    private String watch;

    /**
     * @oddjob.property
     * @oddjob.description The output stream to be provided to something who's output need watching.
     */
    private OutputStream out;

    @Override
    protected Restore doStart(Consumer<? super InstantEvent<String>> consumer) throws Exception {

         AtomicReference<Consumer<? super InstantEvent<String>>> consumerRef =
                 new AtomicReference<>(consumer);


        final byte[] watch = Optional.ofNullable(this.watch)
                .map(String::getBytes)
                .orElseThrow(() -> new IllegalArgumentException("Nothing to watch"));

        logger().info("Starting to watch [{}]", new String(watch));

        this.out = new OutputStream() {

            int index = 0;
            @Override
            public void write(int b) throws IOException {
                if ( b != watch[index++]) {
                    index = 0;
                }

                if ( index == watch.length) {
                    index = 0;
                    Optional.ofNullable(consumerRef.get())
                            .ifPresent(c -> c.accept(InstantEvent.of(new String(watch))));
                }
            }

            @Override
            public String toString() {
                return "StreamWatcherOutputStream";
            }
        };

        return () -> {
            consumerRef.set(null);
            logger().info("Closed watcher.");
        };
    }


    public String getWatch() {
        return watch;
    }

    public void setWatch(String watch) {
        this.watch = watch;
    }

    public OutputStream getOut() {
        return out;
    }
}
