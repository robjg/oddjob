/*
 * (c) Rob Gordon 2005.
 */
package org.oddjob.designer.components;

import org.oddjob.arooa.design.DesignFactory;
import org.oddjob.arooa.design.DesignInstance;
import org.oddjob.arooa.design.DesignProperty;
import org.oddjob.arooa.design.SimpleDesignProperty;
import org.oddjob.arooa.design.SimpleTextAttribute;
import org.oddjob.arooa.design.screem.BorderedGroup;
import org.oddjob.arooa.design.screem.Form;
import org.oddjob.arooa.design.screem.StandardForm;
import org.oddjob.arooa.parsing.ArooaContext;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.jmx.JMXClientJob;

/**
 * Design Component for {@link JMXClientJob}.
 * 
 * @author rob
 * 
 */
public class ClientDC implements DesignFactory {
	
	public DesignInstance createDesign(ArooaElement element,
			ArooaContext parentContext) {

		return new ClientDesign(element, parentContext);
	}
		
}

class ClientDesign extends BaseDC {

	private final SimpleTextAttribute connection;
	
	private final SimpleDesignProperty environment;
	
	private final SimpleTextAttribute heartbeat;
	
	private final SimpleTextAttribute maxLoggerLines;
	
	private final SimpleTextAttribute maxConsoleLines;
	
	private final SimpleTextAttribute logPollingInterval;
	
	public ClientDesign(ArooaElement element, ArooaContext parentContext) {
		super(element, parentContext);
		
		connection = new SimpleTextAttribute("connection", this);
		
		environment = new SimpleDesignProperty("environment", this);
		
		heartbeat = new SimpleTextAttribute("heartbeat", this);
		
		maxLoggerLines = new SimpleTextAttribute("maxLoggerLines", this);
		
		maxConsoleLines = new SimpleTextAttribute("maxConsoleLines", this);

		logPollingInterval = new SimpleTextAttribute("logPollingInterval", this);
	}
	
	public DesignProperty[] children() {
		return new DesignProperty[] { name, connection, environment, 
				heartbeat, maxLoggerLines, maxConsoleLines, logPollingInterval };
	}
	
	
	public Form detail() {
		return new StandardForm(this)
		.addFormItem(basePanel())	
		.addFormItem(
				new BorderedGroup("Connection Details")
				.add(connection.view().setTitle("Connection"))
				.add(environment.view().setTitle("Environment"))
			)
		.addFormItem(
				new BorderedGroup("Advanced")
				.add(heartbeat.view().setTitle("Heartbeat Interval"))
				.add(maxLoggerLines.view().setTitle("Log Lines"))
				.add(maxConsoleLines.view().setTitle("Console Lines"))
				.add(logPollingInterval.view().setTitle("Log Interval"))
			);
	}
		
}

