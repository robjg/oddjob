/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Provide some components and dimensions with a standard look and feel.
 */

public class Looks{
	
	public static final int DESIGNER_TREE_WIDTH = 200;
	public static final int DETAIL_FORM_WIDTH = 400;
	public static final int DETAIL_FORM_BORDER = 4;
	public static final int GROUP_BORDER = 3;
	
	public static final int DETAIL_USABLE_WIDTH = DETAIL_FORM_WIDTH
			- 2 * GROUP_BORDER- 2 * DETAIL_FORM_BORDER; 
	
	public static final int TEXT_FIELD_SIZE = 24;
	public static final int LABEL_SIZE = 20;
	
	public static final int LIST_ROWS = 8;
	
	public static final int DESIGNER_HEIGHT = 380;
	public static final int DESIGNER_WIDTH 
			= DESIGNER_TREE_WIDTH + DETAIL_FORM_WIDTH;
	
	/**
	 * Create a standard looking border.
	 * 
	 * @param title The border title.
	 * @return The border.
	 */
	public static Border groupBorder(String title) {
		return new CompoundBorder(new TitledBorder(title),
				new EmptyBorder(GROUP_BORDER, GROUP_BORDER, GROUP_BORDER, GROUP_BORDER));
	}
	
	/**
	 * Create the title panel with the type name for the top of
	 * the detail form.
	 */
	public static Component typePanel(String tag) {
		JPanel typePanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(4, 4, 4, 4);
		
		typePanel.setBorder(Looks.groupBorder("Type"));
		JLabel typeLabel = new JLabel(tag, SwingConstants.CENTER);
		typeLabel.setFont(typeLabel.getFont()
				.deriveFont(Font.BOLD, typeLabel.getFont().getSize() * 1.1F));
		typePanel.add(typeLabel, c);
		return typePanel;		
	}
	
}
