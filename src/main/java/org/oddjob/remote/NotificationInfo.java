package org.oddjob.remote;

import java.util.Set;

/**
 * Information about Notifications available from an {@link RemoteNotifier}
 *
 * @see Notification
 */
public interface NotificationInfo {

    /**
     * Provide the notification types.
     *
     * @return The types. Maybe be empty but never null.
     */
    Set<String> getTypes();

    /**
     * Get the class of the data for the given type. Void should be used for
     * no data type.
     *
     * @param type The notification type.
     *
     * @return The class. Null of there is no notification of this type.
     */
    Class<?> getTypeOf(String type);

    /**
     * Get the optional description for a given notification.
     *
     * @param type The notification type.
     * @return Null if there is no type or no description.
     */
    String getDescription(String type);

}
