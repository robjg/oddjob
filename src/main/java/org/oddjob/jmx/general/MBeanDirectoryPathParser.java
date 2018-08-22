package org.oddjob.jmx.general;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses an MBean property expression into the object name and the
 * attribute name and possibly further properties of the attribute.
 * 
 * @author rob
 *
 */
public class MBeanDirectoryPathParser {

	public static final String QUOTE = "\"";
	
	public static final String DELIMITER = Pattern.quote(".");
	
	private final static Pattern pattern = Pattern.compile(
			"((\"([^" + QUOTE + "]+?)\")|([^" + QUOTE + 
	//       12  3                       4
			"]+?))((" + DELIMITER + "(.+)?)|($))");
	//            56                 7      8
	
	
	private String name;
	
	private String property;
	

	public void parse(String expression) throws ParseException {
		
		name = null;
		property = null;
		
		Matcher matcher = pattern.matcher(expression);
				
		if (!matcher.matches()) {
			throw new ParseException("Unable to parse: " + expression, 0);
		}
		
		name = matcher.group(3);
		if (name == null) {
			name = matcher.group(4);
		}
		
		property = matcher.group(7);
	}
	
	public String getName() {
		return name;
	}
	
	public String getProperty() {
		return property;
	}
}
