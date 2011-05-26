package org.oddjob.input;

/**
 * 
 * @author rob
 *
 */
public interface InputRequest {

	public void render(InputMedium medium);
	
	public String getProperty();
}
