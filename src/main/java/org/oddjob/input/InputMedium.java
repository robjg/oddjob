package org.oddjob.input;

import org.oddjob.arooa.design.screem.FileSelectionOptions;

/**
 * Allows an {@link InputHandler} to provide a call-back to an 
 * {@link InputRequest}. Implementation are essentially the Visitor in
 * the Visitor Pattern.
 * 
 * @author rob
 *
 */
public interface InputMedium {

	/**
	 * A simple prompt for a value.
	 * 
	 * @param prompt
	 * @param defaultValue
	 */
	void prompt(String prompt, String defaultValue);
	
	/**
	 * A prompt for a password. The implementing medium should not display
	 * the password as it is typed.
	 * 
	 * @param prompt
	 */
	void password(String prompt);
	
	/**
	 * Prompt for a yes/no confirmation.
	 * 
	 * @param message
	 * @param defaultValue True for yes, False for no.
	 */
	void confirm(String message, Boolean defaultValue);
	
	/**
	 * Display a message. Wait for acknowledgement. (Any Key To Continue 
	 * type thing).
	 * 
	 * @param message
	 */
	void message(String message);
	
	
	void file(String message,
			String defaultValue, FileSelectionOptions options);
}
