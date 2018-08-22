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

	public void render(InputMedium medium);
	
	public String getProperty();
}
