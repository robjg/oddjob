package org.oddjob.util;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.oddjob.Structural;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

public class LoggerNode implements Structural {

	private final ChildHelper<LoggerNode> children = 
		new ChildHelper<LoggerNode>(this);
	
	private final String name;
	
	private Logger logger;
	
	public LoggerNode(Logger logger) {
		this.logger = logger;
		this.name = logger.getName();
	}
	
	public LoggerNode(String name) {
		this.name = name;
	}
	
	public String getLevel() {
		if (logger == null) {
			return null;
		}
		Level level = logger.getLevel();
		if (level == null) {
			return null;
		}
		else {
			return level.toString();
		}
	}
	
	public void setLevel(String level) {
		if (logger == null) {
			logger = Logger.getLogger(name);
		}
		if (level == null || level.length() == 0) {
			logger.setLevel(null);
		}
		else {
			logger.setLevel(Level.toLevel(level));
		}
	}
	
	void addChild(LoggerNode child) {
		children.addChild(child);
	}
	
	void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public void addStructuralListener(StructuralListener listener) {
		children.addStructuralListener(listener);
	}
	
	@Override
	public void removeStructuralListener(StructuralListener listener) {
		children.removeStructuralListener(listener);
	}
	
	public String toString() {
		return name;
	}
	
	public void destroy() {
		
		while (children.size() > 0) {
			LoggerNode child = children.removeChildAt(0);
			child.destroy();
		}
	}
}
