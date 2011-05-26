package org.oddjob.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.oddjob.tools.includes.JavaCodeFileLoader;
import org.oddjob.tools.includes.XMLFileLoader;
import org.oddjob.tools.includes.XMLResourceLoader;


public class DocPostProcessor implements Runnable {

	private File baseDir;
	
	private InputStream input;
	
	private OutputStream output;
	
	@Override
	public void run() {
		
		Injector injector1 = new JavaCodeInjector();
		Injector injector2 = new XMLResourceInjector();
		Injector injector3 = new XMLFileInjector();
		
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(input));

			PrintWriter writer = new PrintWriter(
					new OutputStreamWriter(output));

			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				
				if (!injector1.parse(line, writer)
						&& !injector2.parse(line, writer)
						&& !injector3.parse(line, writer)) {
					writer.println(line);
				}
			}

			reader.close();
			writer.close();
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public File getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(File baseDir) {
		this.baseDir = baseDir;
	}

	public InputStream getInput() {
		return input;
	}

	public void setInput(InputStream input) {
		this.input = input;
	}

	public OutputStream getOutput() {
		return output;
	}

	public void setOutput(OutputStream output) {
		this.output = output;
	}
	
	interface Injector {
		
		public boolean parse(String line, PrintWriter out);
	}
	
	class JavaCodeInjector implements Injector {	
		
		final Pattern pattern = Pattern.compile("\\{\\s*" + 
				JavaCodeFileLoader.TAG + "\\s*(\\S+)\\s*\\}");
		
		@Override
		public boolean parse(String line, PrintWriter out) {
			
			Matcher matcher = pattern.matcher(line);
			
			if (!matcher.find()) {
				return false;
			}
			
			out.println(new JavaCodeFileLoader(baseDir).load(matcher.group(1)));
			
			return true;
		}		
	}
	
	static class XMLResourceInjector implements Injector {
		
		final Pattern pattern = Pattern.compile("\\{\\s*" + 
				XMLResourceLoader.TAG + "\\s*(\\S+)\\s*\\}");
		
		@Override
		public boolean parse(String line, PrintWriter out) {
			
			Matcher matcher = pattern.matcher(line);
			
			if (!matcher.find()) {
				return false;
			}
			
			out.println(new XMLResourceLoader().load(matcher.group(1)));
			
			return true;
		}		
	}
	
	class XMLFileInjector implements Injector {	
		
		final Pattern pattern = Pattern.compile("\\{\\s*" + 
				XMLFileLoader.TAG + "\\s*(\\S+)\\s*\\}");
		
		@Override
		public boolean parse(String line, PrintWriter out) {
			
			Matcher matcher = pattern.matcher(line);
			
			if (!matcher.find()) {
				return false;
			}
			
			out.println(new XMLFileLoader(baseDir).load(matcher.group(1)));
			
			return true;
		}		
	}
}
