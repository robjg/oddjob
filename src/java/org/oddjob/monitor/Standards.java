/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.monitor;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

/**
 *  
 */
public class Standards {

	// file actions 
	public static final Integer NEW_MNEMONIC_KEY = new Integer(KeyEvent.VK_N);
	public static final KeyStroke NEW_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK);
		
	public static final Integer OPEN_MNEMONIC_KEY = new Integer(KeyEvent.VK_O); 
	public static final KeyStroke OPEN_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK);

	public static final Integer CLOSE_MNEMONIC_KEY = new Integer(KeyEvent.VK_C);
	public static final KeyStroke CLOSE_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK);

	public static final Integer RELOAD_MNEMONIC_KEY = new Integer(KeyEvent.VK_R);
	public static final KeyStroke RELOAD_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK);

	public static final Integer SAVE_MNEMONIC_KEY = new Integer(KeyEvent.VK_S);
	public static final KeyStroke SAVE_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK);

	public static final Integer SAVEAS_MNEMONIC_KEY = new Integer(KeyEvent.VK_A);
	public static final KeyStroke SAVEAS_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK);
	
	public static final Integer EXPLORER_MNEMONIC_KEY = new Integer(KeyEvent.VK_E);
	public static final KeyStroke EXPLORER_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK);
	
	// explorer job actions
	public static final Integer RUN_MNEMONIC_KEY = new Integer(KeyEvent.VK_R);
	public static final KeyStroke RUN_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK);
	
	public static final Integer SOFT_RESET_MNEMONIC_KEY = new Integer(KeyEvent.VK_S);
	public static final KeyStroke SOFT_RESET_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_J, ActionEvent.CTRL_MASK);
	
	public static final Integer HARD_RESET_MNEMONIC_KEY = new Integer(KeyEvent.VK_H);  
	public static final KeyStroke HARD_RESET_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK);
	
	public static final Integer STOP_MNEMONIC_KEY = new Integer(KeyEvent.VK_T);  
	public static final KeyStroke STOP_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK);

	public static final Integer PROPERTY_MNEMONIC_KEY = new Integer(KeyEvent.VK_P);  
	public static final KeyStroke PROPERTY_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK);
	
	public static final Integer LOAD_MNEMONIC_KEY = new Integer(KeyEvent.VK_L);  
	public static final KeyStroke LOAD_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK);

	public static final Integer UNLOAD_MNEMONIC_KEY = new Integer(KeyEvent.VK_D);  
	public static final KeyStroke UNLOAD_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK);
	
	public static final Integer DESIGNER_MNEMONIC_KEY = new Integer(KeyEvent.VK_G);
	
	public static final Integer ADD_JOB_MNEMONIC_KEY = new Integer(KeyEvent.VK_B);
	public static final KeyStroke ADD_JOB_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK);
	
	
	// designer component actions
	public static final Integer VIEW_XML_MNEMONIC_KEY = new Integer(KeyEvent.VK_X);
	public static final KeyStroke VIEW_XML_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK);

	public static final Integer VIEW_COMP_MNEMONIC_KEY = new Integer(KeyEvent.VK_C);
	public static final KeyStroke VIEW_COMP_ACCELERATOR_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK);
	
}
