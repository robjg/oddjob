/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.sql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.oddjob.arooa.convert.ArooaConversionException;
import org.oddjob.arooa.types.ValueFactory;

/**
 * @oddjob.description Definition for a Database connection.
 * 
 * @oddjob.example
 * 
 * See {@link org.oddjob.sql.SQLJob} for an example.
 * 
 * @author Rob Gordon.
 */
public class ConnectionType implements ValueFactory<Connection>, Serializable {
	private final static long serialVersionUID = 20070315;
	
	private static final Logger logger = Logger.getLogger(ConnectionType.class);
		
	/** 
	 * @oddjob.property
	 * @oddjob.description The driver class name.
	 * @oddjob.required Yes. 
	 */
	private String driver;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The jdbc url.
	 * @oddjob.required Yes. 
	 */
	private String url;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The database username..
	 * @oddjob.required No. 
	 */
	private String username;

	/** 
	 * @oddjob.property
	 * @oddjob.description The users password.
	 * @oddjob.required No. 
	 */
	private String password;

	/** 
	 * @oddjob.property
	 * @oddjob.description The class loader to use to load the JDBC driver.
	 * @oddjob.required No. 
	 */
	private ClassLoader classLoader;
	
	/*
	 * (non-Javadoc)
	 * @see org.oddjob.arooa.types.ValueFactory#toValue()
	 */
	public Connection toValue() throws ArooaConversionException {
		if (driver == null) {
			throw new NullPointerException("Driver must be provided.");
		}
		if (url == null) {
			throw new NullPointerException("Url must be provided.");
		}
		
		ClassLoader loader = classLoader;
		if (loader == null) {
			loader = getClass().getClassLoader();
		}
		
		Class<?> driverClass;
		try {
			driverClass = Class.forName(driver, true, loader);
		} catch (ClassNotFoundException e) {
			throw new ArooaConversionException(e);					
		}

		Driver theDriver;
		try {
			theDriver = (Driver) driverClass.newInstance(); 
		} catch (Exception e) {
			throw new ArooaConversionException(e);					
		}
		
		Properties info = new Properties();
		if (username != null) {
		    info.put("user", username);
		}
		if (password != null) {
		    info.put("password", password);
		}

		try {
			Connection connection = theDriver.connect(url, info);
			if (connection == null) {
				throw new ArooaConversionException(
						"No connection available for [" + url + "]");
			}
			return connection;
		} catch (SQLException e) {					
			logger.warn("Failed creating connection to: " + url, e);
			for (SQLException ce = e.getNextException(); ce != null; 
					ce = ce.getNextException()) {
				logger.warn("Next chained exception:", ce);						
			}
			throw new ArooaConversionException(e);
		}
	}
	
	/**
	 * Get this connections driver class name.
	 * 
	 * @return The driver class name.
	 */
	public String getDriver() {
		return driver;
	}

	/**
	 * Set this connections dirver class name.
	 * 
	 * @param driver The driver class name.
	 */
	public void setDriver(String driver) {
		this.driver = driver;
	}

	/**
	 * Get the password.
	 * 
	 * @return The password.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set the password.
	 * 
	 * @param password The password.
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Get the url.
	 * 
	 * @return The url.
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Set the url.
	 * 
	 * @param url The url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Get the username.
	 * 
	 * @return The username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the username.
	 * 
	 * @param username The username.
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Inject
	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Connection to " + url + " as " + username;
	}
}
