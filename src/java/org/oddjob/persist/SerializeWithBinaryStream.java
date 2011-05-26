package org.oddjob.persist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class SerializeWithBinaryStream {

	public InputStream toStream(Object object) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ObjectOutput oo = new ObjectOutputStream(os);
			oo.writeObject(object);
			oo.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new ByteArrayInputStream(os.toByteArray());
	}
	
	public Object fromStream(InputStream stream, ClassLoader classLoader) {
		try {
			ObjectInput oi = new OddjobObjectInputStream(stream, classLoader);
			Object o = oi.readObject();
			oi.close();
			
			return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
}
