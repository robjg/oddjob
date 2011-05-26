package org.oddjob.input.requests;

import org.oddjob.input.InputMedium;

/**
 * @oddjob.description An input request for a password.
 * 
 * @author rob
 *
 */
public class InputPassword extends BaseInputRequest {

	/**
	 * @oddjob.property
	 * @oddjob.description Prompt to display.
	 * @oddjob.required. No. No prompt will be displayed if missing.
	 */
	private String prompt;
	
	@Override
	public void render(InputMedium medium) {
		medium.password(prompt);
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

}
