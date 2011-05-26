package org.oddjob.monitor.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Observable;

import org.oddjob.state.JobStateEvent;

/**
 * This class encapsualtes the model of a job state.
 * 
 * @author Rob Gordon 
 */


public class StateModel extends Observable {

	private String state;
	private String time;
	private String exception;
					

	public void change(JobStateEvent event) {
		state = event.getJobState().toString();
		time = event.getTime().toString();
		StringWriter stackBuffer = new StringWriter();
		
		Throwable t = event.getException();
		if (t != null) {
			PrintWriter writer = new PrintWriter(stackBuffer);
			t.printStackTrace(writer);
			exception = stackBuffer.toString();
		}
		else {
			exception = "";
		}
		setChanged();
		notifyObservers();
	}
		
	/**
	 * @return Returns the exception.
	 */
	public String getException() {
		return exception;
	}
	/**
	 * @return Returns the state.
	 */
	public String getState() {
		return state;
	}
	/**
	 * @return Returns the time.
	 */
	public String getTime() {
		return time;
	}
	
	public void clear() {
		state = "";
		time = "";
		exception = "";
		setChanged();
		notifyObservers();
	}

}
