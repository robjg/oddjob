/*
 * (c) Rob Gordon 2006
 */
package org.oddjob.io;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.oddjob.ConverterHelper;
import org.oddjob.arooa.ArooaDescriptor;
import org.oddjob.arooa.MockArooaSession;
import org.oddjob.arooa.convert.ArooaConverter;
import org.oddjob.arooa.deploy.ClassesOnlyDescriptor;

public class ResourceTypeTest extends TestCase {
	
	public void test1() throws Exception {
		
		ArooaConverter converter = 
			new ConverterHelper().getConverter();
		
		ResourceType test = new ResourceType();
		test.setResource("org/oddjob/io/ResourceTypeTest.xml");
		test.setArooaSession(new MockArooaSession() {
			@Override
			public ArooaDescriptor getArooaDescriptor() {
				return new ClassesOnlyDescriptor(getClass().getClassLoader());
			}
		});
		InputStream in = converter.convert(test, InputStream.class);
		
		assertNotNull(in);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		String line = reader.readLine();
		
		assertEquals("<foo>A Test.</foo>", line);
		
		reader.close();
	}

}
