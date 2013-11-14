package org.oddjob.tools.includes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.oddjob.doclet.CustomTagNames;

/**
 * Converts Plain Text to HTML. At the moment this just wraps the text in
 * a <code>pre</code> tags.
 * 
 * @author rob
 *
 */
public class PlainTextToHTML {

	public String toHTML(InputStream inputStream) throws IOException {

		StringBuilder builder = new StringBuilder();
		builder.append("<pre>");
		builder.append(CustomTagNames.EOL);
		
		InputStreamReader reader = new InputStreamReader(inputStream);
		
		char[] buffer = new char[1024];
		int i;
		
		while ((i = reader.read(buffer)) != -1) {
			builder.append(buffer, 0, i);
		}
		
		builder.append("</pre>");
		builder.append(CustomTagNames.EOL);
		
		return builder.toString();
	}
}
