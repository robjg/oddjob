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

	void render(InputMedium medium);
	
	String getProperty();
}
