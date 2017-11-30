package org.oddjob.jobs;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @oddjob.description A job that performs XSLT transformations.
 * <p>
 * Still a work in progress.
 * 
 * @author rob
 *
 */
public class XSLTJob implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(XSLTJob.class);
	
	private String name;
	
	private File[] from;
	
	private File to;
	
	private InputStream stylesheet;
	
	private InputStream input;
	
	private OutputStream output;
	
	private final Map<String, Object> parameters =
		new HashMap<String, Object>();
	
	private Transformer transformer;
	
	public void run() {
		
		InputStream input = this.input;
		OutputStream output = this.output;
		
		try {
			
			if (from != null) {
				if (from.length == 1) {
					String info = "Processing " + from[0];
					
					input = new BufferedInputStream(
							new FileInputStream(from[0]));
					
					if (to != null) {
						info += " to " + to;
						
						output = new BufferedOutputStream(
								new FileOutputStream(to));
					}
					
					logger.info(info);
				}
				else {
					if (to == null) {
						throw new RuntimeException(
								"A to directory must be provided for " +
								"transforming multiple files");
					}					
					if (!to.isDirectory()) {
						throw new RuntimeException(
								"The to must be a directory for " +
								"transforming multiple files");
					}
					
					for (File file : from) {
												
						File outputFile = new File(to, file.getName());
						
						logger.info("Processing " + file + " to " + outputFile);
						
						input = new BufferedInputStream(
								new FileInputStream(file));
						
						output = new BufferedOutputStream(
								new FileOutputStream(outputFile));
								
						transform(input, output);
					}
					
					return;
				}
			}
			
			if (input == null) {
				throw new NullPointerException("No From.");
			}
			if (output == null) {
				throw new NullPointerException("No To.");
			}
			
			transform(input, output);
			
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			transformer = null;			
			
			try {
				if (stylesheet != null) {
					stylesheet.close();
				}
			} catch (IOException e) {
				// ignore
			}
			
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException e) {
				// ignore
			}
			
			try {
				if (output!= null) {
					output.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}
	}

	protected void transform(InputStream input, OutputStream output) 
	throws TransformerException, IOException {
		
		if (transformer == null) {			
			
			if (stylesheet == null) {
				transformer = TransformerFactory.newInstance().newTransformer();
			}
			else {
				transformer = TransformerFactory.newInstance(
						).newTransformer(new StreamSource(stylesheet));				
			}
		}

		for (Map.Entry<String, Object> entry: parameters.entrySet()) {
			transformer.setParameter(entry.getKey(), entry.getValue());
		}

		transformer.transform(
				new StreamSource(input), 
				new StreamResult(output));
	}
	
	public void setStylesheet(InputStream stylesheet) {
		this.stylesheet = stylesheet;
	}

	public void setInput(InputStream from) {
		this.input = from;
	}

	public void setOutput(OutputStream to) {
		this.output = to;
	}

	public Object getParameters(String name) {
		return parameters.get(name);
	}

	public void setParameters(String name, Object value) {
		this.parameters.put(name, value);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public File[] getFrom() {
		return from;
	}

	public void setFrom(File[] from) {
		this.from = from;
	}

	public File getTo() {
		return to;
	}

	public void setTo(File to) {
		this.to = to;
	}
	
	@Override
	public String toString() {
		if (name == null) {
			return getClass().getName();
		}
		else {
			return name;
		}
	}
}
