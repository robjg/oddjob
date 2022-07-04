package org.oddjob.util;

import org.apache.commons.io.FileUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.function.LongConsumer;

/**
 * Simple progress tracker.
 *
 * @see org.oddjob.arooa.utils.IoUtils#copy(InputStream, OutputStream, LongConsumer).
 */
public class Progress implements LongConsumer {

    private final long total;

    private volatile long count;

    public Progress(long total) {
        this.total = total;
    }

    @Override
    public void accept(long value) {
        this.count = value;
    }

    public long getTotal() {
        return total;
    }

    public long getCount() {
        return count;
    }

    /**
     * Allow property access to the text.
     *
     * @return The progress summary text.
     */
    public String getAsString() {
        return toString();
    }

    @Override
    public String toString() {
        String displayCount = FileUtils.byteCountToDisplaySize(count);
        if (total == 0) {
            return displayCount;
        }
        else {
            String displayTotal = FileUtils.byteCountToDisplaySize(total);
            String percentage = NumberFormat.getPercentInstance().format(
                    (double)count/total);
            return displayCount + "/" + displayTotal + " (" + percentage + ")";
        }
    }
}
