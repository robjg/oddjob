package org.oddjob.persist;

import org.oddjob.Describable;
import org.oddjob.Iconic;
import org.oddjob.Stateful;
import org.oddjob.Structural;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.describe.UniversalDescriber;
import org.oddjob.images.IconEvent;
import org.oddjob.images.IconListener;
import org.oddjob.images.ImageData;
import org.oddjob.state.StateDetail;
import org.oddjob.state.StateEvent;
import org.oddjob.state.StateListener;
import org.oddjob.structural.StructuralEvent;
import org.oddjob.structural.StructuralListener;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Capture as much serializable information about a job 
 * hierarchy as possible. This serialized form of the hierarchy
 * is known as its silhouette.
 * 
 * @author rob
 *
 */
public class SilhouetteFactory {
	
	/**
	 * Create a silhouette of the subject and it's children.
	 * 
	 * @param subject
	 * @param session
	 * 
	 * @return
	 */
	public Object create(Object subject, ArooaSession session) {
		
		String name = subject.toString();
		Map<String, String> description = new UniversalDescriber(
				session).describe(subject);
				
		List<Class<?>> interfaces = new ArrayList<>();
		
		interfaces.add(Describable.class);
		
		StateEvent lastJobStateEvent = null;
		
		if (subject instanceof Stateful) {
			Stateful stateful = (Stateful) subject;
			lastJobStateEvent = stateful.lastStateEvent();
			interfaces.add(Stateful.class);
		}
		
		Object[] children = null;
		
		// Session will only be null in tests.
		if (subject instanceof Structural && session != null) {
			Structural structural = (Structural) subject;
			ChildCatcher childCatcher = new ChildCatcher(session);
			structural.addStructuralListener(childCatcher);
			structural.removeStructuralListener(childCatcher);
			children = childCatcher.getChildren();
			interfaces.add(Structural.class);
		}
				
		IconInfo iconInfo = null;
		
		if (subject instanceof Iconic) {
			Iconic iconic = (Iconic) subject;
			IconCapture capture = new IconCapture();
			iconic.addIconListener(capture);
			iconic.removeIconListener(capture);
			iconInfo = capture.getIconInfo();
			interfaces.add(Iconic.class);
		}
		
		Silhouette silhouette = new Silhouette(name, description);
		
		Object proxy = Proxy.newProxyInstance(
				this.getClass().getClassLoader(), 
				interfaces.toArray(new Class[0]),
				silhouette);
		
		if (lastJobStateEvent != null) {
			silhouette.setLastStateEvent(lastJobStateEvent.copy()
					.withSource((Stateful) proxy)
					.create());
		}
		
		if (children != null) {
			StructuralEvent[] structuralEvents = 
				new StructuralEvent[children.length];
			for (int i = 0; i < structuralEvents.length; ++i) {
				StructuralEvent event = new StructuralEvent(
						(Structural) proxy, children[i], i);
				structuralEvents[i] = event;
			}
			silhouette.setChildren(structuralEvents);
		}
		
		if (iconInfo != null) {
			silhouette.setIconInfo(
					new IconEvent((Iconic) proxy, iconInfo.getIconId()),
					iconInfo.getIcon());
		}
		
		return proxy;
	}
}

/**
 * The serialized form.
 *
 */
class Silhouette implements InvocationHandler, Serializable,
        Describable, Structural, Stateful, Iconic {
	private static final long serialVersionUID = 201004092014050800L;
	
	private final Map<String, String> description;

	private final String name;
	
	private volatile StructuralEvent[] structuralEvents;
	
	private volatile transient StateEvent lastStateEvent;
	
	private volatile IconEvent iconEvent;
	
	private volatile ImageData iconTip;
	
	Silhouette(String name, Map<String, String> description) {
		this.name = name;
		this.description = description;		
	}
	
	void setChildren(StructuralEvent[] children) {
		this.structuralEvents = children;
	}
	
	void setLastStateEvent(StateEvent lastJobStateEvent) {
		this.lastStateEvent = lastJobStateEvent;
	}
	
	void setIconInfo(IconEvent iconEvent, ImageData iconTip) {
		this.iconEvent = iconEvent;
		this.iconTip = iconTip;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		Method ourMethod = getClass().getMethod(
				method.getName(), method.getParameterTypes());		
		return ourMethod.invoke(this, args);
	}
		
	@Override
	public Map<String, String> describe() {
		return description;
	}
	
	@Override
	public void addStateListener(StateListener listener) {
		listener.jobStateChange(lastStateEvent);
	}
	
	@Override
	public void removeStateListener(StateListener listener) {
	}

	@Override
	public StateEvent lastStateEvent() {
		return lastStateEvent;
	}
	
	@Override
	public void addStructuralListener(StructuralListener listener) {
		for (StructuralEvent structuralEvent : structuralEvents) {
			listener.childAdded(structuralEvent);
		}
	}
	
	@Override
	public void removeStructuralListener(StructuralListener listener) {
	}		
	
	@Override
	public void addIconListener(IconListener listener) {
		listener.iconEvent(iconEvent);
	}
	
	@Override
	public void removeIconListener(IconListener listener) {
	}
	
	@Override
	public ImageData iconForId(String id) {
		return iconTip;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof Proxy)) {
			return false;
		}
		
		return this == Proxy.getInvocationHandler(other);
	}

	/**
	 * Custom serialisation.
	 */
	private void writeObject(ObjectOutputStream s) 
	throws IOException {
		s.defaultWriteObject();
		s.writeObject(lastStateEvent == null ? 
				null : lastStateEvent.serializable());
	}

	/**
	 * Custom serialisation.
	 */
	private void readObject(ObjectInputStream s) 
	throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		StateDetail savedEvent =
				(StateDetail) s.readObject();
		if (savedEvent == null) {
			this.lastStateEvent = null;
		}
		else {
			this.lastStateEvent = savedEvent.toEvent(this);
		}
	}

}

/**
 * For catching child events.
 * 
 * @author rob
 *
 */
class ChildCatcher implements StructuralListener {

	private final ArooaSession session;

	private final List<Object> childHelper =
			new ArrayList<>();
	
	private boolean childNotOurs;
	
	public ChildCatcher(ArooaSession session) {
		this.session = session;
	}
	
	Object[] getChildren() {
		if (childNotOurs) {
			return new Object[0];
		}
		else {
			return childHelper.toArray(new Object[0]);
		}
	}
	
	@Override
	public void childAdded(StructuralEvent event) {
		
		
		Object child = event.getChild();
		if (session.getComponentPool().contextFor(child) == null) {
			childNotOurs = true;
		}
		else {
			Object childSilhouette = new SilhouetteFactory().create(child, session);
			childHelper.add(event.getIndex(), childSilhouette);
		}
	}
	
	@Override
	public void childRemoved(StructuralEvent event) {
		childHelper.remove(event.getIndex());
	}
}

/**
 * For capturing Icon info.
 *
 */
class IconInfo {
	
	private final String iconId;
	private final ImageData icon;
	
	IconInfo(String iconId, ImageData iconTip) {
		this.iconId = iconId;
		this.icon = iconTip;
	}
	
	public String getIconId() {
		return iconId;
	}
	
	public ImageData getIcon() {
		return icon;
	}
}

/**
 * For capturing Icon info.
 */
class IconCapture implements IconListener {
	
	private IconInfo iconInfo;

	@Override
	public void iconEvent(IconEvent e) {
		iconInfo = new IconInfo(e.getIconId(), 
				e.getSource().iconForId(e.getIconId()));
	}
	
	public IconInfo getIconInfo() {
		return iconInfo;
	}
}