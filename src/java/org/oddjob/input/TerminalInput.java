package org.oddjob.input;


/**
 * Base class for shared implementation of Console type {@link InputMedium}s.
 * @author rob
 *
 */
abstract class TerminalInput implements InputMedium {

	private String value;
	
	@Override
	public void prompt(String prompt, String defaultValue) {
		
		StringBuilder promptBuilder = new StringBuilder();
		if (prompt != null) {
			promptBuilder.append(prompt);
		}
		promptBuilder.append("? ");
		if (defaultValue != null) {
			promptBuilder.append("(" + defaultValue + ") ");
		}
		
		String value = doPrompt(promptBuilder.toString());
		
		if (value == null) {
			this.value = null;
		}
		else if (value.length() == 0 && defaultValue != null) {
			this.value = defaultValue;
		}
		else {
			this.value = value;
		}
	}

	@Override
	public void password(String prompt) {

		StringBuilder promptBuilder = new StringBuilder();
		if (prompt != null) {
			promptBuilder.append(prompt);
		}
		promptBuilder.append("? ");

		this.value = doPassword(promptBuilder.toString());
	}
	
	@Override
	public void confirm(String message, Boolean defaultValue) {
		
		StringBuilder promptBuilder = new StringBuilder();
		if (message != null) {
			promptBuilder.append(message);
			promptBuilder.append(' ');
		}
		promptBuilder.append("(Yes/No)");
		promptBuilder.append("? ");
		if (defaultValue != null) {
			promptBuilder.append("(" + 
					(defaultValue ? "Yes" : "No") + ") ");
		}
		
		do {
			String value = doPrompt(promptBuilder.toString());
			
			if (value == null) {
				this.value = null;
				break;
			}
			else if (value.length() == 0 && defaultValue != null) {
				this.value = defaultValue.toString();
			}
			else {
				if (value.toUpperCase().matches("Y(E(S)?)?")) {
					this.value = Boolean.TRUE.toString();
				}
				else if (value.toUpperCase().matches("N(O?)")){
					this.value = Boolean.FALSE.toString();
				}
			}
		}
		while (this.value == null);
	}
	
	@Override
	public void message(String message) {
		
		this.value = doPrompt((message == null ? "" : message) +
				" (Return To Continue) ");		
	}
	
	protected abstract String doPrompt(String prompt);
	
	protected abstract String doPassword(String prompt);

	public String getValue() {
		return value;
	}
}
