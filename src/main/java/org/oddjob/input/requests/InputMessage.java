package org.oddjob.input.requests;

import org.oddjob.arooa.deploy.annotations.ArooaText;
import org.oddjob.input.InputMedium;
import org.oddjob.input.InputRequest;

import java.io.Serializable;
import java.util.Objects;

/**
 * @oddjob.description A Message with a prompt to continue. 
 * 
 * @oddjob.example
 * 
 * See {@link org.oddjob.input.InputJob} for an example.
 * 
 * @author rob
 *
 */
public class InputMessage 
implements InputRequest, Serializable {
	private static final long serialVersionUID = 2015041000L;

	/**
	 * @oddjob.property
	 * @oddjob.description The Message.
	 * @oddjob.required. No.
	 */
	private String message;
	
	
	@Override
	public void render(InputMedium medium) {
		medium.message(message);
	}

	public String getMessage() {
		return message;
	}

	@ArooaText
	public void setMessage(String prompt) {
		this.message = prompt;
	}
	
	/**
	 * @oddjob.property property
	 * @oddjob.description Always null. This is because no
	 * property can be set for a confirmation.
	 */
	@Override
	public String getProperty() {
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InputMessage that = (InputMessage) o;
		return Objects.equals(message, that.message);
	}

	@Override
	public int hashCode() {
		return Objects.hash(message);
	}
}
