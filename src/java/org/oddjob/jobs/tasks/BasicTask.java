package org.oddjob.jobs.tasks;

import java.util.Properties;

public class BasicTask implements Task {
	private static final long serialVersionUID = 2015050800L;

	private final Properties properties;
	
	public BasicTask(Properties properties) {
		this.properties = properties;
	}
	
	
	@Override
	public Properties getProperties() {
		return properties;
	}
}
