package org.oddjob.monitor.view;

/**
 * Display messages. Based on an example from Log4j.
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.StringTokenizer;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.oddjob.logging.LogLevel;
import org.oddjob.monitor.model.LogAction;
import org.oddjob.monitor.model.LogEventProcessor;

public class LogTextPanel extends JPanel 
implements Observer, LogEventProcessor {
	private static final long serialVersionUID = 2009071400L;
	
	private static final int MAX_DOC_LENGTH = 100000;
	
	private JTextPane textPane;
	private JCheckBox cbxTail;
	private StyledDocument doc;
	private Hashtable<LogLevel, MutableAttributeSet> fontAttributes;

	public LogTextPanel(Observable model) {
		model.addObserver(this);
		
		constructComponents();
		createDefaultFontAttributes();
	}

	private void constructComponents() {
		// setup the panel's additional components...
		this.setLayout(new BorderLayout());

		cbxTail = new JCheckBox();
		cbxTail.setSelected(true);
		cbxTail.setText("Tail log events");
		cbxTail.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (cbxTail.isSelected()) {
					textPane.setCaretPosition(doc.getLength());
				}
			}
		});

		JPanel bottomPanel = new JPanel();
		bottomPanel.add(cbxTail, null);

		textPane = new JTextPane() {
			private static final long serialVersionUID = 2010022700L;
			@Override
			public boolean getScrollableTracksViewportWidth() {
				return false;
			}
		};
		textPane.setEditable(false);
		textPane.setText("");
		doc = textPane.getStyledDocument();
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(textPane);
		scroll.getViewport().setBackground(Color.white );

		this.add(bottomPanel, BorderLayout.SOUTH);
		this.add(scroll, BorderLayout.CENTER);
	}

	public void setTextBackground(Color color) {
		textPane.setBackground(color);
	}

	public void setTextBackground(String v) {
		textPane.setBackground(parseColor(v));
	}

	private void createDefaultFontAttributes() {
		LogLevel[] prio = new LogLevel[] { 
				LogLevel.FATAL, 
				LogLevel.ERROR,
				LogLevel.WARN, 
				LogLevel.INFO, 
				LogLevel.DEBUG,
				LogLevel.TRACE };

		fontAttributes = new Hashtable<LogLevel, MutableAttributeSet>();
		for (int i = 0; i < prio.length; i++) {
			MutableAttributeSet att = new SimpleAttributeSet();
			fontAttributes.put(prio[i], att);
			//StyleConstants.setFontSize(att,11);
		}

		setTextColor(LogLevel.FATAL, Color.red);
		setTextColor(LogLevel.ERROR, Color.magenta.darker());
		setTextColor(LogLevel.WARN, Color.orange.darker());
		setTextColor(LogLevel.INFO, Color.blue);
		setTextColor(LogLevel.DEBUG, Color.black);
		setTextColor(LogLevel.TRACE, Color.darkGray);
		
		setTextFontName("Lucida Console");
	}

	private Color parseColor(String v) {
		StringTokenizer st = new StringTokenizer(v, ",");
		int val[] = { 255, 255, 255, 255 };
		int i = 0;
		while (st.hasMoreTokens()) {
			val[i] = Integer.parseInt(st.nextToken());
			i++;
		}
		return new Color(val[0], val[1], val[2], val[3]);
	}

	void setTextColor(LogLevel l, String v) {
		StyleConstants.setForeground((MutableAttributeSet) fontAttributes
				.get(l), parseColor(v));
	}

	void setTextColor(LogLevel l, Color c) {
		StyleConstants.setForeground((MutableAttributeSet) fontAttributes
				.get(l), c);
	}

	void setTextFontSize(int size) {
		Enumeration<?> e = fontAttributes.elements();
		while (e.hasMoreElements()) {
			StyleConstants.setFontSize((MutableAttributeSet) e.nextElement(),
					size);
		}
		return;
	}

	void setTextFontName(String name) {
		Enumeration<?> e = fontAttributes.elements();
		while (e.hasMoreElements()) {
			StyleConstants.setFontFamily((MutableAttributeSet) e.nextElement(),
					name);
		}
		return;
	}

	public void onClear() {
		try {
			doc.remove(0, doc.getLength());
		} catch (BadLocationException e) {
				e.printStackTrace();
		}
	}
	
	public void onUnavailable() {
		textPane.setText(("No log available."));
	}
	
	public void onEvent(final String text, final LogLevel level) {

					try {
						doc.insertString(doc.getLength(), text,
								(MutableAttributeSet) fontAttributes
										.get(level));
						int overflow = doc.getLength() - MAX_DOC_LENGTH;
						if (overflow > 0) {
							doc.remove(0, overflow);
							// this will work when we move to 1.5 and can use
							// DefaultCaret.NEVER_UPDATE but at the moment the caret
							// does it's own thing on remove and we can't change it.
							if (!cbxTail.isSelected()) {
								int position = textPane.getCaretPosition();
								if (position - overflow < 0) {
									position = 0;
								}
								textPane.setCaretPosition(position);
							}
						}
						if (cbxTail.isSelected()) {
							textPane.setCaretPosition(doc.getLength());
						}
					} catch (BadLocationException e) {
						e.printStackTrace();
					}	
	}
	
	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		LogAction a = (LogAction) arg;
		a.accept(this);
	}
}
