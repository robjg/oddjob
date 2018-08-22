package org.oddjob.jmx.client;

import java.util.HashMap;
import java.util.Map;

import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.jmx.JMXClientJob;

/**
 * 
 * @oddjob.description Provide a JMX simple security credentials 
 * environment for a {@link JMXClientJob}.
 * <p>
 * 
 * @author rob
 *
 */
public class UsernamePassword implements ValueFactory<Map<String,?>>{

	/** 
	 * @oddjob.property
	 * @oddjob.description The username.
	 * @oddjob.required Yes. 
	 */
	private String username;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The password.
	 * @oddjob.required Yes. 
	 */
	private String password;
	
	public Map<String, ?> toValue() {
		
		Map<String, Object> env = new HashMap<String, Object>();
		
	    String[] credentials = new String[] { username , password }; 
	    env.put("jmx.remote.credentials", credentials); 

		return env;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
