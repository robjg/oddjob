package org.oddjob.input.requests;

import org.oddjob.input.InputMedium;

import java.util.Objects;

/**
 * @oddjob.description A request for a yes/no confirmation. The value
 * captured for this request is a boolean true/false value.
 * 
 * @oddjob.example
 * 
 * See {@link org.oddjob.input.InputJob} for an example.
 * 
 * @author rob
 *
 */
public class InputConfirm extends BaseInputRequest {
	private static final long serialVersionUID = 2015041000L;

	/**
	 * @oddjob.property
	 * @oddjob.description Prompt to display.
	 * @oddjob.required. No. Only a yes/no prompt will be displayed if missing.
	 */
	private String prompt;
	
	/**
	 * @oddjob.property default
	 * @oddjob.description The default value to use, true or false.
	 * @oddjob.required. No.
	 */
	private Boolean defaultValue;
	
	
	@Override
	public void render(InputMedium medium) {
		medium.confirm(prompt, defaultValue);
	}

	public String getPrompt() {
		return prompt;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public Boolean getDefault() {
		return defaultValue;
	}

	public void setDefault(Boolean defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InputConfirm that = (InputConfirm) o;
		return Objects.equals(getProperty(), that.getProperty())
				&& Objects.equals(prompt, that.prompt)
				&& Objects.equals(defaultValue, that.defaultValue);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getProperty(), prompt, defaultValue);
	}
}
