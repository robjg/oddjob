package org.oddjob.swing;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.oddjob.framework.SerializableJob;

/**
 * Pop up a dialog to select a file.
 * 
 * @author Rob Gordon 
 */

public class ChooseFile extends SerializableJob {
	private static final long serialVersionUID = 2009042100L;
	
	private File chosen;
	
	private File dir;

	public File getChosen() {
		return chosen;
	}

	public void setDir(File dir) {
	    this.dir = dir;
	}
	
	public int execute() throws Exception {

	    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    
	    JFrame f = new JFrame();

	    JFileChooser chooser = new JFileChooser();
	    if (dir != null) {
	        chooser.setCurrentDirectory(dir);
	    }
	    
		int option = chooser.showOpenDialog(f);
		f.dispose();
		
		if (option == JFileChooser.APPROVE_OPTION) {
			chosen = chooser.getSelectedFile();
			logger().debug("chosen file " + chosen.getAbsolutePath());
			return 0;
		} else {
			chosen = null;
			return 1;
		}		
	}

}
