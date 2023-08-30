package org.oddjob.input.requests;

import org.oddjob.input.InputMedium;

import java.util.Objects;

/**
 * @oddjob.description An input request for a password.
 * 
 * @oddjob.example
 * 
 * See {@link org.oddjob.input.InputJob} for an example.
 * 
 * @author rob
 *
 */
public class InputPassword extends BaseInputRequest {
	private static final long serialVersionUID = 2015041000L;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InputPassword inputText = (InputPassword) o;
		return Objects.equals(getProperty(), inputText.getProperty()) &&
				Objects.equals(prompt, inputText.prompt);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getProperty(), prompt);
	}

}
