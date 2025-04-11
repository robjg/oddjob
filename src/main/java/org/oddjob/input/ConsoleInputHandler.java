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
	public Session start() {
		return new Session() {
			@Override
			public Properties handleInput(InputRequest[] requests) {
				if (System.console() == null) {
					throw new IllegalStateException(
							"There is no console for this process.");
				}

				Properties properties = new Properties();

                for (InputRequest request : requests) {
                    ConsoleInputMedium console = new ConsoleInputMedium();
                    request.render(console);

                    String value = console.getValue();
                    // Input must have been cancelled with a Control-Z.
                    if (value == null) {
                        return null;
                    }

                    String property = request.getProperty();
                    if (property == null) {
                        continue;
                    }
                    properties.setProperty(property, value);
                }
				return properties;
			}

			@Override
			public void close() {

			}
		};
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
