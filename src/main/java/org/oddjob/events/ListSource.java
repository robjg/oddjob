package org.oddjob.events;


import org.oddjob.Structural;
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
 * @oddjob.description A list of events.
 * 
 * @author Rob Gordon
 */
public class ListSource<E> extends EventSourceBase<List<E>>
implements Serializable, Structural {
	private static final long serialVersionUID = 2009031500L;


	/** Track changes to children an notify listeners. */
	protected transient volatile ChildHelper<EventSource< E > > childHelper;

	private EventOperator<E> eventOperator;

    private List<E> previous;

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
    protected Restore doStart(Consumer<? super List<E>> consumer) throws Exception {

        EventOperator<E> eventOperator = Optional.ofNullable(this.eventOperator).orElse(new AllEvents<>());

        List<EventSource<E>> children = new ArrayList<>();
        for (EventSource<E> child : this.childHelper) {
            children.add(child);
        }

        return eventOperator.start(previous, children,
                list -> {
                    previous = list;
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
		this.previous = null;
	}

	public void setChild(int index, EventSource< E > child) {
	    childHelper.insertChild(index, child);
    }

    public EventOperator<E> getEventOperator() {
        return eventOperator;
    }

    public void setEventOperator(EventOperator<E> eventOperator) {
        this.eventOperator = eventOperator;
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
