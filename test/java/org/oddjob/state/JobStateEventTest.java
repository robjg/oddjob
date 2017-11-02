/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.oddjob.OjTestCase;

import org.oddjob.MockStateful;
import org.oddjob.OddjobException;

/**
 * 
 */
public class JobStateEventTest extends OjTestCase {

	String message = "This should serialize.";
	
	class NotSerializable {
		
	}
	
	class OurStateful extends MockStateful {
		
		public void addStateListener(StateListener listener) {
			throw new RuntimeException("Unexpected.");
		}
		
		public void removeStateListener(StateListener listener) {
			throw new RuntimeException("Unexpected.");
		}
	}
	
	class NotSerializableException extends Exception {
		private static final long serialVersionUID = 2009021000L; 
			
		public NotSerializableException() {
			super(message);
		}
		NotSerializable ns = new NotSerializable();
	}
	
    @Test
	public void testSerializeWithSerializableException() 
	throws IOException, ClassNotFoundException {
		
		OurStateful source = new OurStateful();
		
		StateEvent event = new StateEvent(source, 
				JobState.EXCEPTION, new Date(1234), 
				new OddjobException(message));

		StateEvent.SerializableNoSource event2 = 
				(StateEvent.SerializableNoSource) outAndBack(
						event.serializable());
		
		assertEquals(JobState.EXCEPTION, event2.getState());
		assertEquals(1234, event2.getTime().getTime());
		assertEquals(message, event2.getException().getMessage());
	}
	
    @Test
	public void testSerializeWithNoneSerializableException() 
	throws IOException, ClassNotFoundException {
		
		OurStateful source = new OurStateful();
		
		StateEvent event = new StateEvent(source, 
				JobState.EXCEPTION, new Date(1234), 
				new NotSerializableException());

		StateEvent.SerializableNoSource event2 = 
				(StateEvent.SerializableNoSource) outAndBack(
						event.serializable());
		
		assertEquals(JobState.EXCEPTION, event2.getState());
		assertEquals(1234, event2.getTime().getTime());
		assertEquals(StateEvent.REPLACEMENT_EXCEPTION_TEXT + message, 
				event2.getException().getMessage());
	}
	
   @Test
	public void testSerializeComplete() throws IOException, ClassNotFoundException {
		OurStateful source = new OurStateful();
		
		StateEvent event = new StateEvent(source, 
				JobState.COMPLETE, new Date(1234), null);

		StateEvent.SerializableNoSource event2 = 
				(StateEvent.SerializableNoSource) outAndBack(
						event.serializable());
		
		assertEquals(JobState.COMPLETE, event2.getState());
		assertEquals(1234, event2.getTime().getTime());
		assertEquals(null, event2.getException());
	}
	
	
	Object outAndBack(Object object) throws IOException, ClassNotFoundException {
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutput oo = new ObjectOutputStream(os);
		oo.writeObject(object);
		oo.close();

		ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());
		ObjectInput oi = new ObjectInputStream(in);
		
		Object o = oi.readObject();
		return o;
	}
}
