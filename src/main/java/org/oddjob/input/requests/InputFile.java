package org.oddjob.input.requests;

import org.oddjob.arooa.deploy.annotations.ArooaAttribute;
import org.oddjob.arooa.design.screem.FileSelectionOptions;
import org.oddjob.arooa.design.screem.FileSelectionOptions.SelectionMode;
import org.oddjob.input.InputMedium;

import java.io.File;
import java.util.Objects;

/**
 * @oddjob.description A request for a file or directory.
 * 
 * @oddjob.example
 * 
 * See {@link org.oddjob.input.InputJob} for an example.
 * 
 * @author rob
 *
 */
public class InputFile extends BaseInputRequest {
	private static final long serialVersionUID = 2015041000L;

	/**
	 * @oddjob.property
	 * @oddjob.description Prompt to display.
	 * @oddjob.required. No. No prompt will be displayed if missing.
	 */
	private String prompt;
	
	/**
	 * @oddjob.property default
	 * @oddjob.description The default file name.
	 * @oddjob.required. No.
	 */
	private String defaultName;
	
	private final FileSelectionOptions options = 
			new FileSelectionOptions();
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.input.InputRequest#render(org.oddjob.input.InputMedium)
	 */
	@Override
	public void render(InputMedium medium) {
		medium.file(prompt, defaultName, options);
	}

	/**
	 * Getter for prompt.
	 * 
	 * @return
	 */
	public String getPrompt() {
		return prompt;
	}

	/**
	 * Setter for prompt.
	 * 
	 * @param prompt
	 */
	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public String getDefault() {
		return defaultName;
	}

	@ArooaAttribute
	public void setDefault(String defaultValue) {
		this.defaultName = defaultValue;
	}
	
	public File getCurrentDirectory() {
		return options.getCurrentDirectory();
	}

	/**
	 * @oddjob.property currentDirectory
	 * @oddjob.description The directory to start in.
	 * @oddjob.required. No.
	 */
	@ArooaAttribute
	public void setCurrentDirectory(File currentDirectory) {
		options.setCurrentDirectory(currentDirectory);
	}

	public SelectionMode getSelectionMode() {
		return options.getSelectionMode();
	}
	
	/**
	 * @oddjob.property selectionMode
	 * @oddjob.description File Selection Mode, FILE or DIRECTORY.
	 * @oddjob.required. No. Defaults to either File or Directory.
	 */
	public void setSelectionMode(SelectionMode selectionMode) {
		options.setSelectionMode(selectionMode);
	}

	public String getFileFilterDescription() {
		return options.getFileFilterDescription();
	}

	/**
	 * @oddjob.property fileFilterDescription
	 * @oddjob.description The description of the file filter used by 
	 * visual input handlers.
	 * @oddjob.required. No.
	 */
	public void setFileFilterDescription(String fileFilterDescription) {
		options.setFileFilterDescription(fileFilterDescription);
	}

	public String[] getFileFilterExtensions() {
		return options.getFileFilterExtensions();
	}

	/**
	 * @oddjob.property
	 * @oddjob.description The extensions for the file filter used by
	 * visual input handlers.
	 * @oddjob.required. No.
	 */
	@ArooaAttribute
	public void setFileFilterExtensions(String[] fileFilterExtensions) {
		options.setFileFilterExtensions(fileFilterExtensions);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		InputFile inputFile = (InputFile) o;
		return Objects.equals(getProperty(), inputFile.getProperty())
				&& Objects.equals(prompt, inputFile.prompt)
				&& Objects.equals(defaultName, inputFile.defaultName)
				&& Objects.equals(options, inputFile.options);
	}

	@Override
	public int hashCode() {
		return Objects.hash(getProperty(), prompt, defaultName, options);
	}

	@Override
	public String toString() {
		return "InputFile{" +
				"property='" + getProperty() + '\'' +
				", prompt='" + prompt + '\'' +
				", defaultName='" + defaultName + '\'' +
				", options=" + options +
				'}';
	}
}
