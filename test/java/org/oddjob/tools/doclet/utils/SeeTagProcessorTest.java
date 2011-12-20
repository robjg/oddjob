package org.oddjob.tools.doclet.utils;

import junit.framework.TestCase;

import org.mockito.Mockito;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.SeeTag;

public class SeeTagProcessorTest extends TestCase {
	
	public void testProcessSeeTag() {

		PackageDoc packageDoc = Mockito.mock(PackageDoc.class);
		Mockito.when(packageDoc.name()).thenReturn("org.oddjob.tools.doclet");

		ClassDoc classDoc = Mockito.mock(ClassDoc.class);
		Mockito.when(classDoc.containingPackage()).thenReturn(packageDoc);
		
		ClassDoc referencedClassDock = Mockito.mock(ClassDoc.class);
		Mockito.when(referencedClassDock.name()).thenReturn("Apples");
		
		SeeTag seeTag = Mockito.mock(SeeTag.class);
		Mockito.when(seeTag.holder()).thenReturn(classDoc);
		Mockito.when(seeTag.referencedClassName()).thenReturn(
				"org.oddjob.somewhere.else.Apples");
		Mockito.when(seeTag.referencedClass()).thenReturn(referencedClassDock);
		
		SeeTagProcessor test = new SeeTagProcessor();
		
		String result = test.process(seeTag);
		
		assertEquals("<code><a href='" +
				"../../../../org/oddjob/somewhere/else/Apples.html'>" +
				"Apples</a></code>", result);
	}
}
