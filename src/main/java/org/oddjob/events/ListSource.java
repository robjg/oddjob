package org.oddjob.events;


import org.oddjob.Structural;
import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.deploy.annotations.ArooaComponent;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.state.StateEvent;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;
import org.oddjob.util.Restore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @oddjob.description An event source that aggregates a list of child event sources. The
 * events are aggregated according to the provided {@link EventOperator} which defaults to
 * {@link AllEvents}.
 *
 * @oddjob.example
 *
 * Trigger on a list of state expressions.
 *
 * {@oddjob.xml.resource org/oddjob/events/ListSourceExample.xml}
 *
 * @author Rob Gordon
 */
public class ListSource<T> extends EventServiceBase<CompositeEvent<T>>
implements Serializable, Structural {
	private static final long serialVersionUID = 2009031500L;

	/** Track changes to children an notify listeners. */
	protected transient volatile ChildHelper<Object> childHelper;

	/**
	 * @oddjob.property
	 * @oddjob.description Event Operator to filter events. ANY/ALL.
	 * @oddjob.required No, default to ALL.
	 */
	private volatile EventOperator<T> eventOperator;

	/**
	 * @oddjob.property
	 * @oddjob.description The last event to be passed to a consumer.
	 * @oddjob.required Read only.
	 */
    private volatile CompositeEvent<T> last;

	/**
	 * Constructor.
	 */
	public ListSource() {
		completeConstruction();
	}
	
	private void completeConstruction() {
		childHelper = new ChildHelper<>(this);
	}

    @Override
    protected Restore doStart(Consumer<? super CompositeEvent<T>> consumer) {

        EventOperator<T> eventOperator = Optional.ofNullable(this.eventOperator).orElse(new AllEvents<>());

        List<EventSource<?>> children = new ArrayList<>();
        for (Object child : this.childHelper) {
            children.add(EventSourceAdaptor.maybeEventSourceFrom(child, getArooaSession())
					.orElseThrow(() -> new IllegalStateException("Child [" +
							child + "] is not able to Event Source")));
        }

        return eventOperator.start(children,
                list -> {
                    last = list;
                    try {
                        save();
                    } catch (ComponentPersistException e) {
                        throw new RuntimeException(e);
                    }
                    consumer.accept(list);
                });
    }

	/**
	 * Add a listener. The listener will immediately receive add
	 * notifications for all existing children.
	 * 
	 * @param listener The listener.
	 */	
	public void addStructuralListener(StructuralListener listener) {
		childHelper.addStructuralListener(listener);
	}
	
	/**
	 * Remove a listener.
	 * 
	 * @param listener The listener.
	 */
	public void removeStructuralListener(StructuralListener listener) {
		childHelper.removeStructuralListener(listener);
	}

	@Override
	protected void onSoftReset() {
		childHelper.softResetChildren();
	}

	@Override
	protected void onHardReset() {
		childHelper.hardResetChildren();
		this.last = null;
	}

	/**
	 * @oddjob.property
	 * @oddjob.description The child event sources.
	 * @oddjob.required No, but pointless without.
	 */
	@ArooaComponent
	public void setChild(int index, Object child) {
	    childHelper.insertOrRemoveChild(index, child);
    }

    public EventOperator<T> getEventOperator() {
        return eventOperator;
    }

    @ArooaAttribute
    public void setEventOperator(EventOperator<T> eventOperator) {
        this.eventOperator = eventOperator;
    }

    public CompositeEvent<T> getLast() {
	    return last;
    }

    /**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
		s.writeObject(getName());
		if (loggerName().startsWith(getClass().getName())) {
			s.writeObject(null);
		}
		else {
			s.writeObject(loggerName());
		}
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		String name = (String) s.readObject();
		logger((String) s.readObject());
		StateEvent.SerializableNoSource savedEvent = 
				(StateEvent.SerializableNoSource) s.readObject();
		
		completeConstruction();
		
		setName(name);
	}
}
