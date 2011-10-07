package org.oddjob.jobs;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import org.oddjob.arooa.deploy.annotations.ArooaText;

/**
 * @oddjob.description Echo text to the console.
 * 
 * @oddjob.example
 * 
 * Hello World.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/EchoExample.xml}
 * 
 * @oddjob.example
 *
 * Hello World Twice.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/EchoTwice.xml}
 * 
 * @oddjob.example
 *
 * Echo a file list.
 * 
 * {@oddjob.xml.resource org/oddjob/jobs/EchoLinesTest.xml}
 * 
 * 
 * @author rob
 */
public class EchoJob  
implements Runnable, Serializable {
	private static final long serialVersionUID = 20051130;
		
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;

	/** 
	 * @oddjob.property
	 * @oddjob.description The text to display.
	 * @oddjob.required No, if there is no text and no lines 
	 * only a blank line will be printed.
	 */
	private String text;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Lines of text to display.
	 * @oddjob.required No, if there is no text and no lines 
	 * only a blank line will be printed.
	 * printed.
	 */
	private String[] lines;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Where to send the output.
	 * @oddjob.required No, defaults to the console.
	 */
	private transient OutputStream output;
	
	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Get the text.
	 * 
	 * @return The text.
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * Set the text.
	 * 
	 * @param The text.
	 */
	@ArooaText
	public void setText(String text) {
		this.text = text;
	}

	public String[] getLines() {
		return lines;
	}

	public void setLines(String[] lines) {
		this.lines = lines;
	}

	public OutputStream getOutput() {
		return output;
	}

	public void setOutput(OutputStream output) {
		this.output = output;
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		
		PrintStream out = null;
		if (output != null) {
			out = new PrintStream(output);
		}
		if (out == null) {
			out = System.out;
		}
		
		if (text != null) {
			out.println(text);
		}
		else if (lines != null){
			for (String line : lines) {
				out.println(line);
			}
		}
		else {
			out.println();
		}
		
		if (output != null) {
			out.close();
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (name == null) {
			return "Echo"; 
		}
		return name;
	}
}
