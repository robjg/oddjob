package org.oddjob.util;

/**
 * General Utilities.
 */
public class Etc {

    public static String toTabbedString(StackTraceElement[] elements) {

        StringBuilder builder = new StringBuilder();
        for(StackTraceElement element : elements) {
            builder.append('\t');
            builder.append(element);
            builder.append(System.lineSeparator());
        }
        return builder.toString();
    }
}
