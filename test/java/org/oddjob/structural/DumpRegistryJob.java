package org.oddjob.structural;

import java.util.Arrays;

import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.life.ArooaSessionAware;
import org.oddjob.arooa.registry.BeanDirectory;
import org.oddjob.arooa.registry.BeanDirectoryOwner;

public class DumpRegistryJob implements Runnable, ArooaSessionAware {

	private ArooaSession session;
	
	public void setArooaSession(ArooaSession session) {
		this.session = session;
	}
	
	public void run() {
		
		BeanDirectory reg = session.getBeanRegistry();
		
		dump(0, reg);
	}
	
	void dump(int level, BeanDirectory reg) {
		
		for (Object component : reg.getAllByType(Object.class)) {
			
			System.out.println(
					spaces(level) + 
					reg.getIdFor(component) + "=" + component);
			
			if (component instanceof BeanDirectoryOwner) {
				BeanDirectory child = 
					((BeanDirectoryOwner) component).provideBeanDirectory();
				
				dump(level + 1, child);
			}
		}
	}
	
	String spaces(int number) {
		char[] spaces = new char[number];
		Arrays.fill(spaces, ' ');
		return new String(spaces);
	}
	
}
