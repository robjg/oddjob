package org.oddjob.input;

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
	public void prompt(String prompt, String defaultValue);
	
	/**
	 * A prompt for a password. The implementing medium should not display
	 * the password as it is typed.
	 * 
	 * @param prompt
	 */
	public void password(String prompt);
	
	/**
	 * Prompt for a yes/no confirmation.
	 * 
	 * @param message
	 * @param defaultValue True for yes, False for no.
	 */
	public void confirm(String message, Boolean defaultValue);
	
	/**
	 * Display a message. Wait for acknolagement. (Any Key To Continue 
	 * type thing).
	 * 
	 * @param message
	 */	
	public void message(String message);
}
