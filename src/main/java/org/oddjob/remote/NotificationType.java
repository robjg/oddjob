package org.oddjob.remote;

import java.io.Serializable;
import java.util.Objects;

/**
 * Define a type of remote notification.
 *
 * @param <T> The data type of the notification.
 */
public class NotificationType<T> implements Serializable {

    private static final long serialVersionUID = 2020070500L;

    private final String name;

    private final Class<T> dataType;

    public NotificationType(String name, Class<T> dataType) {
        this.name = Objects.requireNonNull(name);
        this.dataType = Objects.requireNonNull(dataType);
    }

    public  String getName() {
        return name;
    }

    public Class<T> getDataType() {
        return dataType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationType<?> that = (NotificationType<?>) o;
        return name.equals(that.name) &&
                dataType.equals(that.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, dataType);
    }

    @Override
    public String toString() {
        return "NotificationType{" +
                "name='" + name + '\'' +
                ", dataType=" + dataType +
                '}';
    }

    public static Builder ofName(String name) {
        return new Builder(name);
    }

    public static class Builder {

        private final String name;

        public Builder(String name) {
            this.name = name;
        }

        public <T> NotificationType<T> andDataType(Class<T> dataType) {
            return new NotificationType<>(name,
                    dataType);
        }

        public NotificationType<Void> andNoData() {
            return andDataType(void.class);
        }
    }

}
