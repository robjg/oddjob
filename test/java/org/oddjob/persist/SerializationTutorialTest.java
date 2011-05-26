package org.oddjob.persist;

import java.io.IOException;
import java.io.NotSerializableException;
import java.io.Serializable;

import junit.framework.TestCase;

import org.oddjob.Helper;

public class SerializationTutorialTest extends TestCase {

	static class NonSerializable {
		String fruit;
	}
	
	static class SerializableSub extends NonSerializable implements Serializable {
		private static final long serialVersionUID = 2009012200L;
		
		String colour;
	}	
	
	public void testBaseSerialization() throws IOException, ClassNotFoundException {
		
		SerializableSub sub = new SerializableSub();
		sub.fruit = "apple";
		sub.colour = "red";
		
		SerializableSub copy =  (SerializableSub) Helper.copy(sub);

		assertNull(copy.fruit);
		assertEquals("red", copy.colour);
	}
	
	static class ThinksItsSerializable implements Serializable {
		private static final long serialVersionUID = 2009012200L;

		NonSerializable memeber = new NonSerializable();
	}
	
	public void testNonSerializableMemeber() throws ClassNotFoundException {
		
		try {
			Helper.copy(
					new ThinksItsSerializable());
			fail("Non serializable memeber");
		} catch (IOException e) {
			assertTrue(e instanceof NotSerializableException);
		} 
	}
}
