package org.oddjob.swing;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.oddjob.framework.SerializableJob;

public class ConfirmationJob extends SerializableJob {
	private static final long serialVersionUID = 2010010600L;
	
	private volatile String title;
	
	private volatile String message;
	
	@Override
	protected int execute() throws Throwable {
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		
		int result = JOptionPane.showConfirmDialog(null,
				message, title, JOptionPane.YES_NO_OPTION);	
		
		return result;

	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}	
}
