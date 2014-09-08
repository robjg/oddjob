package org.oddjob.values.properties;

import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

/**
 * Track system properties set.
 * 
 * @author rob
 *
 */
public class SystemPropertyStack {

	private static final LinkedList<Token> tokens = 
			new LinkedList<>();
			
				
	public static synchronized Token addProperties(Properties properties) {
		
		Token token = new Token(properties);
		tokens.push(token);
		return token;
	}
	
	public static synchronized void removeProperties(Token token) {
		
		if (tokens.peek() == token) {
			token.restore();
			tokens.pop();
		}
		else {
			Token next = tokens.get(tokens.indexOf(token) - 1);
			next.acceptFrom(token);
			tokens.remove(token);
		}
	}
	
	public static class Token {
		
		private final Set<String> propertyNames;
		
		private final Properties originals = new Properties();
		
		Token(Properties properties) {
			this.propertyNames = properties.stringPropertyNames();
			for (String name : propertyNames) {
				String existing = System.getProperty(name);
				if (existing != null) {
					originals.put(name, existing);
				}
				System.setProperty(name, properties.getProperty(name));
			}
		}
				
		void acceptFrom(Token previous) {
			
			for (String name : previous.propertyNames) {
				
				String value = previous.originals.getProperty(name);
				
				if (propertyNames.contains(name)) {
					if (value == null) {
						originals.remove(name);
					}
					else {
						originals.put(name, value);
					}
				}
				else {
					if (value == null) {
						System.getProperties().remove(name);
					}
					else {
						System.setProperty(name, value);
					}
				}
				
			}
		}
		
		void restore() {
			
			for (String name : propertyNames) {
				
				String value = originals.getProperty(name);
				
				if (value == null) {
					System.getProperties().remove(name);
				}
				else {
					System.setProperty(name, value);
				}
			}
		}
	}
}
