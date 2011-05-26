package org.oddjob.input;

import java.util.Properties;


/**
 * An {@link InputHandler} that uses the console to provide input.
 * 
 * @author rob
 *
 */
public class ConsoleInputHandler implements InputHandler {

	@Override
	public Properties handleInput(InputRequest[] requests) {
		if (System.console() == null) {
			throw new IllegalStateException(
					"There is no console for this process.");
		}
		
		Properties properties = new Properties();
		
		for (int i = 0; i < requests.length; ++i) {
			ConsoleInputMedium console = new ConsoleInputMedium();
			requests[i].render(console);
			
			String value = console.getValue();
			// Input must have been cancelled with a Control-Z.
			if (value == null) {
				return null;
			}

			String property = requests[i].getProperty();
			if (property == null) {
				continue;
			}
			properties.setProperty(property, value);
		}
		return properties;
	}
	
	static class ConsoleInputMedium extends TerminalInput {

		@Override
		protected String doPrompt(String prompt) {
			
			return System.console().readLine(prompt);
		}
		
		@Override
		protected String doPassword(String prompt) {
			
			char[] password = System.console().readPassword(prompt);

			if (password == null) {
				return null;
			}
			else {
				return new String(password);
			}
		}
	}
}
