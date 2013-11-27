package org.oddjob.tools.doclet.utils;

import junit.framework.TestCase;

import org.mockito.Mockito;
import org.oddjob.OddjobTestHelper;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Tag;

public class XMLResourceTagProcessorTest extends TestCase {
		
	public void testProcessTag() {

		PackageDoc packageDoc = Mockito.mock(PackageDoc.class);
		Mockito.when(packageDoc.name()).thenReturn("org.oddjob.tools.doclet");

		ClassDoc classDoc = Mockito.mock(ClassDoc.class);
		Mockito.when(classDoc.containingPackage()).thenReturn(packageDoc);
		
		ClassDoc referencedClassDock = Mockito.mock(ClassDoc.class);
		Mockito.when(referencedClassDock.name()).thenReturn("Apples");
		
		Tag tag = Mockito.mock(Tag.class);
		Mockito.when(tag.text()).thenReturn(
				"org/oddjob/tools/doclet/utils/SomeXML.xml");
		Mockito.when(tag.name()).thenReturn("@oddjob.xml.resource");
		
		XMLResourceTagProcessor test = new XMLResourceTagProcessor();
		
		String result = test.process(tag);
		
		assertEquals("<pre class=\"xml\">" + OddjobTestHelper.LS +
				"&lt;hello/&gt;</pre>" + OddjobTestHelper.LS, 
				result);
	}
}
