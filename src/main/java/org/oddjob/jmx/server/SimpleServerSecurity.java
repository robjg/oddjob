package org.oddjob.jmx.server;

import org.oddjob.arooa.types.ValueFactory;
import org.oddjob.jmx.JMXServerJob;

import javax.management.remote.rmi.RMIConnectorServer;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.rmi.ssl.SslRMIServerSocketFactory;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @oddjob.description Provide a JMX simple security environment for a
 * {@link JMXServerJob}.
 * <p>
 * If SSL is used the appropriate JVM parameters need to be set for both
 * client and server. See  <a href="http://java.sun.com/javase/6/docs/technotes/guides/jmx/tutorial/security.html">
 * The JMX Tutorial</a>.
 *
 * 
 * @author rob
 *
 */
public class SimpleServerSecurity implements ValueFactory<Map<String,?>>{

	/** 
	 * @oddjob.property
	 * @oddjob.description The location of the password file.
	 * @oddjob.required Not sure. 
	 */
	private File passwordFile;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The location of the access file.
	 * @oddjob.required No. 
	 */
	private File accessFile;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description Use Secure Sockets (SSL).
	 * @oddjob.required No. 
	 */
	private boolean useSSL;
	
	public Map<String, ?> toValue() {
		
		Map<String, Object> env = new HashMap<>();

		if (useSSL) {
			SslRMIClientSocketFactory csf =  
				new SslRMIClientSocketFactory(); 
			SslRMIServerSocketFactory ssf =  
				new SslRMIServerSocketFactory(); 
			env.put(RMIConnectorServer. 
					RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE,csf); 
			env.put(RMIConnectorServer. 
					RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE,ssf); 
		}

	    if (passwordFile != null) {
		    env.put("jmx.remote.x.password.file", 
		              passwordFile.getAbsolutePath()); 
	    	
	    }
	    if (accessFile != null) {
		    env.put(JMXServerJob.ACCESS_FILE_PROPERTY, 
		              accessFile.getAbsolutePath()); 
	    }

		return env;
	}

	public File getPasswordFile() {
		return passwordFile;
	}

	public void setPasswordFile(File passwordFile) {
		this.passwordFile = passwordFile;
	}

	public File getAccessFile() {
		return accessFile;
	}

	public void setAccessFile(File accessFile) {
		this.accessFile = accessFile;
	}

	public boolean isUseSSL() {
		return useSSL;
	}

	public void setUseSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}
}
