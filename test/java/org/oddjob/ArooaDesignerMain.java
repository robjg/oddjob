package org.oddjob;

import java.io.File;

import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.ArooaParseException;
import org.oddjob.arooa.ArooaSession;
import org.oddjob.arooa.ArooaType;
import org.oddjob.arooa.design.designer.ArooaDesigner;
import org.oddjob.arooa.parsing.ArooaElement;
import org.oddjob.arooa.standard.StandardArooaSession;

public class ArooaDesignerMain {

	
	public void testRun() throws ArooaParseException {
		
		ArooaDesigner designer = new ArooaDesigner();

    	ArooaDescriptor descriptor = new OddjobDescriptorFactory(
    		).createDescriptor(getClass().getClassLoader());
		
		final ArooaSession session = new StandardArooaSession(
						descriptor);
		
		designer.setArooaSession(session);		
		designer.setFile(
				new File("c:/rob/projects/ojobs/oddjobbuild.xml"));
				
		designer.setDocumentElement(new ArooaElement("oddjob"));
		designer.setArooaType(ArooaType.COMPONENT);
		
		designer.run();
		
	}
	
	public static void main(String[] args) throws ArooaParseException {
		
		new ArooaDesignerMain().testRun();
	}
	
}
