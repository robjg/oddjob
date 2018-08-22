/*
 * Copyright (c) 2005, Rob Gordon (Apart from the Ant bits).
 *
 * This source code is heavily based on source code from the Apache
 * Ant project. As such the following is included:
 * ------------------------------------------------------------------
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.oddjob.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.oddjob.util.Clock;


/**
 * @oddjob.description Runs a SQL query. If the SQL provides results then
 * they are available as nested properties of rows, and possibly row, properties of 
 * this job.
 * 
 */
public class SQLClock {
		
//	private static final Logger logger = LoggerFactory.getLogger(SqlJob.class);
	   
	public static final String DEFAULT_SQL = "VALUES CURRENT_TIMESTAMP";
	
	/** 
	 * @oddjob.property
	 * @oddjob.description A name, can be any text.
	 * @oddjob.required No. 
	 */
	private String name;
	
	private Connection connection;
	
	private String sql;

	private PreparedStatement statement;
	
	/**
	 * Get the name.
	 * 
	 * @return The name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Set the name
	 * 
	 * @param name The name.
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public void start() throws SQLException {
		if (sql == null) {
			sql = DEFAULT_SQL;
		}
		
		if (connection == null) {
			throw new NullPointerException("No Connection.");
		}
		
		statement = connection.prepareStatement(sql);
	}

	public void stop() throws SQLException {
		try {
			if (statement != null) {
				statement.close();
			}
		}
		finally {
			if (connection != null) {
				connection.close();
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public Clock getClock() {
		
		return new Clock() {
			
			public Date getDate() {
				try {
					ResultSet rs = statement.executeQuery();
					rs.next();
					Date date = rs.getDate(1);
					rs.close();
					return date;
				}
				catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}		
			
			@Override
			public String toString() {
				return "SqlClock";
			}
		};
	}
	
	/** 
	 * @oddjob.property sql
	 * @oddjob.description The sql to provide time.
	 * @oddjob.required No. 
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}
	

	/**
	 * @oddjob.property connection
	 * @oddjob.description The {@link ConnectionType} to use.
	 * @oddjob.required Yes. 
	 */
	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	/*
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (name == null) {
			return getClass().getSimpleName();
		}
		return name;
	}
}
