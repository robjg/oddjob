package org.oddjob.jmx.client;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ClassResolver;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.types.ValueFactory;

/**
 * Arooa configuration bean for creating a {@link DirectInvocationClientFactory}.
 */
public class DirectInvocationBean implements ValueFactory<DirectInvocationClientFactory<?>>, ArooaSessionAware {

    private String className;

    private ArooaSession session;

    @Override
    public void setArooaSession(ArooaSession session) {
        this.session = session;
    }

    @Override
    public DirectInvocationClientFactory<?> toValue() {

        ClassResolver resolver = session.getArooaDescriptor().getClassResolver();

        Class<?> cl = resolver.findClass(className);

        if (cl == null) {
            return null;
        }
        else {
            return new DirectInvocationClientFactory<>(cl);
        }
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String toString() {
        return "DirectInvocationBean{" +
                "className='" + className + '\'' +
                '}';
    }
}
