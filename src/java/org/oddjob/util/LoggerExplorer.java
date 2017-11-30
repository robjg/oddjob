package org.oddjob.util;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.oddjob.Structural;
import org.oddjob.arooa.life.Destroy;
import org.oddjob.framework.HardReset;
import org.oddjob.framework.SoftReset;
import org.oddjob.structural.ChildHelper;
import org.oddjob.structural.StructuralListener;

/**
 * 
 * @author rob
 *
 */
public class LoggerExplorer implements Runnable, Structural {

	private static final Logger logger = Logger.getLogger(LoggerExplorer.class);
	
	private final ChildHelper<LoggerNode> children =
				new ChildHelper<LoggerNode>(this);
	
	public void run() {
		
		Map<String, LoggerNode> nodes = 
				new HashMap<String, LoggerNode>();
		
		Logger rootLogger = Logger.getRootLogger();
		LoggerNode rootNode = new LoggerNode(rootLogger);
		children.addChild(rootNode);
		
		@SuppressWarnings("unchecked")
		Enumeration<Logger> loggers = 
				LogManager.getLoggerRepository().getCurrentLoggers();
		
		while(loggers.hasMoreElements()) {
			Logger next = loggers.nextElement();
			
			String name = next.getName();
			
			logger.info("Processing Logger: " + name);
			
			boolean stop = false;
		
			LoggerNode node = nodes.get(name);
			
			if (node == null) {
				node = new LoggerNode(next);
				nodes.put(name, node);
			}
			else {
				node.setLogger(next);
				stop = true;
			}
			
			while (!stop) {
				
				LoggerNode parentNode;
				
				int dot = name.lastIndexOf('.');
				if (dot < 0) {
					parentNode = rootNode;
					stop = true;
				}
				else {
					name = name.substring(0, dot);
					parentNode = nodes.get(name);
					if (parentNode == null) {
						parentNode = new LoggerNode(name);
						nodes.put(name, parentNode);
					}
					else {
						stop = true;
					}
				}
				
				parentNode.addChild(node);
				
				node = parentNode;
			}
		}
	}
	
	@HardReset
	@SoftReset
	@Destroy
	public void reset() {
		
		while (children.size() > 0) {
			LoggerNode child = children.removeChildAt(0);
			child.destroy();
		}
	}
	
	@Override
	public void addStructuralListener(StructuralListener listener) {
		children.addStructuralListener(listener);
	}
	
	@Override
	public void removeStructuralListener(StructuralListener listener) {
		children.removeStructuralListener(listener);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
