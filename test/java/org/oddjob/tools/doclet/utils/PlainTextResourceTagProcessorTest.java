package org.oddjob.tools.doclet.utils;

import org.junit.Test;

import org.oddjob.OjTestCase;

import org.mockito.Mockito;
import org.oddjob.doclet.CustomTagNames;
import org.oddjob.tools.OddjobTestHelper;
import org.oddjob.tools.includes.PlainTextResourceLoader;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Tag;

public class PlainTextResourceTagProcessorTest extends OjTestCase {
		
   @Test
	public void testProcessTag() {

		PackageDoc packageDoc = Mockito.mock(PackageDoc.class);
		Mockito.when(packageDoc.name()).thenReturn("org.oddjob.tools.doclet");

		ClassDoc classDoc = Mockito.mock(ClassDoc.class);
		Mockito.when(classDoc.containingPackage()).thenReturn(packageDoc);
		
		ClassDoc referencedClassDock = Mockito.mock(ClassDoc.class);
		Mockito.when(referencedClassDock.name()).thenReturn("Apples");
		
		Tag tag = Mockito.mock(Tag.class);
		Mockito.when(tag.text()).thenReturn(
				"org/oddjob/tools/doclet/utils/SomePlainText.txt");
		Mockito.when(tag.name()).thenReturn("@oddjob.text.resource");
		
		GenericIncludeTagProcessor test = new GenericIncludeTagProcessor(
				CustomTagNames.TEXT_RESOURCE_TAG, new PlainTextResourceLoader());
		
		String result = test.process(tag);

		String expected = 
				"<pre>" + OddjobTestHelper.LS +
				"Remember 2 < 3 & 5 > 4" + OddjobTestHelper.LS +
				"But This is a new line." + OddjobTestHelper.LS +
				"</pre>" + OddjobTestHelper.LS;
		
		assertEquals(expected, result);
	}
}
