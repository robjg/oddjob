package org.oddjob.persist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

public class SerializeWithBytes {

	public byte[] toBytes(Object object) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try {
			ObjectOutput oo = new ObjectOutputStream(os);
			oo.writeObject(object);
			oo.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return os.toByteArray();
	}
	
	public Object fromBytes(byte[] bytes, ClassLoader classLoader) {
		ByteArrayInputStream is = new ByteArrayInputStream(bytes);
		try {
			ObjectInput oi = new OddjobObjectInputStream(is, classLoader);
			Object o = oi.readObject();
			oi.close();
			
			return o;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}		
	}
	
}
