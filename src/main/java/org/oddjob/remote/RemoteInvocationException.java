package org.oddjob.remote;

import org.oddjob.arooa.utils.ClassUtils;

import java.util.Arrays;

/**
 * An {@link RemoteComponentException} for when the operation is unknown.
 */
public class RemoteInvocationException extends RemoteComponentException {
    static final long serialVersionUID = 2023091400L;

    private final String name;

    private final String[] signature;

    public RemoteInvocationException(long remoteId,
                                     String name,
                                     String[] signature,
                                     String message,
                                     Throwable cause) {
        super(remoteId, message, cause);
        this.name = name;
        this.signature = signature;
    }

    public static RemoteInvocationException of(long remoteId,
                                               OperationType<?> operation,
                                               Object[] args,
                                               Throwable cause) {

        return RemoteInvocationException.of(remoteId, operation.getName(), operation.getSignature(),
                args, cause);
    }

    public static RemoteInvocationException of(long remoteId,
                                               String name,
                                               Class<?>[] signature,
                                               Throwable cause) {

        String[] classNames = ClassUtils.classesToStrings(signature);
        return RemoteInvocationException.of(remoteId, name, classNames, cause);
    }

    public static RemoteInvocationException of(long remoteId,
                                               String name,
                                               String[] signature,
                                               Throwable cause) {
        return new RemoteInvocationException(remoteId, name, signature,
                "Remote Id: " + remoteId + ", Failed to invoke " + name +
                        "(" + Arrays.toString(signature) + ")", cause);
    }

    public static RemoteInvocationException of(long remoteId,
                                               String name,
                                               Class<?>[] signature,
                                               Object[] args,
                                               Throwable cause) {
        String[] classNames = ClassUtils.classesToStrings(signature);
        return RemoteInvocationException.of(remoteId, name, classNames, args, cause);
    }

    public static RemoteInvocationException of(long remoteId,
                                               String name,
                                               String[] signature,
                                               Object[] args,
                                               Throwable cause) {
        return new RemoteInvocationException(remoteId, name, signature,
                "Failed to invoke " + name + ":" + Arrays.toString(signature) +
                        ",  args:" + Arrays.toString(args), cause);
    }

    public String getName() {
        return name;
    }

    public String[] getSignature() {
        return signature;
    }
}
