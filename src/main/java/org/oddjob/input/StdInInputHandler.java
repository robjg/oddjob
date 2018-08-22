package org.oddjob.input;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * An {@link InputHandler} that uses stdin to provide input. 
 * <p>
 * 
 * @author rob
 *
 */
public class StdInInputHandler implements InputHandler {

	@Override
	public Properties handleInput(InputRequest[] requests) {
		if (System.in == null) {
			throw new IllegalStateException(
					"There is no stdin associated with the current process.");
		}
		
		LineReader in = new LineReader(System.in);
		
		Properties properties = new Properties();
		
		for (int i = 0; i < requests.length; ++i) {
			
			StdInInputMedium console = new StdInInputMedium(in);
			
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
			// This is here otherwise automated input via stdin
			// sees everything on one line.
			System.out.println();
		}
		return properties;
	}
	
	class StdInInputMedium extends TerminalInput {
		
		private final LineReader in;
		
		public StdInInputMedium(LineReader in) {
			this.in = in;
		}

		@Override
		protected String doPrompt(String prompt) {
			System.out.print(prompt);
			
			try {
				return in.readLine();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}			
		}
		
		@Override
		protected String doPassword(String prompt) {
			return doPrompt(prompt);
		}
	}
	
	static class LineReader extends FilterInputStream {
		
		public LineReader(InputStream in) {
			super(in);
		}
		
	    String readLine() throws IOException {
	    
	    	StringBuilder s = new StringBuilder();

		    while(true) {

		    	int c = read();
		    	if (c < 0) {
		    			return null;
		    	}
		    			    		
		    	if (c == '\r') {
		    		continue;
		    	}
		    	if (c == '\n') {
		    		return s.toString();
		    	}
		    	s.append((char) c);		    		
	        }
	    }
	}
}
