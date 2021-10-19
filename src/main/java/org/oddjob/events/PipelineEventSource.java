package org.oddjob.events;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.deploy.annotations.ArooaHidden;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.beanbus.Outbound;
import org.oddjob.beanbus.adapt.OutboundStrategies;

import java.util.Objects;

/**
 * Link a Pipeline {@link Outbound} as an {@link EventSource}.
 *
 * @param <T> The Pipeline Outbound type.
 */
public class PipelineEventSource<T> implements ValueFactory<EventSource<T>>, ArooaSessionAware {

    private ArooaSession session;

    private Object outbound;

    @ArooaHidden
    @Override
    public void setArooaSession(ArooaSession session) {
        this.session = session;
    }

    @Override
    public EventSource<T> toValue() throws ArooaConversionException {

        final Outbound<T> outbound = new OutboundStrategies()
                .outboundFor(Objects.requireNonNull(this.outbound, "No Outbound"),
                        session);

        if (outbound == null) {
            throw new NullPointerException("Can't derive outbound from " + this.outbound);
        }

        return consumer -> {
            outbound.setTo(consumer);
            return () -> outbound.setTo(null);
        };
    }

    public Object getOutbound() {
        return outbound;
    }

    public void setOutbound(Object outbound) {
        this.outbound = outbound;
    }

    @Override
    public String toString() {
        return "PipelineEventSource{" +
                "outbound=" + outbound +
                '}';
    }
}
