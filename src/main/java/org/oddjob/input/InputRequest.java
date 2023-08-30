package org.oddjob.input;

import java.io.Serializable;

/**
 * Provides an input. 
 * 
 * @see InputJob
 * 
 * @author rob
 *
 */
public interface InputRequest extends Serializable {

	/**
	 * Render the input request with the given medium.
	 *
	 * @param medium The medium. Never null.
	 */
	void render(InputMedium medium);

	/**
	 * The property set by this request. May be null i.e. {@link org.oddjob.input.requests.InputMessage}.
	 *
	 * @return The property. May be null.
	 */
	String getProperty();
}
