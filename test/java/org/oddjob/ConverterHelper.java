package org.oddjob;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.standard.StandardArooaSession;

public class ConverterHelper {

	public ArooaConverter getConverter() {
		
    	ArooaDescriptor descriptor = 
    		new OddjobDescriptorFactory().createDescriptor(null);
    	
		ArooaSession session = new StandardArooaSession(
				descriptor);

		return session.getTools().getArooaConverter();
	}
}
