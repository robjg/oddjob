package org.oddjob.tools.includes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SnippetFilter implements StreamToText {

	private final Pattern start;

	private final Pattern end;
	
	public SnippetFilter(String filter) {
		
		start = Pattern.compile("#" + Pattern.quote(filter) + 
			"\\s*\\{");
		
		end = Pattern.compile("\\}\\s*#" + Pattern.quote(filter) + 
			"\\b");
	}
	
	@Override
	public String load(InputStream input) throws IOException {
		
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(input));
		
		StringWriter buff = new StringWriter();
		PrintWriter writer = new PrintWriter(buff);
		
		boolean record = false;
		
		while (true) {
			
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			
			
			if (record) {
				Matcher matcher = end.matcher(line);
				
				if (matcher.find()) {
					record = false;
				}
				else {
					writer.println(line);
				}		
			}
			else {
				Matcher matcher = start.matcher(line);
				
				if (matcher.find()) {
					record = true;
				}
				
			}
		}

		reader.close();
		writer.close();
		
		return buff.toString();
	}
	
}
