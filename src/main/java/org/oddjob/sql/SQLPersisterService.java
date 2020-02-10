/*
 * Copyright (c) 2004, Rob Gordon.
 */
package org.oddjob.sql;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.oddjob.arooa.life.ComponentPersistException;
import org.oddjob.arooa.registry.Path;
import org.oddjob.persist.OddjobPersister;
import org.oddjob.persist.PersisterBase;

/**
 * @oddjob.description Persists job state to a database. The database must
 * have a table which can be created with the following sql.
 * <pre><code>
 * CREATE TABLE oddjob(
 *   path VARCHAR(128), 
 *   id VARCHAR(32), 
 *   job BLOB, 
 *  CONSTRAINT oddjob_pk PRIMARY KEY (path, id))
 * </pre></code>
 * 
 * @oddjob.example
 * 
 * Using a SQL Persister.
 * 
 * {@oddjob.xml.resource org/oddjob/sql/SqlPersisterTest.xml}
 * 
 * Note that because this is a service, it must be stopped once the inner Oddjob
 * has completed it's work. In an Oddjob that was running continually this would
 * not be necessary.
 * 
 * @author Rob Gordon.
 */
public class SQLPersisterService {
	private static final Logger logger = LoggerFactory.getLogger(SQLPersisterService.class);

	/** 
	 * @oddjob.property
	 * @oddjob.description The {@link ConnectionType} to use.
	 * @oddjob.required Yes. 
	 */
	private Connection connection;
	
	/** 
	 * @oddjob.property
	 * @oddjob.description The name.
	 * @oddjob.required No. 
	 */
	private String name;
	
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A plugin for providers of the serialization.
	 * The default is for HSQL.
	 * @oddjob.required No. 
	 */
	private SQLSerializationFactory serializationFactory;
	
	/**	The actual serialization. */
	private volatile SQLSerialization serialization;
	
	public void start() throws SQLException {
		
		if (serializationFactory == null) {
			serializationFactory = new HSQLSerializationFactory();
		}
		
		serialization = serializationFactory.createSerialization(connection);
	}
	
	public void stop() throws SQLException {
		if (serialization != null) {
			serialization.close();
			serialization = null;
		}
	}
	
	
	/**
	 * Set the connection.
	 * 
	 * @param connection The connection.
	 */
	public void setConnection(Connection connection) throws SQLException {
		this.connection = connection;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public SQLSerializationFactory getSerializationFactory() {
		return serializationFactory;
	}

	public void setSerializationFactory(SQLSerializationFactory serializationFactory) {
		this.serializationFactory = serializationFactory;
	}

	/** 
	 * @oddjob.property persister
	 * @oddjob.description The persister.
	 * @oddjob.required R/O. 
	 */
	public OddjobPersister getPersister(String path) {
		return new SQLPersister(path);
	}
	
	@Override
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		else {
			return name;
		}
	}
	
	class SQLPersister extends PersisterBase {

		public SQLPersister(String path) {
			super(path == null ? null : new Path(path));
		}
		
		@Override
		protected void persist(Path path, String id, Object o) 
		throws ComponentPersistException {

			if (serialization == null) {
				throw new IllegalStateException("Persister Service Not Started.");
			}

			try {
				serialization.persist(path, id, o);

				logger.debug("Saved [" + o + "], id [" + id 
						+ "] to database.");
			}
			catch (SQLException e) {
				throw new ComponentPersistException("Failed saving object id ["
						+ id + "], class [" + o.getClass().getName() 
						+ "], object [" + o + "]."
						, e);
			}
		}

		@Override
		protected Object restore(Path path, String id, ClassLoader classLoader) 
		throws ComponentPersistException {

			if (serialization == null) {
				throw new IllegalStateException("Persister Service Not Started.");
			}

			try {
				return serialization.restore(path, id, classLoader);
			}
			catch (SQLException e) {
				throw new ComponentPersistException("Failed to restore object.", e);
			}
		}
		
		@Override
		protected String[] list(Path path)
				throws ComponentPersistException {
			if (serialization == null) {
				throw new IllegalStateException("Persister Service Not Started.");
			}

			try {
				return serialization.children(path);
			}
			catch (SQLException e) {
				throw new ComponentPersistException("Failed to remove object.", e);
			}		
		}

		@Override
		protected void remove(Path path, String id) 
		throws ComponentPersistException {
			
			if (serialization == null) {
				throw new IllegalStateException("Persister Service Not Started.");
			}

			try {
				serialization.remove(path, id);
			}
			catch (SQLException e) {
				throw new ComponentPersistException("Failed to remove object.", e);
			}		
		}

		@Override
		protected void clear(Path path) 
		throws ComponentPersistException {
			if (serialization == null) {
				throw new IllegalStateException("Persister Service Not Started.");
			}

			try {
				serialization.clear(path);
			}
			catch (SQLException e) {
				throw new ComponentPersistException("Failed to clear.", e);
			}		
		}
		
		@Override
		public String toString() {
			return getClass().getSimpleName();
		}
	}	
}
