package org.oddjob.remote;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Define a remote operation.
 *
 * @param <T> The return type of the remote operation.
 */
public class OperationType<T> {

    private final String name;

    private final Class<?>[] signature;

    private final Class<T> returnType;

    public OperationType(String name, Class<?>[] signature, Class<T> returnType) {
        this.name = Objects.requireNonNull(name);
        this.signature = Objects.requireNonNull(signature);
        this.returnType = Objects.requireNonNull(returnType);
    }

    public  String getName() {
        return name;
    }

    public Class<?>[] getSignature() {
        return signature;
    }

    public Class<T> getReturnType() {
        return returnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationType<?> that = (OperationType<?>) o;
        return name.equals(that.name) &&
                Arrays.equals(signature, that.signature) &&
                returnType.equals(that.returnType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(name, returnType);
        result = 31 * result + Arrays.hashCode(signature);
        return result;
    }

    @Override
    public String toString() {
        return "OperationType{" +
                "name='" + name + '\'' +
                ", signature=" + Arrays.toString(signature) +
                ", returnType=" + returnType +
                '}';
    }

    public static Builder ofName(String name) {
        return new Builder(name);
    }

    public static class Builder {

        private final String name;

        private Class<?>[] signature;

        public Builder(String name) {
            this.name = name;
        }

        public Builder withSignature(Class<?>... signature) {
            this.signature = signature;
            return this;
        }

        public <T>  OperationType<T> returning(Class<T> returnType) {
            return new OperationType<>(name,
                    Optional.ofNullable(signature).orElse(new Class<?>[0]),
                    returnType);
        }

        public  OperationType<Void> returningVoid() {
            return returning(void.class);
        }
    }

}
