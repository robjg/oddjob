package org.oddjob.beanbus;

import java.util.EventObject;

/**
 * An event broadcast by a {@link StageNotifier}.
 * 
 * @author rob
 *
 */
public class StageEvent extends EventObject {
	private static final long serialVersionUID = 2010021800L;
	
	private final String description;

	private final Object data;
	
	/**
	 * Constructor with no data.
	 * 
	 * @param source
	 * @param description
	 */
	public StageEvent(StageNotifier source, String description) {
		this(source, description, null);
	}
	
	/**
	 * Constructor with data.
	 * 
	 * @param source
	 * @param description
	 * @param data
	 */
	public StageEvent(StageNotifier source, String description, Object data) {
		super(source);
		this.description = description;
		this.data = data;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Object getData() {
		return data;
	}
	
	@Override
	public StageNotifier getSource() {
		return (StageNotifier) super.getSource();
	}
}
