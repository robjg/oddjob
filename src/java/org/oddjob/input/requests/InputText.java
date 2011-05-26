package org.oddjob.input.requests;

import org.oddjob.input.InputMedium;

/**
 * @oddjob.description A request for a simple line of input.
 * 
 * @author rob
 *
 */
public class InputText extends BaseInputRequest {

	/**
	 * @oddjob.property
	 * @oddjob.description Prompt to display.
	 * @oddjob.required. No. No prompt will be displayed if missing.
	 */
	private String prompt;
	
	/**
	 * @oddjob.property default
	 * @oddjob.description The default value to use.
	 * @oddjob.required. No.
	 */
	private String defaultValue;
	
	@Override
	public void render(InputMedium medium) {
		medium.prompt(prompt, defaultValue);
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public String getDefault() {
		return defaultValue;
	}

	public void setDefault(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
}
