/*
 * (c) Rob Gordon 2005
 */
package org.oddjob.state;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.oddjob.MockStateful;
import org.oddjob.OddjobException;
import org.oddjob.OjTestCase;

import java.io.*;

import static org.hamcrest.Matchers.is;

/**
 * 
 */
public class JobStateEventTest extends OjTestCase {

	String message = "This should serialize.";
	
	static class NotSerializable {
		
	}
	
	static class OurStateful extends MockStateful {
		
		public void addStateListener(StateListener listener) {
			throw new RuntimeException("Unexpected.");
		}
		
		public void removeStateListener(StateListener listener) {
			throw new RuntimeException("Unexpected.");
		}

		@Override
		public String toString() {
			return OurStateful.class.getSimpleName();
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

		StateInstant instant = StateInstant.parse("2023-06-15T06:24:30Z");

		StateEvent event = StateEvent.exceptionAtInstant(source,
				JobState.EXCEPTION, instant,
				new OddjobException(message));

		StateEvent.SerializableNoSource event2 = 
				(StateEvent.SerializableNoSource) outAndBack(
						event.serializable());
		
		assertEquals(JobState.EXCEPTION, event2.getState());
		assertEquals(instant.getInstant(), event2.getInstant());
		assertEquals(message, event2.getException().getMessage());
	}
	
    @Test
	public void testSerializeWithNoneSerializableException() 
	throws IOException, ClassNotFoundException {
		
		OurStateful source = new OurStateful();

		StateInstant instant = StateInstant.parse("2023-06-15T06:24:30Z");

		StateEvent event = StateEvent.exceptionAtInstant(source,
				JobState.EXCEPTION, instant,
				new NotSerializableException());

		StateEvent.SerializableNoSource event2 = 
				(StateEvent.SerializableNoSource) outAndBack(
						event.serializable());
		
		assertEquals(JobState.EXCEPTION, event2.getState());
		assertEquals(instant.getInstant(), event2.getInstant());
		assertEquals(StateEvent.REPLACEMENT_EXCEPTION_TEXT + message, 
				event2.getException().getMessage());
	}
	
   @Test
	public void testSerializeComplete() throws IOException, ClassNotFoundException {
		OurStateful source = new OurStateful();

	   StateInstant instant = StateInstant.parse("2023-06-15T06:24:30Z");

	   StateEvent event = StateEvent.exceptionAtInstant(source,
				JobState.COMPLETE, instant, null);

		StateEvent.SerializableNoSource event2 = 
				(StateEvent.SerializableNoSource) outAndBack(
						event.serializable());
		
		assertEquals(JobState.COMPLETE, event2.getState());
		assertEquals(instant.getInstant(), event2.getInstant());
		assertEquals(null, event2.getException());
	}
	
	
	Object outAndBack(Object object) throws IOException, ClassNotFoundException {
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutput oo = new ObjectOutputStream(os);
		oo.writeObject(object);
		oo.close();

		ByteArrayInputStream in = new ByteArrayInputStream(os.toByteArray());
		ObjectInput oi = new ObjectInputStream(in);

		return oi.readObject();
	}

	@Test
	public void testToString() {

		StateInstant instant = StateInstant.parse("2022-03-07T07:01:00Z");

		StateEvent test = StateEvent.atInstant(new OurStateful(), JobState.COMPLETE, instant);

		MatcherAssert.assertThat(test.toString(),
				is("JobStateEvent, source=OurStateful, COMPLETE at 2022-03-07T07:01:00Z"));
	}
}
