package org.oddjob.remote;

import org.oddjob.arooa.utils.ClassUtils;

import java.util.Arrays;

/**
 * An {@link RemoteComponentException} for when the operation is unknown.
 */
public class NoSuchOperationException extends RemoteComponentException {
    static final long serialVersionUID = 2023091400L;

    private final String name;

    private final String[] signature;

    public NoSuchOperationException(long remoteId,
                                    String name,
                                    String[] signature,
                                    String message) {
        super(remoteId, message);
        this.name = name;
        this.signature = signature;
    }

    public static NoSuchOperationException of(long remoteId,
                                              OperationType<?> operationType) {

        return of(remoteId, operationType.getName(), operationType.getSignature());
    }

    public static NoSuchOperationException of(long remoteId,
                                              String name,
                                              Class<?>[] signature) {

        String[] classNames = ClassUtils.classesToStrings(signature);
        return of(remoteId, name, classNames);
    }

    public static NoSuchOperationException of(long remoteId,
                                              String name,
                                              String[] signature) {

        return new NoSuchOperationException(remoteId, name, signature,
                "No such operation " + name + "(" + Arrays.toString(signature) + ")");
    }

    public String getName() {
        return name;
    }

    public String[] getSignature() {
        return signature;
    }
}
