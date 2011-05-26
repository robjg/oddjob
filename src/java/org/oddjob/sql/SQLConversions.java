package org.oddjob.sql;

import org.oddjob.arooa.convert.ConversionProvider;
import org.oddjob.arooa.convert.ConversionRegistry;
import org.oddjob.arooa.convert.Convertlet;
import org.oddjob.arooa.convert.ConvertletException;

public class SQLConversions implements ConversionProvider {
	
	@Override
	public void registerWith(ConversionRegistry registry) {
		
		registry.register(java.util.Date.class, java.sql.Date.class, 
				new Convertlet<java.util.Date, java.sql.Date>() {
			@Override
			public java.sql.Date convert(java.util.Date from)
					throws ConvertletException {
				return new java.sql.Date(from.getTime());
			}
		});
		registry.register(java.util.Date.class, java.sql.Time.class, 
				new Convertlet<java.util.Date, java.sql.Time>() {
			@Override
			public java.sql.Time convert(java.util.Date from)
					throws ConvertletException {
				return new java.sql.Time(from.getTime());
			}
		});
		registry.register(java.util.Date.class, java.sql.Timestamp.class, 
				new Convertlet<java.util.Date, java.sql.Timestamp>() {
			@Override
			public java.sql.Timestamp convert(java.util.Date from)
					throws ConvertletException {
				return new java.sql.Timestamp(from.getTime());
			}
		});
	}

}
